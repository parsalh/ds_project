package gr.hua.dit.project.core.service.impl;

import gr.hua.dit.project.core.integration.ExternalDistanceAdapter;
import gr.hua.dit.project.core.model.Address;
import gr.hua.dit.project.core.model.OpenHour;
import gr.hua.dit.project.core.model.Person;
import gr.hua.dit.project.core.model.Restaurant;
import gr.hua.dit.project.core.port.GeocodingService;
import gr.hua.dit.project.core.repository.PersonRepository;
import gr.hua.dit.project.core.repository.RestaurantRepository;
import gr.hua.dit.project.core.service.RestaurantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final PersonRepository personRepository;
    private final GeocodingService geocodingService;
    private final ExternalDistanceAdapter distanceAdapter;

    public RestaurantServiceImpl(RestaurantRepository restaurantRepository,
                                 PersonRepository personRepository,
                                 GeocodingService geocodingService,
                                 ExternalDistanceAdapter distanceAdapter) {
        this.restaurantRepository = restaurantRepository;
        this.personRepository = personRepository;
        this.geocodingService = geocodingService;
        this.distanceAdapter = distanceAdapter;
    }

    @Override
    @Transactional(readOnly = true) // Required because isOpen() accesses lazy collection 'openHours'
    public List<Restaurant> getTop15NearbyRestaurants(Double userLat, Double userLon) {
        if (userLat == null || userLon == null) return new ArrayList<>();

        // 1. DB FILTER (Bounding Box)
        double range = 0.15;
        List<Restaurant> candidates = restaurantRepository.findAllByAddressInfoLatitudeBetweenAndAddressInfoLongitudeBetween(
                userLat - range, userLat + range,
                userLon - range, userLon + range
        );

        // 2. MEMORY FILTER: Open Only + Haversine Sort
        // We filter by isOpen() FIRST, so we don't waste time sorting closed restaurants
        List<Restaurant> openCandidates = candidates.stream()
                .filter(Restaurant::isOpen) // <--- NEW FILTER
                .sorted(Comparator.comparingDouble(r ->
                        calculateHaversineDistance(userLat, userLon, getLat(r), getLon(r))
                ))
                .limit(20) // Take top 20 closest OPEN restaurants
                .toList();

        // 3. API SORT (Precise Driving Distance)
        List<Restaurant> finalSorted = new ArrayList<>(openCandidates);
        finalSorted.sort((r1, r2) -> {
            double d1 = getRealDistance(userLat, userLon, r1);
            double d2 = getRealDistance(userLat, userLon, r2);
            return Double.compare(d1, d2);
        });

        // Return Top 15
        return finalSorted.stream().limit(15).toList();
    }

    private Double getLat(Restaurant r) { return r.getAddressInfo() != null ? r.getAddressInfo().getLatitude() : 0.0; }
    private Double getLon(Restaurant r) { return r.getAddressInfo() != null ? r.getAddressInfo().getLongitude() : 0.0; }

    private double getRealDistance(double uLat, double uLon, Restaurant r) {
        Double rLat = getLat(r);
        Double rLon = getLon(r);
        if (rLat == 0.0 || rLon == 0.0) return Double.MAX_VALUE;

        return distanceAdapter.getDistanceAndDuration(uLat, uLon, rLat, rLon)
                .map(metrics -> metrics.distanceMeters())
                .orElse(Double.MAX_VALUE);
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000;
    }

    @Override
    public List<Restaurant> getRestaurantsByOwner(Long ownerId){
        return restaurantRepository.findAllByOwnerId(ownerId);
    }

    @Override
    public Restaurant getRestaurantIfAuthorized(Long restaurantId, Long ownerId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(()-> new RuntimeException("Restaurant not found"));

        if (!restaurant.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Unauthorized: You do not own this restaurant");
        }
        return restaurant;
    }

    @Override
    public void createRestaurant(Restaurant restaurant, Long ownerId) {
        Person owner = personRepository.findById(ownerId)
                .orElseThrow(()-> new RuntimeException("Owner not found"));
        restaurant.setOwner(owner);

        Address address = restaurant.getAddressInfo();
        if (address == null) {
            address = new Address();
            restaurant.setAddressInfo(address);
        }

        if (address.getLatitude() == null || address.getLongitude() == null) {
            String fullAddress = getFullAddress(address);
            if (!fullAddress.isBlank()) {
                Address finalAddress = address;
                geocodingService.getCoordinates(fullAddress)
                        .ifPresent(coords -> {
                            finalAddress.setLatitude(coords[0]);
                            finalAddress.setLongitude(coords[1]);
                        });
            }
        }
        List<OpenHour> validHours = new ArrayList<>();
        if (restaurant.getOpenHours() != null) {
            for (OpenHour hours : restaurant.getOpenHours()) {
                if (hours.getOpenTime() != null && hours.getCloseTime() != null) {
                    hours.setRestaurant(restaurant);
                    validHours.add(hours);
                }
            }
        }
        restaurant.setOpenHours(validHours);
        restaurantRepository.save(restaurant);
    }

    @Override
    @Transactional
    public void updateRestaurant(Long restaurantId, Restaurant formData, Long ownerId) {
        Restaurant existingRestaurant = getRestaurantIfAuthorized(restaurantId, ownerId);

        existingRestaurant.setName(formData.getName());
        existingRestaurant.setMinimumOrderAmount(formData.getMinimumOrderAmount());
        existingRestaurant.setDeliveryFee(formData.getDeliveryFee());
        existingRestaurant.setServiceType(formData.getServiceType());
        existingRestaurant.setCuisines(formData.getCuisines());
        existingRestaurant.setImageUrl(formData.getImageUrl());

        Address newAddress = formData.getAddressInfo();
        if (newAddress != null) {
            if (newAddress.getLatitude() != null && newAddress.getLongitude() != null) {
                existingRestaurant.setAddressInfo(newAddress);
            } else {
                String fullAddress = getFullAddress(newAddress);
                geocodingService.getCoordinates(fullAddress)
                        .ifPresent(coords -> {
                            newAddress.setLatitude(coords[0]);
                            newAddress.setLongitude(coords[1]);
                        });
                existingRestaurant.setAddressInfo(newAddress);
            }
        }

        existingRestaurant.getOpenHours().clear();
        if (formData.getOpenHours() != null) {
            for (OpenHour hours : formData.getOpenHours()) {
                if (hours.getOpenTime() != null && hours.getCloseTime() != null) {
                    hours.setRestaurant(existingRestaurant);
                    existingRestaurant.getOpenHours().add(hours);
                }
            }
        }
        restaurantRepository.save(existingRestaurant);
    }

    private String getFullAddress(Address address) {
        if (address == null) return "";
        String street = address.getStreet() != null ? address.getStreet() : "";
        String number = address.getNumber() != null ? " " + address.getNumber() : "";
        String zip = address.getZipCode() != null ? ", " + address.getZipCode() : "";
        return (street + number + zip).trim();
    }

    @Override
    public Optional<Restaurant> findById(Long id) {
        return restaurantRepository.findById(id);
    }

    @Override
    public List<Restaurant> findAll() {
        return restaurantRepository.findAll();
    }
}
package gr.hua.dit.project.core.service.impl;

import gr.hua.dit.project.core.model.Person;
import gr.hua.dit.project.core.model.Restaurant;
import gr.hua.dit.project.core.port.GeocodingService;
import gr.hua.dit.project.core.repository.PersonRepository;
import gr.hua.dit.project.core.repository.RestaurantRepository;
import gr.hua.dit.project.core.service.RestaurantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final PersonRepository personRepository;
    private final GeocodingService geocodingService;

    public RestaurantServiceImpl(RestaurantRepository restaurantRepository,
                                 PersonRepository personRepository,
                                 GeocodingService geocodingService) {
        this.restaurantRepository = restaurantRepository;
        this.personRepository = personRepository;
        this.geocodingService = geocodingService;
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

        geocodingService.getCoordinates(restaurant.getAddress())
                        .ifPresent(coords -> {
                            restaurant.setLatitude(coords[0]);
                            restaurant.setLongitude(coords[1]);
                        });

        restaurantRepository.save(restaurant);
    }

    @Override
    public void updateRestaurant(Long restaurantId,
                                 Restaurant formData,
                                 Long ownerId) {
        Restaurant existingRestaurant = getRestaurantIfAuthorized(restaurantId, ownerId);

        existingRestaurant.setName(formData.getName());
        existingRestaurant.setAddress(formData.getAddress());
        existingRestaurant.setMinimumOrderAmount(formData.getMinimumOrderAmount());
        existingRestaurant.setDeliveryFee(formData.getDeliveryFee());
        existingRestaurant.setServiceType(formData.getServiceType());
        existingRestaurant.setCuisines(formData.getCuisines());

        geocodingService.getCoordinates(formData.getAddress())
                        .ifPresent(coords -> {
                            existingRestaurant.setLatitude(coords[0]);
                            existingRestaurant.setLongitude(coords[1]);
                        });

        restaurantRepository.save(existingRestaurant);
    }

}

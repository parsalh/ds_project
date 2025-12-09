package gr.hua.dit.project.core.service;

import gr.hua.dit.project.core.model.Restaurant;

import java.util.List;

public interface RestaurantService {

    List<Restaurant> getRestaurantsByOwner(Long ownerId);

    // βρισκει ενα εστιατοριο βασει id και ελεγχει αν ανηκει στον owner
    Restaurant getRestaurantIfAuthorized(Long restaurantId, Long ownerId);

    void createRestaurant(Restaurant restaurant, Long ownerId);

    void updateRestaurant(Long restaurantId,
                          Restaurant formData,
                          Long ownerId);


}

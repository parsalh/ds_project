package gr.hua.dit.project.core.service;

import gr.hua.dit.project.core.model.MenuItem;
import java.util.List;
import java.util.Optional;

public interface MenuItemService {

    List<MenuItem> findAll();


    Optional<MenuItem> findById(Long id);


    List<MenuItem> findByRestaurantId(Long restaurantId);


    MenuItem save(MenuItem menuItem);


    void deleteById(Long id);
}

package gr.hua.dit.project.web.rest;

import gr.hua.dit.project.core.model.MenuItem;
import gr.hua.dit.project.core.model.Restaurant;
import gr.hua.dit.project.core.repository.MenuItemRepository;
import gr.hua.dit.project.core.service.MenuItemService;
import gr.hua.dit.project.core.service.RestaurantService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/restaurants")
public class RestaurantRestController {

    private final RestaurantService restaurantService;
    private final MenuItemService menuItemService;

    public RestaurantRestController(RestaurantService restaurantService, MenuItemService menuItemService) {
        this.restaurantService = restaurantService;
        this.menuItemService = menuItemService;
    }

    @GetMapping
    public ResponseEntity<List<Restaurant>> getRestaurants() {
        return ResponseEntity.ok(restaurantService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Restaurant> getRestaurant(@PathVariable Long id) {
        return restaurantService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/menu")
    public ResponseEntity<List<MenuItem>> getRestaurantMenu(@PathVariable Long id) {
        if (restaurantService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(menuItemService.findByRestaurantId(id));
    }

}

package gr.hua.dit.project.web.rest;

import gr.hua.dit.project.core.model.MenuItem;
import gr.hua.dit.project.core.model.Restaurant;
import gr.hua.dit.project.core.repository.MenuItemRepository;
import gr.hua.dit.project.core.service.MenuItemService;
import gr.hua.dit.project.core.service.RestaurantService;
import gr.hua.dit.project.web.rest.dto.MenuItemDTO;
import gr.hua.dit.project.web.rest.dto.RestaurantDTO;
import gr.hua.dit.project.web.rest.mapper.RestaurantRestMapper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/restaurants")
public class RestaurantRestController {

    private final RestaurantService restaurantService;
    private final MenuItemService menuItemService;
    private final RestaurantRestMapper restaurantRestMapper;

    public RestaurantRestController(RestaurantService restaurantService,
                                    MenuItemService menuItemService,
                                    RestaurantRestMapper restaurantRestMapper) {
        this.restaurantService = restaurantService;
        this.menuItemService = menuItemService;
        this.restaurantRestMapper = restaurantRestMapper;
    }

    @GetMapping
    public ResponseEntity<List<RestaurantDTO>> getRestaurants() {
        List<Restaurant> restaurants = restaurantService.findAll();

        List<RestaurantDTO> restaurantDTOS = restaurants.stream()
                .map(restaurantRestMapper::toDTO)
                .toList();

        return ResponseEntity.ok(restaurantDTOS);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDTO> getRestaurant(@PathVariable Long id) {
        return restaurantService.findById(id)
                .map(restaurantRestMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/menu")
    public ResponseEntity<List<MenuItemDTO>> getRestaurantMenu(@PathVariable Long id) {
        if (restaurantService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<MenuItem> menuItems = menuItemService.findByRestaurantId(id);

        List<MenuItemDTO> menuItemDTOS = menuItems.stream()
                .map(restaurantRestMapper::toDTO)
                .toList();

        return ResponseEntity.ok(menuItemDTOS);
    }

}

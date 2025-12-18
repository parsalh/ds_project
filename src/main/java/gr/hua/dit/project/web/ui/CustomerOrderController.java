package gr.hua.dit.project.web.ui;

import gr.hua.dit.project.core.model.ItemType;
import gr.hua.dit.project.core.model.MenuItem;
import gr.hua.dit.project.core.model.Restaurant;
import gr.hua.dit.project.core.service.MenuItemService;
import gr.hua.dit.project.core.service.RestaurantService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/restaurants")
public class CustomerOrderController {

    private final RestaurantService restaurantService;
    private final MenuItemService menuItemService;

    public CustomerOrderController(RestaurantService restaurantService,
                                   MenuItemService menuItemService) {
        this.restaurantService = restaurantService;
        this.menuItemService = menuItemService;
    }

    @GetMapping("/{restaurantId}/menu")
    public String viewMenu(@PathVariable Long restaurantId, Model model) {

        Restaurant restaurant = restaurantService.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        List<MenuItem> menuItems = menuItemService.findByRestaurantId(restaurantId);

        Map<ItemType, List<MenuItem>> groupedItems =
                menuItems.stream()
                        .filter(MenuItem::getAvailable)
                        .collect(Collectors.groupingBy(MenuItem::getType));

        model.addAttribute("restaurant", restaurant);
        model.addAttribute("groupedMenuItems", groupedItems);

        return "RestaurantMenuView";
    }

}


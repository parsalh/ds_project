package gr.hua.dit.project.web.ui;

import gr.hua.dit.project.core.model.ItemType;
import gr.hua.dit.project.core.model.MenuItem;
import gr.hua.dit.project.core.model.Person;
import gr.hua.dit.project.core.model.Restaurant;
import gr.hua.dit.project.core.repository.PersonRepository; // Added
import gr.hua.dit.project.core.service.CustomerOrderService;
import gr.hua.dit.project.core.service.MenuItemService;
import gr.hua.dit.project.core.service.RestaurantService;
import gr.hua.dit.project.core.service.model.CreateOrderRequest;
import gr.hua.dit.project.core.security.CurrentUser;       // Added
import gr.hua.dit.project.core.security.CurrentUserProvider; // Added
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/restaurants")
public class CustomerOrderController {

    private final RestaurantService restaurantService;
    private final MenuItemService menuItemService;
    private final CustomerOrderService customerOrderService;
    private final CurrentUserProvider currentUserProvider;
    private final PersonRepository personRepository;

    public CustomerOrderController(RestaurantService restaurantService,
                                   MenuItemService menuItemService,
                                   CustomerOrderService customerOrderService,
                                   CurrentUserProvider currentUserProvider,
                                   PersonRepository personRepository) {
        this.restaurantService = restaurantService;
        this.menuItemService = menuItemService;
        this.customerOrderService = customerOrderService;
        this.currentUserProvider = currentUserProvider;
        this.personRepository = personRepository;
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

        return "restaurantMenuView";
    }

    @GetMapping("/{restaurantId}/order/finalize")
    public String finalizeOrder(@PathVariable Long restaurantId, Model model) {
        CurrentUser currentUser = currentUserProvider.requireCurrentUser();

        Restaurant restaurant = restaurantService.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        Person customer = personRepository.findById(currentUser.id())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("restaurant", restaurant);
        model.addAttribute("user", customer); // Pass user to get address

        return "finalizeOrder";
    }

    @PostMapping("/{restaurantId}/order")
    public String placeOrder(@PathVariable Long restaurantId,
                             @ModelAttribute CreateOrderRequest createOrderRequest) {

        customerOrderService.createOrder(createOrderRequest);
        return "redirect:/customer/profile";
    }
}
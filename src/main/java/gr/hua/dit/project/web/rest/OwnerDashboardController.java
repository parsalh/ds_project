package gr.hua.dit.project.web.rest;

import gr.hua.dit.project.core.model.*;
import gr.hua.dit.project.core.repository.MenuItemRepository;
import gr.hua.dit.project.core.service.RestaurantService;
import gr.hua.dit.project.core.security.ApplicationUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/owner")
public class OwnerDashboardController {

    private final RestaurantService restaurantService;
    private final MenuItemRepository menuItemRepository;

    public OwnerDashboardController(RestaurantService restaurantService,
                                    MenuItemRepository menuItemRepository) {

        this.restaurantService = restaurantService;
        this.menuItemRepository = menuItemRepository;

    }

    @GetMapping("/dashboard")
    public String ownerDashboard(Authentication authentication, Model model) {
        ApplicationUserDetails userDetails = (ApplicationUserDetails) authentication.getPrincipal();
        Long ownerId = userDetails.personId();

        List<Restaurant> myRestaurants = restaurantService.getRestaurantsByOwner(ownerId);

        model.addAttribute("restaurants", myRestaurants);
        model.addAttribute("username", userDetails.getUsername());

        return "ownerDashboard";
    }

    @GetMapping("/restaurant/new")
    public String showAddRestaurantForm(Model model) {

        Restaurant restaurant = new Restaurant();
        prepareOpenHours(restaurant);

        model.addAttribute("restaurant", restaurant);
        model.addAttribute("cuisines", Cuisine.values());
        model.addAttribute("serviceTypes", ServiceType.values());

        return "restaurantForm";

    }

    @PostMapping("/restaurant/new")
    public String saveRestaurant(@ModelAttribute("restaurant") Restaurant restaurant,
                                 Authentication authentication) {

        ApplicationUserDetails userDetails = (ApplicationUserDetails) authentication.getPrincipal();

        restaurantService.createRestaurant(restaurant, userDetails.personId());

        return "redirect:/owner/dashboard";

    }

    @GetMapping("/restaurant/{id}/edit")
    public String showEditRestaurantForm(@PathVariable Long id,
                                         Model model,
                                         Authentication authentication) {

        ApplicationUserDetails userDetails = (ApplicationUserDetails) authentication.getPrincipal();

        Restaurant restaurant = restaurantService.getRestaurantIfAuthorized(id, userDetails.personId());
        prepareOpenHours(restaurant);

        model.addAttribute("restaurant", restaurant);
        model.addAttribute("cuisines", Cuisine.values());
        model.addAttribute("serviceTypes", ServiceType.values());

        return "restaurantForm";

    }

    @PostMapping("/restaurant/{id}/edit")
    public String updateRestaurant(@PathVariable Long id,
                                   @ModelAttribute("restaurant") Restaurant formData,
                                   Authentication authentication) {
        ApplicationUserDetails userDetails = (ApplicationUserDetails) authentication.getPrincipal();

        restaurantService.updateRestaurant(id, formData, userDetails.personId());

        return "redirect:/owner/dashboard";
    }

    @GetMapping("/restaurant/{restaurantId}/menu/new")
    public String showAddMenuItemForm(@PathVariable Long restaurantId,
                                      Model model) {
        MenuItem menuItem = new MenuItem();
        model.addAttribute("menuItem", menuItem);
        model.addAttribute("restaurantId", restaurantId);
        model.addAttribute("itemTypes", ItemType.values());

        return "menuItemForm";
    }

    @GetMapping("/restaurant/{restaurantId}/menu/{menuId}/edit")
    public String showEditMenuItemForm(@PathVariable Long restaurantId,
                                       @PathVariable Long menuId,
                                       Model model) {

        MenuItem menuItem = menuItemRepository.findById(menuId)
                .orElseThrow(()-> new RuntimeException("Item not found"));

        model.addAttribute("menuItem", menuItem);
        model.addAttribute("restaurantId", restaurantId);
        model.addAttribute("itemTypes", ItemType.values());

        return "menuItemForm";

    }

    @PostMapping("/restaurant/{restaurantId}/menu/save")
    public String saveMenuItem(@PathVariable Long restaurantId,
                               @ModelAttribute("menuItem") MenuItem menuItem,
                               Authentication authentication) {

        ApplicationUserDetails userDetails = (ApplicationUserDetails) authentication.getPrincipal();

        Restaurant restaurant = restaurantService.getRestaurantIfAuthorized(restaurantId, userDetails.personId());

        if (menuItem.getId() != null) {
            MenuItem existing = menuItemRepository.findById(menuItem.getId()).get();
            existing.setName(menuItem.getName());
            existing.setPrice(menuItem.getPrice());
            existing.setDescription(menuItem.getDescription());
            existing.setAvailable(menuItem.getAvailable());
            existing.setType(menuItem.getType());
            menuItemRepository.save(existing);
        } else {
            menuItem.setRestaurant(restaurant);
            menuItemRepository.save(menuItem);
        }

        return "redirect:/owner/restaurant/" + restaurantId + "/edit";
    }

    @GetMapping("/restaurant/{restaurantId}/menu/{menuId}/delete")
    public String deleteMenuItem(@PathVariable Long restaurantId,
                                 @PathVariable Long menuId,
                                 Authentication authentication) {

        ApplicationUserDetails userDetails = (ApplicationUserDetails) authentication.getPrincipal();

        restaurantService.getRestaurantIfAuthorized(restaurantId, userDetails.personId());

        menuItemRepository.deleteById(menuId);

        return "redirect:/owner/restaurant/" + restaurantId + "/edit";
    }

    private void prepareOpenHours(Restaurant restaurant) {
        if (restaurant.getOpenHours() == null) {
            restaurant.setOpenHours(new ArrayList<>());
        }

        List<OpenHour> hours = restaurant.getOpenHours();

        for (DayOfWeek day : DayOfWeek.values()) {
            boolean exists = hours.stream().anyMatch(h -> h.getDayOfWeek() == day);

            if (!exists) {
                OpenHour newHour = new OpenHour();
                newHour.setDayOfWeek(day);
                newHour.setRestaurant(restaurant);
                hours.add(newHour);
            }
        }

        hours.sort(Comparator.comparing(OpenHour::getDayOfWeek));

    }

}
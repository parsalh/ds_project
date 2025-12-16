package gr.hua.dit.project.web;

import gr.hua.dit.project.core.model.Cuisine;
import gr.hua.dit.project.core.repository.RestaurantRepository;
import gr.hua.dit.project.web.rest.AuthController;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final RestaurantRepository restaurantRepository;

    public HomeController(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @GetMapping("/")
    public String index(
            final Authentication authentication,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Cuisine cuisine,
            Model model
    ) {
        if (AuthController.isAuthenticated(authentication)) {
            boolean isOwner = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

            if (isOwner) {
                return "redirect:/owner/dashboard";
            }
        }

        if (query != null && !query.isBlank()) {
            model.addAttribute("restaurants", restaurantRepository.findByNameContainingIgnoreCase(query));
        } else if (cuisine != null) {
            model.addAttribute("restaurants", restaurantRepository.findAllByCuisinesContaining(cuisine));
        } else {
            model.addAttribute("restaurants", restaurantRepository.findAll());
        }

    model.addAttribute("cuisines", Cuisine.values());

    return "index";

    }

}

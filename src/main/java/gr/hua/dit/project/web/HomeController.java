package gr.hua.dit.project.web;

import gr.hua.dit.project.core.model.Cuisine;
import gr.hua.dit.project.core.repository.RestaurantRepository;
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
            @RequestParam(required = false) String query,
            Model model
    ) { if (query != null && !query.isBlank()) {
        model.addAttribute("restaurant", restaurantRepository.findByNameContainingIgnoreCase(query));
    } else {
        model.addAttribute("restaurants", restaurantRepository.findAll());
    }

    model.addAttribute("cuisines", Cuisine.values());

    return "index";

    }

}

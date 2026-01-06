package gr.hua.dit.project.web;

import gr.hua.dit.project.core.model.Address;
import gr.hua.dit.project.core.model.Cuisine;
import gr.hua.dit.project.core.model.Person;
import gr.hua.dit.project.core.repository.PersonRepository;
import gr.hua.dit.project.core.repository.RestaurantRepository;
import gr.hua.dit.project.core.service.RestaurantService;
import gr.hua.dit.project.web.ui.AuthController;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;

@Controller
public class HomeController {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantService restaurantService;
    private final PersonRepository personRepository;

    public HomeController(RestaurantRepository restaurantRepository,
                          RestaurantService restaurantService,
                          PersonRepository personRepository) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantService = restaurantService;
        this.personRepository = personRepository;
    }

    @GetMapping("/")
    public String index(
            final Authentication authentication,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model
    ) {
        if (AuthController.isAuthenticated(authentication)) {
            boolean isOwner = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));
            if (isOwner) {
                return "redirect:/owner/dashboard";
            }
        }

        if (lat == null && lon == null && (query == null || query.isBlank())) {

            Double targetLat = null;
            Double targetLon = null;

            // Get cookies
            Double cookieLat = getCookieValue(request, "sf_lat");
            Double cookieLon = getCookieValue(request, "sf_lon");

            // Initialize target with cookies if available
            if (cookieLat != null && cookieLon != null) {
                targetLat = cookieLat;
                targetLon = cookieLon;
            }

            // Check if user is authenticated
            if (AuthController.isAuthenticated(authentication)) {
                String username = authentication.getName();
                Person person = personRepository.findByUsernameIgnoreCase(username).orElse(null);

                if (person != null) {
                    model.addAttribute("userAddresses", person.getAddresses());

                    if (cookieLat != null && cookieLon != null && !person.getAddresses().isEmpty()) {
                        // Logic: Find closest saved address to cookie location
                        Address closest = null;
                        double minDistance = Double.MAX_VALUE;

                        for (Address addr : person.getAddresses()) {
                            if (addr.getLatitude() != null && addr.getLongitude() != null) {
                                // Simple squared Euclidean distance for comparison
                                double dist = Math.pow(addr.getLatitude() - cookieLat, 2) +
                                        Math.pow(addr.getLongitude() - cookieLon, 2);
                                if (dist < minDistance) {
                                    minDistance = dist;
                                    closest = addr;
                                }
                            }
                        }

                        // Use closest address if found
                        if (closest != null) {
                            targetLat = closest.getLatitude();
                            targetLon = closest.getLongitude();
                        }

                    } else if (targetLat == null && !person.getAddresses().isEmpty()) {
                        // Fallback: If no cookies, pick the first address
                        Address defaultAddr = person.getAddresses().get(0);
                        targetLat = defaultAddr.getLatitude();
                        targetLon = defaultAddr.getLongitude();
                    }
                }
            }

            // Redirect if we found a valid target
            if (targetLat != null && targetLon != null) {
                return String.format("redirect:/?lat=%s&lon=%s", targetLat, targetLon);
            }
        }

        // Pass user addresses to view if authenticated (for the dropdown)
        if (AuthController.isAuthenticated(authentication)) {
            String username = authentication.getName();
            personRepository.findByUsernameIgnoreCase(username).ifPresent(person -> {
                model.addAttribute("userAddresses", person.getAddresses());
            });
        }

        if (lat != null && lon != null) {
            model.addAttribute("restaurants", restaurantService.getNearbyRestaurants(lat, lon));
            model.addAttribute("selectedLocation", true);
        } else if (query != null && !query.isBlank()) {
            model.addAttribute("restaurants", restaurantRepository.findByNameContainingIgnoreCase(query));
        } else {
            model.addAttribute("restaurants", restaurantRepository.findAll());
        }

        model.addAttribute("cuisines", Cuisine.values());

        return "index";
    }

    private Double getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals(name))
                .map(Cookie::getValue)
                .map(val -> {
                    try { return Double.parseDouble(val); } catch (NumberFormatException e) { return null; }
                })
                .filter(val -> val != null)
                .findFirst()
                .orElse(null);
    }
}
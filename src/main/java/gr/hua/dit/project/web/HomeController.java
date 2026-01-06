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
        // 1. Owner Redirect
        if (AuthController.isAuthenticated(authentication)) {
            boolean isOwner = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));
            if (isOwner) {
                return "redirect:/owner/dashboard";
            }
        }

        // 2. Intelligent Redirect / Defaulting Logic
        // If the user did NOT provide specific search params (lat, lon, or text query)
        // We try to "guess" their location from cookies or profile to provide a better default view.
        if (lat == null && lon == null && (query == null || query.isBlank())) {

            Double targetLat = null;
            Double targetLon = null;

            // A. Check Cookies first (works for guests AND logged-in users who recently selected a location)
            Double cookieLat = getCookieValue(request, "sf_lat");
            Double cookieLon = getCookieValue(request, "sf_lon");

            if (cookieLat != null && cookieLon != null) {
                targetLat = cookieLat;
                targetLon = cookieLon;
            }

            // B. If Logged In: Check saved addresses if cookies are missing
            if (AuthController.isAuthenticated(authentication)) {
                String username = authentication.getName();
                Person person = personRepository.findByUsernameIgnoreCase(username).orElse(null);

                if (person != null) {
                    model.addAttribute("userAddresses", person.getAddresses());

                    // If we haven't found a location from cookies, use the first saved address
                    if (targetLat == null && !person.getAddresses().isEmpty()) {
                        Address defaultAddr = person.getAddresses().get(0);
                        targetLat = defaultAddr.getLatitude();
                        targetLon = defaultAddr.getLongitude();
                    }
                }
            }

            // C. PERFORM REDIRECT
            // If we found a valid location, redirect the user to the parameterized URL.
            if (targetLat != null && targetLon != null) {
                return String.format("redirect:/?lat=%s&lon=%s", targetLat, targetLon);
            }
        }
        // --- End of Defaulting Logic ---


        // 3. Normal Page Rendering
        // At this point, we either have explicit params (lat/lon) or we are showing the generic "All" list.

        if (AuthController.isAuthenticated(authentication)) {
            String username = authentication.getName();
            personRepository.findByUsernameIgnoreCase(username).ifPresent(person -> {
                model.addAttribute("userAddresses", person.getAddresses());
            });
        }

        if (lat != null && lon != null) {
            // Get Top 15 Nearby (Cuisine filtering is now handled Client-Side in HTML)
            model.addAttribute("restaurants", restaurantService.getTop15NearbyRestaurants(lat, lon));
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
package gr.hua.dit.project.web.ui;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import gr.hua.dit.project.core.model.*;
import gr.hua.dit.project.core.repository.CustomerOrderRepository;
import gr.hua.dit.project.core.repository.MenuItemRepository;
import gr.hua.dit.project.core.repository.PersonRepository;
import gr.hua.dit.project.core.service.CustomerOrderService;
import gr.hua.dit.project.core.service.PersonService;
import gr.hua.dit.project.core.service.RestaurantService;
import gr.hua.dit.project.core.service.model.UpdatePersonRequest;
import gr.hua.dit.project.core.security.ApplicationUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/owner")
public class OwnerDashboardController {

    private final RestaurantService restaurantService;
    private final MenuItemRepository menuItemRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final CustomerOrderService customerOrderService;
    private final PersonService personService;
    private final PersonRepository personRepository;

    public OwnerDashboardController(RestaurantService restaurantService,
                                    MenuItemRepository menuItemRepository,
                                    CustomerOrderRepository customerOrderRepository,
                                    CustomerOrderService customerOrderService,
                                    PersonService personService,
                                    PersonRepository personRepository) {

        this.restaurantService = restaurantService;
        this.menuItemRepository = menuItemRepository;
        this.customerOrderRepository = customerOrderRepository;
        this.customerOrderService = customerOrderService;
        this.personService = personService;
        this.personRepository = personRepository;
    }

    public record CountryCodeOption(String code, String label, String isoCode) {}

    private List<CountryCodeOption> getCountryCodes(){
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        Set<String> regions = phoneNumberUtil.getSupportedRegions();
        List<CountryCodeOption> options = new ArrayList<>();
        for (String region : regions) {
            int countryCode = phoneNumberUtil.getCountryCodeForRegion(region);
            String countryName = new Locale("", region).getDisplayCountry(Locale.ENGLISH);
            options.add(new CountryCodeOption("+" + countryCode, countryName + " (+" + countryCode + ")", region.toLowerCase()));
        }
        options.sort(Comparator.comparing(CountryCodeOption::label));
        return options;
    }

    @GetMapping("/dashboard")
    public String ownerDashboard(Authentication authentication, Model model) {
        ApplicationUserDetails userDetails = (ApplicationUserDetails) authentication.getPrincipal();
        Long ownerId = userDetails.personId();

        List<Restaurant> myRestaurants = restaurantService.getRestaurantsByOwner(ownerId);
        model.addAttribute("restaurants", myRestaurants);
        model.addAttribute("me", userDetails);

        return "ownerDashboard";
    }

    @GetMapping("/profile/edit")
    public String showEditProfileForm(Authentication authentication, Model model) {
        ApplicationUserDetails userDetails = (ApplicationUserDetails) authentication.getPrincipal();
        Long ownerId = userDetails.personId();

        Person person = personRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        String currentPrefix = "+30";
        String currentLocal = "";

        if (person.getMobilePhoneNumber() != null && !person.getMobilePhoneNumber().isBlank()) {
            try {
                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                Phonenumber.PhoneNumber number = phoneUtil.parse(person.getMobilePhoneNumber(), null);
                currentPrefix = "+" + number.getCountryCode();
                currentLocal = String.valueOf(number.getNationalNumber());
            } catch (NumberParseException e) {
                currentLocal = person.getMobilePhoneNumber();
            }
        }

        model.addAttribute("person", person);
        model.addAttribute("me", userDetails);
        model.addAttribute("countryCodes", getCountryCodes());
        model.addAttribute("currentCountryPrefix", currentPrefix);
        model.addAttribute("currentLocalPhoneNumber", currentLocal);

        return "ownerProfileEdit";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@ModelAttribute("person") Person formData,
                                @RequestParam(name = "countryPrefix", defaultValue = "+30") String countryPrefix,
                                @RequestParam(name = "localPhoneNumber") String localPhoneNumber,
                                Authentication authentication) {
        ApplicationUserDetails userDetails = (ApplicationUserDetails) authentication.getPrincipal();
        Long ownerId = userDetails.personId();

        String fullPhoneNumber = countryPrefix + localPhoneNumber.trim();

        UpdatePersonRequest updateRequest = new UpdatePersonRequest(
                formData.getFirstName(),
                formData.getLastName(),
                fullPhoneNumber,
                formData.getEmailAddress()
        );

        personService.updatePersonDetails(ownerId, updateRequest);

        return "redirect:/owner/dashboard";
    }

    @GetMapping("/restaurant/{id}/orders")
    public String viewRestaurantOrders(@PathVariable Long id,
                                       Authentication authentication,
                                       Model model) {
        ApplicationUserDetails userDetails = (ApplicationUserDetails) authentication.getPrincipal();

        Restaurant restaurant = restaurantService.getRestaurantIfAuthorized(id, userDetails.personId());

        List<CustomerOrder> orders = customerOrderRepository.findAllByRestaurantIdOrderByCreatedAtDesc(id);

        model.addAttribute("restaurant", restaurant);
        model.addAttribute("orders", orders);

        return "ownerDashboardOrders";
    }

    @PostMapping("/restaurant/{rid}/orders/{oid}/status")
    public String updateOrderStatus(@PathVariable Long rid,
                                    @PathVariable Long oid,
                                    @RequestParam("status") OrderStatus status) {

        customerOrderService.updateOrderStatus(oid, status);

        return "redirect:/owner/restaurant/" + rid + "/orders";
    }

    @GetMapping("/restaurant/new")
    public String showAddRestaurantForm(Model model) {
        Restaurant restaurant = new Restaurant();
        prepareOpenHours(restaurant);

        model.addAttribute("restaurant", restaurant);
        model.addAttribute("cuisines", Cuisine.values());
        model.addAttribute("serviceTypes", ServiceType.values());

        return "ownerRestaurantEdit";
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

        return "ownerRestaurantEdit";
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
    public String showAddMenuItemForm(@PathVariable Long restaurantId, Model model) {
        MenuItem menuItem = new MenuItem();
        model.addAttribute("menuItem", menuItem);
        model.addAttribute("restaurantId", restaurantId);
        model.addAttribute("itemTypes", ItemType.values());
        return "ownerMenuItemEdit";
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
        return "ownerMenuItemEdit";
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
            existing.setImageUrl(menuItem.getImageUrl());
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
package gr.hua.dit.project.web.ui;

import gr.hua.dit.project.core.model.Address;
import gr.hua.dit.project.core.model.Person;
import gr.hua.dit.project.core.repository.PersonRepository;
import gr.hua.dit.project.core.security.ApplicationUserDetails;
import gr.hua.dit.project.core.service.CustomerOrderService;
import gr.hua.dit.project.core.service.PersonService;
import gr.hua.dit.project.core.service.model.CustomerOrderView;
import gr.hua.dit.project.core.service.model.UpdatePersonRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/customer")
public class CustomerProfileController {

    private final CustomerOrderService customerOrderService;
    private final PersonRepository personRepository;
    private final PersonService personService;

    public CustomerProfileController(CustomerOrderService customerOrderService,
                                     PersonRepository personRepository,
                                     PersonService personService) {
        this.customerOrderService = customerOrderService;
        this.personRepository = personRepository;
        this.personService = personService;
    }

    @GetMapping("/profile")
    public String customerProfile(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());

        // order history
        List<CustomerOrderView> orders = customerOrderService.getMyOrders();
        model.addAttribute("orders", orders);

        return "customerProfile";
    }

    @GetMapping("/profile/edit")
    public String editProfilePage(Authentication authentication, Model model) {
        ApplicationUserDetails userDetails = (ApplicationUserDetails) authentication.getPrincipal();
        Person person = personRepository.findById(userDetails.personId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!model.containsAttribute("updatePersonRequest")) {
            model.addAttribute("updatePersonRequest", new UpdatePersonRequest(
                    person.getFirstName(),
                    person.getLastName(),
                    person.getMobilePhoneNumber(),
                    person.getEmailAddress()
            ));
        }
        model.addAttribute("person", person); // for the add address form
        if (!model.containsAttribute("newAddress")) {
            model.addAttribute("newAddress", new Address());
        }
        return "customerProfileEdit";
    }

    @PostMapping("/profile/update")
    public String updateProfile(Authentication authentication,
                                @Valid @ModelAttribute("updatePersonRequest") UpdatePersonRequest request,
                                BindingResult bindingResult,
                                Model model) {
        ApplicationUserDetails userDetails = (ApplicationUserDetails) authentication.getPrincipal();

        if (bindingResult.hasErrors()) {
            Person person = personRepository.findById(userDetails.personId()).orElseThrow();
            model.addAttribute("person", person);
            model.addAttribute("new Address", new Address());
            return "customerProfileEdit";
        }

        personService.updatePersonDetails(userDetails.personId(), request);

        return "redirect:/customer/profile/edit?success";
    }

    @PostMapping("/profile/address/add")
    public String addAddress(Authentication authentication,
                             @Valid @ModelAttribute("newAddress") Address address,
                             BindingResult bindingResult,
                             Model model) {
        ApplicationUserDetails userDetails = (ApplicationUserDetails) authentication.getPrincipal();

        if (bindingResult.hasErrors()) {
            Person person = personRepository.findById(userDetails.personId()).orElseThrow();
            model.addAttribute("person", person);
            model.addAttribute("updatePersonRequest", new UpdatePersonRequest(
                    person.getFirstName(),
                    person.getLastName(),
                    person.getMobilePhoneNumber(),
                    person.getEmailAddress()
            ));
            return "customerProfileEdit";
        }

        personService.addAddress(userDetails.personId(), address);
        return "redirect:/customer/profile/edit";

    }

    @GetMapping("/profile/address/delete")
    public String deleteAddress(Authentication authentication,
                                @RequestParam int index) {
        ApplicationUserDetails userDetails = (ApplicationUserDetails) authentication.getPrincipal();
        personService.removeAddress(userDetails.personId(), index);
        return "redirect:/customer/profile/edit";
    }
}

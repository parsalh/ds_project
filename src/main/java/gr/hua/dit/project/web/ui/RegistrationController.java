package gr.hua.dit.project.web.ui;

import gr.hua.dit.project.core.model.PersonType;
import gr.hua.dit.project.core.service.PersonService;
import gr.hua.dit.project.core.service.model.CreatePersonRequest;
import gr.hua.dit.project.core.service.model.CreatePersonResult;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * UI controller for managing customer/owner registration.
 */
@Controller
public class RegistrationController {

    private final PersonService personService;

    public RegistrationController(PersonService personService) {
        if (personService == null) throw new NullPointerException();
        this.personService = personService;
    }

    /**
     * Serves the registration form (HTML)
     */
    @GetMapping("/register")
    public String showRegistrationForm(final Authentication authentication, Model model){
        if (AuthController.isAuthenticated(authentication)) {
            return "redirect:/";
        }

        //Initial data for the form
        model.addAttribute("createPersonRequest", new CreatePersonRequest(PersonType.CUSTOMER,"","","","","","",""));

        return "register"; // the name of the thymeleaf/HTML template
    }

    /**
     * Handles the registration form submission (POST HTTP request)
     */
    @PostMapping("/register")
    public String handleRegistrationFormSubmission(
            final Authentication authentication,
            @ModelAttribute("createPersonRequest") CreatePersonRequest createPersonRequest,
            final Model model
    ) {

        if (AuthController.isAuthenticated(authentication)){
            return "redirect:/"; // already logged in
        }

        //TODO Form validation + UI errors.

        final CreatePersonResult createPersonResult = this.personService.createPerson(createPersonRequest);
        if (createPersonResult.created()) {
            return "redirect:/login";
        }
        model.addAttribute("createPersonRequest", createPersonRequest); // Pass the same form data.
        model.addAttribute("errorMessage",createPersonResult.reason()); // Show an error message!
        return "register";
    }
}

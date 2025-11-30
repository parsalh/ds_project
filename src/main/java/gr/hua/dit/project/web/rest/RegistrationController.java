package gr.hua.dit.project.web.rest;

import gr.hua.dit.project.core.model.Person;
import gr.hua.dit.project.core.model.PersonType;
import gr.hua.dit.project.core.repository.PersonRepository;
import gr.hua.dit.project.core.service.PersonService;
import gr.hua.dit.project.core.service.model.CreatePersonRequest;
import gr.hua.dit.project.core.service.model.CreatePersonResult;
import gr.hua.dit.project.core.service.model.PersonView;
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
    public String showRegistrationForm(final Model model){
        // TODO if user is authenticated,redirect to tickets
        //Initial data for the form
        model.addAttribute("createPersonRequest", new CreatePersonRequest(PersonType.CUSTOMER,"","","","","","",""));

        return "register"; // the name of the thymeleaf/HTML template
    }

    /**
     * Handles the registration form submission (POST HTTP request)
     */
    @PostMapping("/register")
    public String handleRegistrationFormSubmission(
            @ModelAttribute("createPersonRequest") CreatePersonRequest createPersonRequest,
            final Model model
    ) {
        // TODO if user is authenticated, redirect to tickets
        // TODO validate form (email format, size, blank, etc)
        // TODO if form has errors, show the form (with pre-filled data)
        // TODO otherwise, persist person, then, redirect to login

        final CreatePersonResult createPersonResult = this.personService.createPerson(createPersonRequest);
        if (createPersonResult.created()) {
            return "redirect:/login";
        }
        model.addAttribute("createPersonRequest", createPersonRequest); // Pass the same form data.
        model.addAttribute("errorMessage",createPersonResult.reason()); // Show an error message!
        return "register";
    }
}

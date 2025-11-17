package gr.hua.dit.project.web.rest;

import gr.hua.dit.project.core.model.Person;
import gr.hua.dit.project.core.model.PersonType;
import gr.hua.dit.project.core.repository.PersonRepository;
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

    private final PersonRepository personRepository;

    public RegistrationController(PersonRepository personRepository) {
        if (personRepository == null) throw new NullPointerException();
        this.personRepository = personRepository;
    }

    /**
     * Serves the registration form (HTML)
     */
    @GetMapping("/register")
    public String showRegistrationForm(final Model model){
        // TODO if user is authenticated,redirect to tickets
        //Initial data for the form
        model.addAttribute("person", new Person(null,"","","","","","",PersonType.CUSTOMER,"",null));

        return "register"; // the name of the thymeleaf/HTML template
    }

    /**
     * Handles the registration form submission (POST HTTP request)
     */
    @PostMapping("/register")
    public String handleRegistrationFormSubmission(
            @ModelAttribute("person") Person person,
            final Model model
    ) {
        // TODO if user is authenticated, redirect to tickets
        // TODO validate form (email format, size, blank, etc)
        // TODO if form has errors, show the form (with pre-filled data)
        // TODO otherwise, persist person, then, redirect to login
        System.out.println(person.toString()); //pre-save
        person = this.personRepository.save(person);
        System.out.println(person.toString()); //post-save (we expect a non-null ID)
        model.addAttribute("person",person);
        return "redirect:/login"; // registration successful - redirect to login form (TODO: implement login form)
    }

}

package gr.hua.dit.project.web.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * UI controller for managing profile.
 */
@Controller
public class CustomerProfileController {

    @GetMapping("/customerProfile")
    public String showProfile() {
        return "customerProfile";
    }
}


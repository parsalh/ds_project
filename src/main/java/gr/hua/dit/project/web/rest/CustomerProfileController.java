package gr.hua.dit.project.web.rest;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * UI controller for managing profile.
 */
@Controller
@RequestMapping("/customer")
public class CustomerProfileController {

    @GetMapping("/profile")
    public String customerProfile(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        return "customerProfile";
    }
}


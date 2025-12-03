package gr.hua.dit.project.web.rest;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OwnerProfileController {

    @GetMapping("/owner/profile")
    public String ownerProfile(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        return "ownerProfile";
    }
}
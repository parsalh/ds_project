package gr.hua.dit.project.web.rest;


import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * UI controller for user authentication (login and logout).
 */
@Controller
public class AuthController {

    @GetMapping("/login")
    public String login() {
        return "login"; // thymeleaf template name: login.html
    }

    @GetMapping("/logout-success")
    public String logout() {
        return "redirect:/login?logout";
    }
}

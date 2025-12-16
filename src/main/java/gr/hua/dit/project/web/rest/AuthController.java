package gr.hua.dit.project.web.rest;


import gr.hua.dit.project.core.model.Person;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * UI controller for user authentication (login and logout).
 */
@Controller
public class AuthController {

    @GetMapping("/login")
    public String login(
            final Authentication authentication,
            final HttpServletRequest request,
            final Model model
    ) {
        if (isAuthenticated(authentication)) {
            boolean isOwner = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

            if (isOwner) {
                return "redirect:/owner/dashboard";
            } else {
                return "redirect:/";
            }
        }

        if (request.getParameter("error") != null){
            model.addAttribute("error","Invalid username or password");
        }
        if (request.getParameter("logout") != null){
            model.addAttribute("message","You have been logged out");
        }
        return "login"; // thymeleaf template name: login.html
    }

    @GetMapping("/logout")
    public String logout(final Authentication authentication) {
        if (isAnonymous(authentication)) {
            return "redirect:/login";
        }
        return "logout";
    }

    public static boolean isAuthenticated(final Authentication auth) {
        return auth != null
                && (auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken));

    }

    public static boolean isAnonymous(final Authentication auth) {
        return auth == null
                || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken;
    }

}

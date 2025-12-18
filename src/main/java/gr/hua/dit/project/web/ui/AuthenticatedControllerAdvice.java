package gr.hua.dit.project.web.ui;

import gr.hua.dit.project.core.security.CurrentUser;
import gr.hua.dit.project.core.security.CurrentUserProvider;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Provides specific controllers {@link org.springframework.ui.Model} with the current user.
 */
@ControllerAdvice(basePackageClasses = {CustomerProfileController.class, OwnerDashboardController.class})
public class AuthenticatedControllerAdvice {

    private final CurrentUserProvider currentUserProvider;

    public AuthenticatedControllerAdvice(CurrentUserProvider currentUserProvider) {
        if  (currentUserProvider == null) throw new NullPointerException();
        this.currentUserProvider = currentUserProvider;
    }

    @ModelAttribute("me")
    CurrentUser addCurrentUserAsMe(){
        return this.currentUserProvider.getCurrentUser().orElse(null);
    }


}

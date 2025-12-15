package gr.hua.dit.project.core.security;

import gr.hua.dit.project.core.model.PersonType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


import java.util.Optional;

@Component
public final class CurrentUserProvider {
    public Optional<CurrentUser> getCurrentUser(){
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof ApplicationUserDetails userDetails){
            return Optional.of(new CurrentUser(userDetails.personId(), userDetails.getUsername(), userDetails.type()));
        }
        return Optional.empty();
    }

    public CurrentUser requireCurrentUser() {
        return this.getCurrentUser().orElseThrow(() -> new SecurityException("not authenticated"));
    }

    public long requiredStudentId() {
        final var currentUser = this.requireCurrentUser();
        if (currentUser.type() != PersonType.CUSTOMER) throw new SecurityException("Customer type required");
        return currentUser.id();
    }
}

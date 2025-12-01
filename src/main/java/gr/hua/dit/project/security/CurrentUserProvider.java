package gr.hua.dit.project.security;

import gr.hua.dit.project.core.model.PersonType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CurrentUserProvider {

    public Optional<CurrentUser> getCurrentUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof ApplicationUserDetails userDetails) {
            return Optional.of(new CurrentUser(userDetails.personId(), userDetails.getUsername(), userDetails.type()));
        }
        return Optional.empty();
    }

    public CurrentUser requireCurrentUser() {
        return this.getCurrentUser().orElseThrow(() -> new SecurityException("not authenticated"));
    }

    public long requiredOwnerId() {
        final var currentUser = this.requireCurrentUser();
        if (currentUser.type() != PersonType.OWNER) throw new SecurityException("Owner type/role required");
        return currentUser.id();
    }

    public long requiredCustomerId() {
        final var currentUser = this.requireCurrentUser();
        if (currentUser.type() != PersonType.CUSTOMER) throw new SecurityException("Customer type/role required");
        return currentUser.id();
    }
}

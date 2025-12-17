package gr.hua.dit.project.core.security;

import gr.hua.dit.project.core.model.PersonType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Immutable view implementing Spring's {@link UserDetails} for representing a user in runtime.
 */

public final class  ApplicationUserDetails implements UserDetails {

    private final long id;
    private final String username;
    private final String passwordHash;
    private final PersonType type;
    private final String email;

    public ApplicationUserDetails(final long id,
                                  final String username,
                                  final String passwordHash,
                                  final PersonType type,final String email)
    {
        if (id <= 0) throw new IllegalArgumentException();
        if (username == null) throw new NullPointerException();
        if (username.isBlank()) throw new IllegalArgumentException();
        if (passwordHash == null) throw new NullPointerException();
        if (passwordHash.isBlank()) throw new IllegalArgumentException();
        if (type == null) throw new NullPointerException();
        if (email == null) throw new NullPointerException();
        if (email.isBlank()) throw new IllegalArgumentException();

        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.type = type;
        this.email = email;
    }

    public long personId() {
        return this.id;
    }

    public PersonType type() {
        return this.type;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        final String role;
        if (this.type == PersonType.OWNER) role = "OWNER";
        else if (this.type == PersonType.CUSTOMER) role = "CUSTOMER";
        else throw new RuntimeException("Invalid type: " + this.type);
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.type.name()));
    }


    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    public String getEmail() { return this.email; }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}

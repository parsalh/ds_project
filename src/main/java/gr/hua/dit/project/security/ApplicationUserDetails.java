package gr.hua.dit.project.security;

import gr.hua.dit.project.core.model.PersonType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.yaml.snakeyaml.events.Event;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class ApplicationUserDetails implements UserDetails {

    private final long id;
    private final String username;
    private final String passwordHash;
    private final PersonType type;

    public ApplicationUserDetails(final long id,
                                  final String username,
                                  final String passwordHash,
                                  final PersonType type) {
        if (id <= 0) throw new IllegalArgumentException();
        if (username == null) throw new NullPointerException();
        if (username.isBlank()) throw new IllegalArgumentException();
        if (passwordHash == null) throw new NullPointerException();
        if (passwordHash.isBlank()) throw new IllegalArgumentException();
        if (type == null) throw new NullPointerException();

        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.type = type;
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
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }


    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

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

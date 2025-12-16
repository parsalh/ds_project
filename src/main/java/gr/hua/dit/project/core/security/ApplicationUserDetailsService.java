package gr.hua.dit.project.core.security;

import gr.hua.dit.project.core.model.Person;
import gr.hua.dit.project.core.repository.PersonRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

/**
 * Implementation of Spring's {@link UserDetailsService} for providing application users.
 */
@Service
public class ApplicationUserDetailsService implements UserDetailsService {

    private final PersonRepository personRepository;

    public ApplicationUserDetailsService(PersonRepository personRepository) {
        if (personRepository == null) throw new NullPointerException();
        this.personRepository = personRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        if (username == null) throw new NullPointerException();
        if (username.isBlank()) throw new IllegalArgumentException();

        final Person person = this.personRepository
                .findByUsernameIgnoreCase(username)
                .orElse(null);
        if (person == null) throw new UsernameNotFoundException("Person with username" + username + " does not exist");

        return new ApplicationUserDetails(
                person.getId(),
                person.getUsername(),
                person.getPasswordHash(),
                person.getType()
        );
    }
}

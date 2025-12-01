package gr.hua.dit.project.security;

import gr.hua.dit.project.core.model.Person;
import gr.hua.dit.project.core.repository.PersonRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class ApplicationUserDetailsService implements UserDetailsService {

    private final PersonRepository personRepository;
    public ApplicationUserDetailsService(final PersonRepository personRepository) {
        if (personRepository == null) throw new NullPointerException();
        this.personRepository = personRepository;
    }

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        if (username == null) throw new NullPointerException();
        if (username.isBlank()) throw new IllegalArgumentException();
        final Person person = this.personRepository
                .findByUsername(username)
                .orElse(null);
        if (person == null) {
            throw new UsernameNotFoundException("person with username" + username + " does not exist");
        }
        return new ApplicationUserDetails(
                person.getId(),
                person.getUsername(),
                person.getPasswordHash(),
                person.getType()
        );
    }
}

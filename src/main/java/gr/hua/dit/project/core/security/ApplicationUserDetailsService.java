package gr.hua.dit.project.core.security;

import gr.hua.dit.project.core.model.Person;
import gr.hua.dit.project.core.repository.PersonRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class ApplicationUserDetailsService implements UserDetailsService {

    private final PersonRepository personRepository;

    public ApplicationUserDetailsService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Person person = personRepository
                .findByUsernameOrEmailAddress(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new ApplicationUserDetails(
                person.getId(),
                person.getUsername(),
                person.getPasswordHash(),
                person.getType()
        );
    }
}

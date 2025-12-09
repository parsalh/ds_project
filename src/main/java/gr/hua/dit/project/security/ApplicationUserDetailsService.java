package gr.hua.dit.project.security;

import gr.hua.dit.project.core.model.Person;
import gr.hua.dit.project.core.repository.PersonRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import gr.hua.dit.project.security.ApplicationUserDetails;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationUserDetailsService implements UserDetailsService {

    private final PersonRepository personRepository;

    public ApplicationUserDetailsService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Person person = personRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new ApplicationUserDetails(
                person.getId(),
                person.getUsername(),
                person.getPasswordHash(),
                person.getType()
//                List.of(new SimpleGrantedAuthority("ROLE_" + person.getType().name()))
        );
    }
}

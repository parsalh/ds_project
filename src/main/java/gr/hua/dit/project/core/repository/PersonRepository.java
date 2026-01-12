package gr.hua.dit.project.core.repository;

import gr.hua.dit.project.core.model.Person;
import gr.hua.dit.project.core.model.PersonType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 *  Repository for managing {@link Person} entity.
 */
@Repository
public interface PersonRepository extends JpaRepository<Person,Long> {

    Optional<Person> findByUsernameIgnoreCase(final String username);

    Optional<Person> findByEmailAddressIgnoreCase(final String emailAddress);

    List<Person> findAllByTypeOrderByLastName(final PersonType type);

    Optional<Person> findByUsernameIgnoreCaseOrEmailAddressIgnoreCase(String username, String emailAddress);

    boolean existsByUsernameIgnoreCase(final String username);

    boolean existsByEmailAddressIgnoreCase(final String emailAddress);

    boolean existsByMobilePhoneNumber(final String mobileNumber);


}

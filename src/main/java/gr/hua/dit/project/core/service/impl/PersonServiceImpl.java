package gr.hua.dit.project.core.service.impl;

import gr.hua.dit.project.core.model.Person;
import gr.hua.dit.project.core.model.PersonType;
import gr.hua.dit.project.core.repository.PersonRepository;
import gr.hua.dit.project.core.service.PersonService;
import gr.hua.dit.project.core.service.model.CreatePersonRequest;
import gr.hua.dit.project.core.service.model.PersonView;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 *  Default implementation of {@link PersonService}.
 */
@Service
public final class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;

    public PersonServiceImpl(final PersonRepository personRepository) {
        if (personRepository == null) throw new NullPointerException();
        this.personRepository = personRepository;
    }

    @Override
    public List<PersonView> getPeople() {
        return List.of(); // TODO Implement.
    }

    @Override
    public PersonView createPerson(final CreatePersonRequest createPersonRequest) {
        if (createPersonRequest == null) throw new NullPointerException();

        // Unpack (we assume validated `CreatePersonRequest`)
        // --------------------------------------------------

        final PersonType type = createPersonRequest.type();
        final String username = createPersonRequest.username().strip(); // remove whitespaces
        final String firstName = createPersonRequest.firstName().strip();
        final String lastName = createPersonRequest.lastName().strip();
        final String emailAddress = createPersonRequest.emailAddress().strip();
        final String mobilePhoneNumber = createPersonRequest.mobilePhoneNumber().strip();
        final String address = createPersonRequest.address().strip();
        final String rawPassword = createPersonRequest.rawPassword();

        // --------------------------------------------------

        // TODO username must be unique
        // TODO emailAddress must be unique
        // TODO mobilePhoneNumber must be unique

        // --------------------------------------------------

        // TODO use external service to ... ?
        // TODO find out what we need it for...????

        // --------------------------------------------------

        // TODO encode password! raw to hash!
        final String hashedPassword = rawPassword; // TODO BUG! Encode.

        // Instantiate person.
        // --------------------------------------------------

        Person person = new Person();
        person.setId(null); //auto-generated
        person.setUsername(username);
        person.setType(type);
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setEmailAddress(emailAddress);
        person.setMobilePhoneNumber(mobilePhoneNumber);
        person.setAddress(address);
        person.setPasswordHash(hashedPassword);
        person.setCreatedAt(null); //auto-generated

        // Persist person (save/insert to database)
        // --------------------------------------------------

        person = this.personRepository.save(person);

        // Map `Person` to `PersonView`
        // --------------------------------------------------

        final PersonView personView = null; // TODO Implement.

        // --------------------------------------------------

        return personView;
    }

}

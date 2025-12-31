package gr.hua.dit.project.core.service.impl;

import gr.hua.dit.project.core.model.Person;
import gr.hua.dit.project.core.model.PersonType;
import gr.hua.dit.project.core.port.SmsNotificationPort;
import gr.hua.dit.project.core.repository.PersonRepository;
import gr.hua.dit.project.core.service.PersonService;
import gr.hua.dit.project.core.service.mapper.PersonMapper;
import gr.hua.dit.project.core.service.model.CreatePersonRequest;
import gr.hua.dit.project.core.service.model.CreatePersonResult;
import gr.hua.dit.project.core.service.model.PersonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;



/**
 *  Default implementation of {@link PersonService}.
 */
@Service
public final class PersonServiceImpl implements PersonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonServiceImpl.class);
    private final SmsNotificationPort smsNotificationPort;
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private final PasswordEncoder passwordEncoder;



    public PersonServiceImpl(final SmsNotificationPort smsNotificationPort,
                             final PersonRepository personRepository,
                             final PersonMapper personMapper,
                             final PasswordEncoder passwordEncoder) {
        if (smsNotificationPort == null) throw new NullPointerException();
        if (personRepository == null) throw new NullPointerException();
        if (personMapper == null) throw new NullPointerException();


        this.smsNotificationPort = smsNotificationPort;
        this.personRepository = personRepository;
        this.personMapper = personMapper;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public CreatePersonResult createPerson(final CreatePersonRequest createPersonRequest) {
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

        final String hashedPassword = passwordEncoder.encode(rawPassword);

        // --------------------------------------------------

        if(this.personRepository.existsByUsernameIgnoreCase(username)){
            return CreatePersonResult.fail("Username is already in use");
        }

        if (this.personRepository.existsByEmailAddressIgnoreCase(emailAddress)){
            return CreatePersonResult.fail("E-mail address is already in use");
        }

        if (this.personRepository.existsByMobilePhoneNumber(mobilePhoneNumber)){
            return CreatePersonResult.fail("Mobile phone number is already in use");
        }

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

        final String content = String.format(
                "You have successfully registered for StreetFoodGo application! " +
                "Use your e-mail (%s) or username (%s) to log in.",emailAddress,username);
        final boolean sent = this.smsNotificationPort.sendSms(mobilePhoneNumber, content);
        if (!sent) {
            LOGGER.warn("SMS sent to {} failed!", mobilePhoneNumber);
        }


        person = this.personRepository.save(person);

        // Map `Person` to `PersonView`
        // --------------------------------------------------

        final PersonView personView = this.personMapper.convertPersonToPersonView(person);

        // --------------------------------------------------

        return CreatePersonResult.success(personView);
    }

}

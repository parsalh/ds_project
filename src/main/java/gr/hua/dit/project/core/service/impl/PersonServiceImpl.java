package gr.hua.dit.project.core.service.impl;

import gr.hua.dit.project.core.model.Address;
import gr.hua.dit.project.core.model.Person;
import gr.hua.dit.project.core.model.PersonType;
import gr.hua.dit.project.core.port.GeocodingService;
import gr.hua.dit.project.core.port.SmsNotificationPort;
import gr.hua.dit.project.core.repository.PersonRepository;
import gr.hua.dit.project.core.service.PersonService;
import gr.hua.dit.project.core.service.mapper.PersonMapper;
import gr.hua.dit.project.core.service.model.CreatePersonRequest;
import gr.hua.dit.project.core.service.model.CreatePersonResult;
import gr.hua.dit.project.core.service.model.PersonView;
import gr.hua.dit.project.core.service.model.UpdatePersonRequest;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersonServiceImpl implements PersonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonServiceImpl.class);
    private final SmsNotificationPort smsNotificationPort;
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private final PasswordEncoder passwordEncoder;
    private final GeocodingService geocodingService;

    public PersonServiceImpl(final SmsNotificationPort smsNotificationPort,
                             final PersonRepository personRepository,
                             final PersonMapper personMapper,
                             final PasswordEncoder passwordEncoder,
                             final GeocodingService geocodingService) {
        if (smsNotificationPort == null) throw new NullPointerException();
        if (personRepository == null) throw new NullPointerException();
        if (personMapper == null) throw new NullPointerException();
        if (passwordEncoder == null) throw new NullPointerException();
        if (geocodingService == null) throw new NullPointerException();
        
        this.smsNotificationPort = smsNotificationPort;
        this.personRepository = personRepository;
        this.personMapper = personMapper;
        this.passwordEncoder = passwordEncoder;
        this.geocodingService = geocodingService;
    }

    @Override
    public CreatePersonResult createPerson(final CreatePersonRequest createPersonRequest) {
        // [Existing implementation remains unchanged]
        if (createPersonRequest == null) throw new NullPointerException();

        final PersonType type = createPersonRequest.type();
        final String username = createPersonRequest.username().strip();
        final String firstName = createPersonRequest.firstName().strip();
        final String lastName = createPersonRequest.lastName().strip();
        final String emailAddress = createPersonRequest.emailAddress().strip();
        final String mobilePhoneNumber = createPersonRequest.mobilePhoneNumber().strip();
        final String street = createPersonRequest.street().strip();
        final String number = createPersonRequest.addressNumber().strip();
        final String zip = createPersonRequest.zipCode().strip();
        final String rawPassword = createPersonRequest.rawPassword();

        final String hashedPassword = passwordEncoder.encode(rawPassword);

        if(this.personRepository.existsByUsernameIgnoreCase(username)){
            return CreatePersonResult.fail("Username is already in use");
        }
        if (this.personRepository.existsByEmailAddressIgnoreCase(emailAddress)){
            return CreatePersonResult.fail("E-mail address is already in use");
        }
        if (this.personRepository.existsByMobilePhoneNumber(mobilePhoneNumber)){
            return CreatePersonResult.fail("Mobile phone number is already in use");
        }

        Person person = new Person();
        person.setUsername(username);
        person.setType(type);
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setEmailAddress(emailAddress);
        person.setMobilePhoneNumber(mobilePhoneNumber);

        Address newAddress = new Address();
        newAddress.setStreet(street);
        newAddress.setNumber(number);
        newAddress.setZipCode(zip);

        String fullAddressStr = street + " " + (number.isBlank() ? "" : number) + ", " + zip;
        this.geocodingService.getCoordinates(fullAddressStr).ifPresent(coords -> {
            newAddress.setLatitude(coords[0]);
            newAddress.setLongitude(coords[1]);
        });

        person.getAddresses().add(newAddress);

        person.setPasswordHash(hashedPassword);

        final String content = String.format(
                "You have successfully registered for StreetFoodGo application! " +
                        "Use your e-mail (%s) or username (%s) to log in.",emailAddress,username);
        final boolean sent = this.smsNotificationPort.sendSms(mobilePhoneNumber, content);
        if (!sent) {
            LOGGER.warn("SMS sent to {} failed!", mobilePhoneNumber);
        }

        person = this.personRepository.save(person);
        final PersonView personView = this.personMapper.convertPersonToPersonView(person);
        return CreatePersonResult.success(personView);
    }

    @Override
    @Transactional
    public void updatePersonDetails(Long personId, UpdatePersonRequest request) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new EntityNotFoundException("Person not found"));

        if (request.firstName() != null && !request.firstName().isBlank()) {
            person.setFirstName(request.firstName().strip());
        }
        if (request.lastName() != null && !request.lastName().isBlank()) {
            person.setLastName(request.lastName().strip());
        }
        if (request.emailAddress() != null && !request.emailAddress().isBlank()) {
            // Check uniqueness if email changed
            if (!person.getEmailAddress().equalsIgnoreCase(request.emailAddress()) &&
                    personRepository.existsByEmailAddressIgnoreCase(request.emailAddress())) {
                throw new IllegalArgumentException("Email already in use");
            }
            person.setEmailAddress(request.emailAddress().strip());
        }
        if (request.mobilePhoneNumber() != null && !request.mobilePhoneNumber().isBlank()) {
            person.setMobilePhoneNumber(request.mobilePhoneNumber().strip());
        }

        personRepository.save(person);
    }

    @Override
    @Transactional
    public void addAddress(Long personId, Address address) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new EntityNotFoundException("Person not found"));
        person.getAddresses().add(address);
        personRepository.save(person);
    }

    @Override
    @Transactional
    public void removeAddress(Long personId, int addressIndex) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new EntityNotFoundException("Person not found"));

        if (addressIndex >= 0 && addressIndex < person.getAddresses().size()) {
            person.getAddresses().remove(addressIndex);
            personRepository.save(person);
        }
    }
}

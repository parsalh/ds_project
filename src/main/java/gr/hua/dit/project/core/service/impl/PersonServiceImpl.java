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
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
public class PersonServiceImpl implements PersonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonServiceImpl.class);

    private final Validator validator;
    private final SmsNotificationPort smsNotificationPort;
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private final PasswordEncoder passwordEncoder;
    private final GeocodingService geocodingService;

    public PersonServiceImpl(final Validator validator,
                             final SmsNotificationPort smsNotificationPort,
                             final PersonRepository personRepository,
                             final PersonMapper personMapper,
                             final PasswordEncoder passwordEncoder,
                             final GeocodingService geocodingService) {
        this.validator = validator;
        this.smsNotificationPort = smsNotificationPort;
        this.personRepository = personRepository;
        this.personMapper = personMapper;
        this.passwordEncoder = passwordEncoder;
        this.geocodingService = geocodingService;
    }

    @Override
    @Transactional
    public CreatePersonResult createPerson(final CreatePersonRequest createPersonRequest) {
        if (createPersonRequest == null) throw new NullPointerException();

        final Set<ConstraintViolation<CreatePersonRequest>> requestViolations =
                this.validator.validate(createPersonRequest);
        if (!requestViolations.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            for (final ConstraintViolation<CreatePersonRequest> violation : requestViolations) {
                sb.append(violation.getPropertyPath()).append(" : ").append(violation.getMessage()).append("\n");
            }
            return CreatePersonResult.fail(sb.toString());
        }

        final PersonType type = createPersonRequest.type();
        final String username = createPersonRequest.username().strip();
        final String firstName = createPersonRequest.firstName().strip();
        final String lastName = createPersonRequest.lastName().strip();
        final String emailAddress = createPersonRequest.emailAddress().strip();
        final String mobilePhoneNumber = (createPersonRequest.countryPrefix() + createPersonRequest.localPhoneNumber()).strip();

        String street = null;
        String number = null;
        String zip = null;

        if (createPersonRequest.street() != null && !createPersonRequest.street().isBlank()) {
            street = createPersonRequest.street().strip();
            number = (createPersonRequest.addressNumber() != null) ? createPersonRequest.addressNumber().strip() : "";
            zip = (createPersonRequest.zipCode() != null) ? createPersonRequest.zipCode().strip() : "";
        }

        final String rawPassword = createPersonRequest.rawPassword();
        final String hashedPassword = passwordEncoder.encode(rawPassword);

        if(this.personRepository.existsByUsernameIgnoreCase(username)) return CreatePersonResult.fail("Username is already in use");
        if (this.personRepository.existsByEmailAddressIgnoreCase(emailAddress)) return CreatePersonResult.fail("E-mail address is already in use");
        if (this.personRepository.existsByMobilePhoneNumber(mobilePhoneNumber)) return CreatePersonResult.fail("Mobile phone number is already in use");

        Person person = new Person();
        person.setUsername(username);
        person.setType(type);
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setEmailAddress(emailAddress);
        person.setMobilePhoneNumber(mobilePhoneNumber);
        person.setPasswordHash(hashedPassword);

        if (street != null) {
            Address newAddress = new Address();
            newAddress.setStreet(street);
            newAddress.setNumber(number);
            newAddress.setZipCode(zip);

            newAddress.setLatitude(createPersonRequest.latitude());
            newAddress.setLongitude(createPersonRequest.longitude());

            if (newAddress.getLatitude() == null || newAddress.getLongitude() == null) {
                enrichAddressWithCoordinates(newAddress);
            }
            if (newAddress.getLatitude() == null || newAddress.getLongitude() == null) {
                throw new IllegalArgumentException("The address could not be located on the map. Please check the details.");
            }

            person.getAddresses().add(newAddress);
        }

        final String content = String.format("You have successfully registered for StreetFoodGo application! Use your e-mail (%s) or username (%s) to log in.", emailAddress, username);
        try {
            this.smsNotificationPort.sendSms(mobilePhoneNumber, content);
        } catch (Exception e) {
            LOGGER.error("Error sending SMS", e);
        }

        final Set<ConstraintViolation<Person>> personViolations = this.validator.validate(person);
        if (!personViolations.isEmpty()) {
            throw new RuntimeException("Invalid Person instance");
        }

        person = this.personRepository.save(person);
        final PersonView personView = this.personMapper.convertPersonToPersonView(person);
        return CreatePersonResult.success(personView);
    }

    @Override
    @Transactional
    public void updatePersonDetails(Long personId, UpdatePersonRequest request) {
        Person person = personRepository.findById(personId).orElseThrow(() -> new EntityNotFoundException("Person not found"));
        if (request.firstName() != null && !request.firstName().isBlank()) person.setFirstName(request.firstName().strip());
        if (request.lastName() != null && !request.lastName().isBlank()) person.setLastName(request.lastName().strip());
        if (request.emailAddress() != null && !request.emailAddress().isBlank()) {
            if (!person.getEmailAddress().equalsIgnoreCase(request.emailAddress()) && personRepository.existsByEmailAddressIgnoreCase(request.emailAddress())) {
                throw new IllegalArgumentException("Email already in use");
            }
            person.setEmailAddress(request.emailAddress().strip());
        }
        if (request.mobilePhoneNumber() != null && !request.mobilePhoneNumber().isBlank()) person.setMobilePhoneNumber(request.mobilePhoneNumber().strip());
        personRepository.save(person);
    }

    @Override
    @Transactional
    public void addAddress(Long personId, Address address) {
        Person person = personRepository.findById(personId).orElseThrow(() -> new EntityNotFoundException("Person not found"));
        if (address.getLatitude() == null || address.getLongitude() == null) {
            enrichAddressWithCoordinates(address);
        }
        if (address.getLatitude() == null || address.getLongitude() == null) {
            throw new IllegalArgumentException("The address could not be located on the map.");
        }
        person.getAddresses().add(address);
        personRepository.save(person);
    }

    @Override
    @Transactional
    public void removeAddress(Long personId, int addressIndex) {
        Person person = personRepository.findById(personId).orElseThrow(() -> new EntityNotFoundException("Person not found"));
        if (addressIndex >= 0 && addressIndex < person.getAddresses().size()) {
            person.getAddresses().remove(addressIndex);
            personRepository.save(person);
        }
    }

    private void enrichAddressWithCoordinates(Address address) {
        try {
            String query = address.getStreet() + (address.getNumber() != null ? " " + address.getNumber() : "") + (address.getZipCode() != null ? ", " + address.getZipCode() : "");
            Optional<double[]> coordinates = geocodingService.getCoordinates(query);
            if (coordinates.isPresent()) {
                address.setLatitude(coordinates.get()[0]);
                address.setLongitude(coordinates.get()[1]);
                LOGGER.info("Geocoded address [{}]: Lat={}, Lon={}", query, coordinates.get()[0], coordinates.get()[1]);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to geocode address: " + address, e);
        }
    }
}
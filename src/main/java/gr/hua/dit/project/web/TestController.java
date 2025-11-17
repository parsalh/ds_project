package gr.hua.dit.project.web;

import gr.hua.dit.project.core.model.Person;
import gr.hua.dit.project.core.model.PersonType;
import gr.hua.dit.project.core.repository.PersonRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;
import java.time.Instant;

@RestController
public class TestController {

    private final PersonRepository personRepository;

    public TestController(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    /**
     * REST endpoint for testing.
     */
    @GetMapping(value = "test", produces = MediaType.TEXT_PLAIN_VALUE)
    public String test(){
        //Example 1: create Person
        Person person = new Person();
        person.setId(null); //auto-gen
        person.setUsername("example_username");
        person.setType(PersonType.CUSTOMER);
        person.setFirstName("Stavroula");
        person.setLastName("Parsali");
        person.setEmailAddress("example@gmail.com");
        person.setMobilePhoneNumber("+306900000000");
        person.setAddress("Omirou 9");
        person.setPasswordHash("<invalid>");
        person.setCreatedAt(Instant.now());

        person = this.personRepository.save(person);

        return person.toString();
    }

}

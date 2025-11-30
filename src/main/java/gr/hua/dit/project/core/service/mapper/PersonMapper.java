package gr.hua.dit.project.core.service.mapper;

import gr.hua.dit.project.core.model.Person;
import gr.hua.dit.project.core.service.model.PersonView;

import org.springframework.stereotype.Component;

/**
 * Mapper to convert {@link Person} to {@link PersonView}
 */
@Component
public class PersonMapper {

    public PersonView convertPersonToPersonView(final Person person){
        if (person==null){
            return null;
        }
        final PersonView personView = new PersonView(
                person.getId(),
                person.getUsername(),
                person.getFirstName(),
                person.getLastName(),
                person.getMobilePhoneNumber(),
                person.getEmailAddress(),
                person.getAddress(),
                person.getType()
        );
        return personView;
    }

}

package gr.hua.dit.project.core.service;

import gr.hua.dit.project.core.model.Person;
import gr.hua.dit.project.core.service.model.CreatePersonRequest;
import gr.hua.dit.project.core.service.model.PersonView;

import java.util.List;

/**
 * Service (contract) for managing customers/owners.
 */
public interface PersonService {

    List<PersonView> getPeople();

    PersonView createPerson(CreatePersonRequest createPersonRequest);

}

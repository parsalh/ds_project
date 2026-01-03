package gr.hua.dit.project.core.service;

import gr.hua.dit.project.core.model.Address;
import gr.hua.dit.project.core.service.model.CreatePersonRequest;
import gr.hua.dit.project.core.service.model.CreatePersonResult;
import gr.hua.dit.project.core.service.model.UpdatePersonRequest; // Ensure this DTO exists

public interface PersonService {

    CreatePersonResult createPerson(final CreatePersonRequest createPersonRequest);

    void updatePersonDetails(Long personId, UpdatePersonRequest request);

    void addAddress(Long personId, Address address);

    void removeAddress(Long personId, int addressIndex);
}
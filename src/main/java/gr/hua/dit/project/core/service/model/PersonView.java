package gr.hua.dit.project.core.service.model;

import gr.hua.dit.project.core.model.PersonType;

/**
 * PersonView (DTO) that includes only information to be exposed.
 */
public record PersonView (
        Long id,
        String username,
        String firstName,
        String lastName,
        String mobilePhoneNumber,
        String emailAddress,
        String address,
        PersonType type
) {}

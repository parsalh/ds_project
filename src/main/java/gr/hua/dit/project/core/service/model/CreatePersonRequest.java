package gr.hua.dit.project.core.service.model;

import gr.hua.dit.project.core.model.PersonType;

public record CreatePersonRequest(
        PersonType type,
        String username,
        String firstName,
        String lastName,
        String emailAddress,
        String mobilePhoneNumber,
        String street,
        String addressNumber,
        String zipCode,
        String rawPassword,
        Double latitude,
        Double longitude
) {
}

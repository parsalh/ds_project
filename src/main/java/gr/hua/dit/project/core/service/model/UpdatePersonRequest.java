package gr.hua.dit.project.core.service.model;

public record UpdatePersonRequest(
        String firstName,
        String lastName,
        String mobilePhoneNumber,
        String emailAddress
) {}
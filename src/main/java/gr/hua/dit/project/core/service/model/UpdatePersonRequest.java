package gr.hua.dit.project.core.service.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatePersonRequest(

        @Size(min = 2, max = 100, message = "First name must be valid")
        String firstName,

        @Size(min = 2, max = 100, message = "Last name must be valid")
        String lastName,

        @Pattern(regexp = "^\\+?[0-9]{10,13}$", message = "Invalid phone number")
        String mobilePhoneNumber,

        @Email(message = "Invalid email format")
        String emailAddress
) {}
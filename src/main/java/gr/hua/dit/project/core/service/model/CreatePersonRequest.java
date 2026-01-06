package gr.hua.dit.project.core.service.model;

import gr.hua.dit.project.core.model.PersonType;
import jakarta.validation.constraints.*;

/**
 * DTO for requesting the creation (registration) of a Person.
 */
public record CreatePersonRequest(

        @NotNull(message = "Please select a role")
        PersonType type,

        @NotNull
        @NotBlank(message = "Username cannot be empty")
        @Size(min = 4, max = 50, message = "Username must be between 4 and 20 characters")
        String username,

        @NotNull
        @NotBlank(message = "First name is required")
        @Size(max = 100)
        String firstName,

        @NotNull
        @NotBlank(message = "Last name is required")
        @Size(max = 100)
        String lastName,

        @NotNull
        @NotBlank(message = "Email is required")
        @Size(max = 100)
        @Email(message = "Invalid email format")
        String emailAddress,

        @NotBlank(message = "Country code is required")
        String countryPrefix,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "\\d{10}", message = "Phone must be 10 digits")
        String localPhoneNumber,

        String street,
        String addressNumber,
        String zipCode,

        @NotNull
        @NotBlank(message = "Password is required")
        @Size(min = 4, max = 255, message = "Password must be at least 4 characters")
        String rawPassword,

        Double latitude,
        Double longitude

) {}

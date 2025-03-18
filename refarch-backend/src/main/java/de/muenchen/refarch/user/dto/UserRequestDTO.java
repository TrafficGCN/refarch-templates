package de.muenchen.refarch.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for creating or updating a user.
 */
public record UserRequestDTO(
        @NotBlank(message = "Username is required") String username,
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
        @NotBlank(message = "Password is required") String password,
        String firstName,
        String lastName,
        String title,
        String affiliation,
        String thumbnail) {
}

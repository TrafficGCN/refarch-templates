package de.muenchen.refarch.user.bio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO for creating or updating a user's biography.
 */
public record UserBioRequestDTO(
        @NotNull(message = "User ID is required") UUID userId,
        @NotNull(message = "Language ID is required") UUID languageId,
        @NotBlank(message = "Bio content is required") String bio) {
}

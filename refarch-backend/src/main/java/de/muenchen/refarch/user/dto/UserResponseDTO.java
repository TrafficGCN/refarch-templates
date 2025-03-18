package de.muenchen.refarch.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for returning user data.
 */
public record UserResponseDTO(
        UUID id,
        String username,
        String firstName,
        String lastName,
        String title,
        String affiliation,
        String thumbnail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}

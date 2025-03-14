package de.muenchen.refarch.user.bio.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for returning user biography data.
 */
public record UserBioResponseDTO(
        UUID id,
        UUID userId,
        UUID languageId,
        String bio,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}

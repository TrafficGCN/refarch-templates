package de.muenchen.refarch.role.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RoleResponseDTO(
        UUID id,
        String name,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}

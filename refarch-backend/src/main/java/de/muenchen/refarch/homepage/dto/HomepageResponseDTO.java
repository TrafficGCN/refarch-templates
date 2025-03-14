package de.muenchen.refarch.homepage.dto;

import de.muenchen.refarch.homepage.content.dto.HomepageContentResponseDTO;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record HomepageResponseDTO(
        UUID id,
        UUID linkId,
        String thumbnail,
        Set<HomepageContentResponseDTO> contents,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}

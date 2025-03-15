package de.muenchen.refarch.page.dto;

import de.muenchen.refarch.page.content.dto.PageContentResponseDTO;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record PageResponseDTO(
        UUID id,
        UUID linkId,
        String thumbnail,
        boolean commentsEnabled,
        boolean published,
        Set<PageContentResponseDTO> contents,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}

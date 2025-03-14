package de.muenchen.refarch.page.content.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PageContentResponseDTO(
        UUID id,
        UUID pageId,
        UUID languageId,
        String title,
        String content,
        String shortDescription,
        String keywords,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}

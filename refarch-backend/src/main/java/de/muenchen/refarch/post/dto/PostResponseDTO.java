package de.muenchen.refarch.post.dto;

import de.muenchen.refarch.link.Link;
import java.time.LocalDateTime;
import java.util.UUID;

public record PostResponseDTO(
        UUID id,
        Link link,
        String thumbnail,
        boolean commentsEnabled,
        boolean published,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}

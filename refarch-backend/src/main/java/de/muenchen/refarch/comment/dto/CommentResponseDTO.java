package de.muenchen.refarch.comment.dto;

import de.muenchen.refarch.user.dto.UserResponseDTO;
import java.time.LocalDateTime;
import java.util.UUID;

public record CommentResponseDTO(
        UUID id,
        String content,
        UUID postId,
        UUID pageId,
        UserResponseDTO user,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}

package de.muenchen.refarch.comment.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CommentRequestDTO(
        @NotBlank(message = "Comment content is required") String content) {
}

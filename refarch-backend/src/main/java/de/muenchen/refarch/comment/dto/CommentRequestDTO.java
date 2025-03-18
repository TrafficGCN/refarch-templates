package de.muenchen.refarch.comment.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentRequestDTO(
        @NotBlank(message = "Comment content is required") String content) {
}

package de.muenchen.refarch.post.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PostRequestDTO(
        @NotNull(message = "Link ID is required") UUID linkId,

        String thumbnail,

        @NotNull(message = "Comments enabled flag is required") Boolean commentsEnabled,

        @NotNull(message = "Published flag is required") Boolean published) {
}

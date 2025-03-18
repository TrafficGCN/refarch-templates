package de.muenchen.refarch.page.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PageRequestDTO(
        UUID linkId,
        String thumbnail,
        @NotNull(message = "commentsEnabled must not be null") Boolean commentsEnabled,
        @NotNull(message = "published must not be null") Boolean published) {
}

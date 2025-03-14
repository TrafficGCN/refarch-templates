package de.muenchen.refarch.page.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PageContentRequestDTO(
        @NotNull(message = "languageId must not be null") UUID languageId,

        @NotBlank(message = "title must not be blank") String title,

        @NotBlank(message = "content must not be blank") String content,

        String shortDescription,
        String keywords) {
}

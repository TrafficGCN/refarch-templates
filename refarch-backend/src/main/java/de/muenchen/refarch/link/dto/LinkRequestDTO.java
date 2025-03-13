package de.muenchen.refarch.link.dto;

import de.muenchen.refarch.link.LinkScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating a new link.
 */
public record LinkRequestDTO(
        @NotBlank String link,
        String name,
        String fontAwesomeIcon,
        String mdiIcon,
        String type,
        @NotNull LinkScope scope) {
}

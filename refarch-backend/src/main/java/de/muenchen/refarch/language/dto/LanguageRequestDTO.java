package de.muenchen.refarch.language.dto;

import jakarta.validation.constraints.NotBlank;

public record LanguageRequestDTO(
        @NotBlank(message = "Name is required") String name,

        @NotBlank(message = "Abbreviation is required") String abbreviation,

        @NotBlank(message = "Font Awesome icon is required") String fontAwesomeIcon,

        @NotBlank(message = "MDI icon is required") String mdiIcon) {
}

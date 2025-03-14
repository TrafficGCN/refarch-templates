package de.muenchen.refarch.homepage.dto;

import java.util.UUID;

public record HomepageRequestDTO(
        UUID linkId,
        String thumbnail) {
}

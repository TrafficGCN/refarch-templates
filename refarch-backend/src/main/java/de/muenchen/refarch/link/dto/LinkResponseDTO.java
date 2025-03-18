package de.muenchen.refarch.link.dto;

import de.muenchen.refarch.link.LinkScope;
import java.util.UUID;

/**
 * DTO for returning link data.
 */
public record LinkResponseDTO(
        UUID id,
        String link,
        String name,
        String fontAwesomeIcon,
        String mdiIcon,
        String type,
        LinkScope scope) {
}

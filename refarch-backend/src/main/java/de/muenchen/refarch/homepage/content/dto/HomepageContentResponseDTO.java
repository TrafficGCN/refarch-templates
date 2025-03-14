package de.muenchen.refarch.homepage.content.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record HomepageContentResponseDTO(
        UUID id,
        UUID homepageId,
        UUID languageId,
        String welcomeMessage,
        String welcomeMessageExtended,
        String exploreOurWork,
        String getInvolved,
        String importantLinks,
        String ecosystemLinks,
        String blog,
        String papers,
        String readMore,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}

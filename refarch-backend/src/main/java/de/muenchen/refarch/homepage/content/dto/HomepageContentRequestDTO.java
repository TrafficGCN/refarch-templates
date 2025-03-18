package de.muenchen.refarch.homepage.content.dto;

import java.util.UUID;

public record HomepageContentRequestDTO(
        UUID languageId,
        String welcomeMessage,
        String welcomeMessageExtended,
        String exploreOurWork,
        String getInvolved,
        String importantLinks,
        String ecosystemLinks,
        String blog,
        String papers,
        String readMore) {
}

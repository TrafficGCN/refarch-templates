package de.muenchen.refarch.post.dto;

import de.muenchen.refarch.link.Link;
import java.time.LocalDateTime;
import java.util.UUID;

public record PostResponseDTO(
        UUID id,
        Link link,
        String thumbnail,
        boolean commentsEnabled,
        boolean published,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public PostResponseDTO {
        if (link != null) {
            final Link defensiveCopy = new Link();
            defensiveCopy.setId(link.getId());
            defensiveCopy.setUrl(link.getUrl());
            defensiveCopy.setName(link.getName());
            defensiveCopy.setFontAwesomeIcon(link.getFontAwesomeIcon());
            defensiveCopy.setMdiIcon(link.getMdiIcon());
            defensiveCopy.setType(link.getType());
            defensiveCopy.setScope(link.getScope());
            link = defensiveCopy;
        }
    }

    @Override
    public Link link() {
        if (link == null) {
            return null;
        }
        final Link defensiveCopy = new Link();
        defensiveCopy.setId(link.getId());
        defensiveCopy.setUrl(link.getUrl());
        defensiveCopy.setName(link.getName());
        defensiveCopy.setFontAwesomeIcon(link.getFontAwesomeIcon());
        defensiveCopy.setMdiIcon(link.getMdiIcon());
        defensiveCopy.setType(link.getType());
        defensiveCopy.setScope(link.getScope());
        return defensiveCopy;
    }
}

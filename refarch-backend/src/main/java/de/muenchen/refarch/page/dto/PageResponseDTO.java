package de.muenchen.refarch.page.dto;

import de.muenchen.refarch.page.content.dto.PageContentResponseDTO;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record PageResponseDTO(
        UUID id,
        UUID linkId,
        String thumbnail,
        boolean commentsEnabled,
        boolean published,
        Set<PageContentResponseDTO> contents,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    /**
     * Creates a new PageResponseDTO with defensive copy of contents.
     */
    public PageResponseDTO {
        contents = defensiveCopyContents(contents);
    }

    /**
     * Returns an unmodifiable view of the contents set.
     * If the contents are null, returns an empty set.
     *
     * @return An unmodifiable set of page contents
     */
    @Override
    public Set<PageContentResponseDTO> contents() {
        return defensiveCopyContents(contents);
    }

    /**
     * Creates a defensive copy of the contents set.
     *
     * @param contentsToDefend The set to create a defensive copy of
     * @return An unmodifiable defensive copy of the set, or an empty set if input is null
     */
    private static Set<PageContentResponseDTO> defensiveCopyContents(final Set<PageContentResponseDTO> contentsToDefend) {
        return contentsToDefend == null ? Collections.emptySet()
                : Collections.unmodifiableSet(new HashSet<>(contentsToDefend));
    }
}

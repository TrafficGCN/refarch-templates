package de.muenchen.refarch.homepage.dto;

import de.muenchen.refarch.homepage.content.dto.HomepageContentResponseDTO;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record HomepageResponseDTO(
        UUID id,
        UUID linkId,
        String thumbnail,
        Set<HomepageContentResponseDTO> contents,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    /**
     * Creates a new HomepageResponseDTO with defensive copy of contents.
     */
    public HomepageResponseDTO {
        contents = defensiveCopyContents(contents);
    }

    /**
     * Returns an unmodifiable view of the contents set.
     * If the contents are null, returns an empty set.
     *
     * @return An unmodifiable set of homepage contents
     */
    @Override
    public Set<HomepageContentResponseDTO> contents() {
        return defensiveCopyContents(contents);
    }

    /**
     * Creates a defensive copy of the contents set.
     *
     * @param contentsToDefend The set to create a defensive copy of
     * @return An unmodifiable defensive copy of the set, or an empty set if input is null
     */
    private static Set<HomepageContentResponseDTO> defensiveCopyContents(final Set<HomepageContentResponseDTO> contentsToDefend) {
        return contentsToDefend == null ? Collections.emptySet()
                : Collections.unmodifiableSet(new HashSet<>(contentsToDefend));
    }
}

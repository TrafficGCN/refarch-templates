package de.muenchen.refarch.post.content.dto;

import de.muenchen.refarch.common.EntityCopyUtils;
import de.muenchen.refarch.language.Language;
import java.time.LocalDateTime;
import java.util.UUID;

public record PostContentResponseDTO(
        UUID id,
        UUID postId,
        Language language,
        String title,
        String content,
        String shortDescription,
        String keywords,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public PostContentResponseDTO {
        if (language != null) {
            language = EntityCopyUtils.copyLanguage(language);
        }
    }

    @Override
    public Language language() {
        return EntityCopyUtils.copyLanguage(language);
    }
}

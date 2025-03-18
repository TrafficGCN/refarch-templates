package de.muenchen.refarch.page.content;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PageContentRepository extends JpaRepository<PageContent, UUID> {
    Optional<PageContent> findByPageIdAndLanguageId(UUID pageId, UUID languageId);

    boolean existsByPageIdAndLanguageId(UUID pageId, UUID languageId);
}

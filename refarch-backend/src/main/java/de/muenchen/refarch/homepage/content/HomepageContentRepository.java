package de.muenchen.refarch.homepage.content;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HomepageContentRepository extends JpaRepository<HomepageContent, UUID> {
    Optional<HomepageContent> findByHomepageIdAndLanguageId(UUID homepageId, UUID languageId);

    boolean existsByHomepageIdAndLanguageId(UUID homepageId, UUID languageId);
}

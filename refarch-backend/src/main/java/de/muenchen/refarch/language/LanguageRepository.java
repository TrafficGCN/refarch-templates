package de.muenchen.refarch.language;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LanguageRepository extends JpaRepository<Language, UUID> {
    boolean existsByAbbreviation(String abbreviation);
}

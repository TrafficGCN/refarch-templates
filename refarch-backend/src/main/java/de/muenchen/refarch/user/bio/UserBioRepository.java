package de.muenchen.refarch.user.bio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserBioRepository extends JpaRepository<UserBio, UUID> {
    Optional<UserBio> findByUserIdAndLanguageId(UUID userId, UUID languageId);

    boolean existsByUserIdAndLanguageId(UUID userId, UUID languageId);
}

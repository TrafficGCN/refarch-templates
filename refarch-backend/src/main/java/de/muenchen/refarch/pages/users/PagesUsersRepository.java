package de.muenchen.refarch.pages.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PagesUsersRepository extends JpaRepository<PagesUsers, UUID> {
    List<PagesUsers> findByPageLinkId(UUID pageLinkId);

    List<PagesUsers> findByUserId(UUID userId);

    void deleteByPageLinkIdAndUserId(UUID pageLinkId, UUID userId);
}

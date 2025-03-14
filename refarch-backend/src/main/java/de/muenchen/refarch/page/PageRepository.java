package de.muenchen.refarch.page;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface PageRepository extends JpaRepository<Page, UUID> {
    boolean existsByLinkId(UUID linkId);
}

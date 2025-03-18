package de.muenchen.refarch.link;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface LinkRepository extends JpaRepository<Link, UUID> {
    List<Link> findByScope(LinkScope scope);
}

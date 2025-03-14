package de.muenchen.refarch.homepage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HomepageRepository extends JpaRepository<Homepage, UUID> {
}

package de.muenchen.refarch.user.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    Optional<UserSession> findByToken(String token);

    Optional<UserSession> findByRefreshToken(String refreshToken);

    List<UserSession> findAllByUserId(UUID userId);

    @Query("SELECT s FROM UserSession s WHERE s.expiresAt < :now")
    List<UserSession> findExpiredSessions(LocalDateTime now);

    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.expiresAt < :now")
    void deleteExpiredSessions(LocalDateTime now);

    void deleteAllByUserId(UUID userId);
}

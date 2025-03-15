package de.muenchen.refarch.user.session;

import de.muenchen.refarch.security.Authorities;
import de.muenchen.refarch.user.User;
import de.muenchen.refarch.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final UserSessionRepository userSessionRepository;
    private final UserRepository userRepository;

    @PreAuthorize(Authorities.USER_READ)
    @Transactional(readOnly = true)
    public Optional<UserSession> findById(UUID id) {
        return userSessionRepository.findById(id);
    }

    @PreAuthorize(Authorities.USER_READ)
    @Transactional(readOnly = true)
    public Optional<UserSession> findByToken(String token) {
        return userSessionRepository.findByToken(token);
    }

    @PreAuthorize(Authorities.USER_READ)
    @Transactional(readOnly = true)
    public Optional<UserSession> findByRefreshToken(String refreshToken) {
        return userSessionRepository.findByRefreshToken(refreshToken);
    }

    @PreAuthorize(Authorities.USER_READ)
    @Transactional(readOnly = true)
    public List<UserSession> findAllByUserId(UUID userId) {
        return userSessionRepository.findAllByUserId(userId);
    }

    @PreAuthorize(Authorities.SESSION_WRITE)
    @Transactional
    public UserSession createSession(UUID userId, String token, String refreshToken,
            LocalDateTime expiresAt, String ipAddress, String userAgent) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        UserSession session = new UserSession();
        session.setUser(user);
        session.setToken(token);
        session.setRefreshToken(refreshToken);
        session.setExpiresAt(expiresAt);
        session.setLastActivityAt(LocalDateTime.now());
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);

        return userSessionRepository.save(session);
    }

    @PreAuthorize(Authorities.SESSION_WRITE)
    @Transactional
    public void updateLastActivity(UUID sessionId) {
        UserSession session = userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found with id: " + sessionId));
        session.setLastActivityAt(LocalDateTime.now());
        userSessionRepository.save(session);
    }

    @PreAuthorize(Authorities.SESSION_DELETE)
    @Transactional
    public void deleteAllByUserId(UUID userId) {
        userSessionRepository.deleteAllByUserId(userId);
    }

    @PreAuthorize(Authorities.SESSION_DELETE)
    @Transactional
    public void deleteById(UUID sessionId) {
        userSessionRepository.deleteById(sessionId);
    }

    @PreAuthorize(Authorities.SESSION_DELETE)
    @Scheduled(cron = "0 */15 * * * *") // Run every 15 minutes
    @Transactional
    public void cleanupExpiredSessions() {
        userSessionRepository.deleteExpiredSessions(LocalDateTime.now());
    }
}

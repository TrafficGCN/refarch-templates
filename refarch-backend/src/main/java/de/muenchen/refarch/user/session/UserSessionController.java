package de.muenchen.refarch.user.session;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class UserSessionController {

    private final UserSessionService userSessionService;

    @GetMapping("/{sessionId}")
    public ResponseEntity<UserSession> getSessionById(@PathVariable UUID sessionId) {
        Optional<UserSession> session = userSessionService.findById(sessionId);
        return session.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/token/{token}")
    public ResponseEntity<UserSession> getSessionByToken(@PathVariable String token) {
        Optional<UserSession> session = userSessionService.findByToken(token);
        return session.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/refresh-token/{refreshToken}")
    public ResponseEntity<UserSession> getSessionByRefreshToken(@PathVariable String refreshToken) {
        Optional<UserSession> session = userSessionService.findByRefreshToken(refreshToken);
        return session.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserSession>> getAllSessionsByUserId(@PathVariable UUID userId) {
        List<UserSession> sessions = userSessionService.findAllByUserId(userId);
        return ResponseEntity.ok(sessions);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserSession createSession(@Valid @RequestBody CreateSessionRequest request) {
        return userSessionService.createSession(
                request.userId(),
                request.token(),
                request.refreshToken(),
                request.expiresAt(),
                request.ipAddress(),
                request.userAgent());
    }

    @PutMapping("/{sessionId}/activity")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateLastActivity(@PathVariable UUID sessionId) {
        userSessionService.updateLastActivity(sessionId);
    }

    @DeleteMapping("/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSession(@PathVariable UUID sessionId) {
        userSessionService.deleteById(sessionId);
    }

    @DeleteMapping("/user/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllUserSessions(@PathVariable UUID userId) {
        userSessionService.deleteAllByUserId(userId);
    }

    @DeleteMapping("/expired")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cleanupExpiredSessions() {
        userSessionService.cleanupExpiredSessions();
    }

    public record CreateSessionRequest(
            UUID userId,
            String token,
            String refreshToken,
            LocalDateTime expiresAt,
            String ipAddress,
            String userAgent) {
    }
}

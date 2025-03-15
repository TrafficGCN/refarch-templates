package de.muenchen.refarch.user.session;

import de.muenchen.refarch.user.User;
import de.muenchen.refarch.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSessionServiceTest {

    @Mock
    private UserSessionRepository userSessionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserSessionService userSessionService;

    @Captor
    private ArgumentCaptor<UserSession> sessionCaptor;

    private User testUser;
    private UserSession testSession;
    private UUID userId;
    private String token;
    private String refreshToken;
    private String userAgent;
    private String ipAddress;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        token = "test-token";
        refreshToken = "test-refresh-token";
        userAgent = "Mozilla/5.0";
        ipAddress = "127.0.0.1";

        testUser = new User();
        testUser.setId(userId);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testSession = new UserSession();
        testSession.setId(UUID.randomUUID());
        testSession.setUser(testUser);
        testSession.setToken(token);
        testSession.setRefreshToken(refreshToken);
        testSession.setUserAgent(userAgent);
        testSession.setIpAddress(ipAddress);
        testSession.setLastActivityAt(LocalDateTime.now());
        testSession.setExpiresAt(LocalDateTime.now().plusDays(1));
    }

    @Test
    void findByToken_WhenTokenExists_ShouldReturnSession() {
        when(userSessionRepository.findByToken(token)).thenReturn(Optional.of(testSession));

        Optional<UserSession> result = userSessionService.findByToken(token);

        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(token);
        verify(userSessionRepository).findByToken(token);
    }

    @Test
    void findByToken_WhenTokenDoesNotExist_ShouldReturnEmpty() {
        when(userSessionRepository.findByToken(token)).thenReturn(Optional.empty());

        Optional<UserSession> result = userSessionService.findByToken(token);

        assertThat(result).isEmpty();
        verify(userSessionRepository).findByToken(token);
    }

    @Test
    void findByRefreshToken_WhenRefreshTokenExists_ShouldReturnSession() {
        when(userSessionRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.of(testSession));

        Optional<UserSession> result = userSessionService.findByRefreshToken(refreshToken);

        assertThat(result).isPresent();
        assertThat(result.get().getRefreshToken()).isEqualTo(refreshToken);
        verify(userSessionRepository).findByRefreshToken(refreshToken);
    }

    @Test
    void createSession_ShouldSaveAndReturnNewSession() {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userSessionRepository.save(any(UserSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserSession result = userSessionService.createSession(userId, token, refreshToken, expiresAt, ipAddress, userAgent);

        verify(userSessionRepository).save(sessionCaptor.capture());
        UserSession savedSession = sessionCaptor.getValue();

        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getToken()).isEqualTo(token);
        assertThat(result.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(result.getUserAgent()).isEqualTo(userAgent);
        assertThat(result.getIpAddress()).isEqualTo(ipAddress);
        assertThat(result.getExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void updateLastActivity_ShouldUpdateLastActivityTime() {
        UUID sessionId = UUID.randomUUID();
        when(userSessionRepository.findById(sessionId)).thenReturn(Optional.of(testSession));
        when(userSessionRepository.save(any(UserSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userSessionService.updateLastActivity(sessionId);

        verify(userSessionRepository).save(sessionCaptor.capture());
        UserSession updatedSession = sessionCaptor.getValue();
        assertThat(updatedSession.getLastActivityAt()).isNotNull();
    }

    @Test
    void deleteById_ShouldDeleteSessionById() {
        UUID sessionId = UUID.randomUUID();
        doNothing().when(userSessionRepository).deleteById(sessionId);

        userSessionService.deleteById(sessionId);

        verify(userSessionRepository).deleteById(sessionId);
    }

    @Test
    void cleanupExpiredSessions_ShouldDeleteExpiredSessions() {
        doNothing().when(userSessionRepository).deleteExpiredSessions(any(LocalDateTime.class));

        userSessionService.cleanupExpiredSessions();

        verify(userSessionRepository).deleteExpiredSessions(any(LocalDateTime.class));
    }
}

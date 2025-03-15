package de.muenchen.refarch.user.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.muenchen.refarch.MicroServiceApplication;
import de.muenchen.refarch.TestConstants;
import de.muenchen.refarch.config.TestConfig;
import de.muenchen.refarch.user.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(
        classes = { MicroServiceApplication.class },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles(profiles = { TestConstants.SPRING_TEST_PROFILE, TestConstants.SPRING_NO_SECURITY_PROFILE })
@AutoConfigureMockMvc
@Import(TestConfig.class)
class UserSessionControllerTest {

    @Container
    @ServiceConnection
    @SuppressWarnings("unused")
    private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER = new PostgreSQLContainer<>(
            DockerImageName.parse(TestConstants.TESTCONTAINERS_POSTGRES_IMAGE));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserSessionService userSessionService;

    private UUID userId;
    private UUID sessionId;
    private User testUser;
    private UserSession testSession;
    private String token;
    private String refreshToken;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        sessionId = UUID.randomUUID();
        token = "test-token";
        refreshToken = "test-refresh-token";

        testUser = new User();
        testUser.setId(userId);

        testSession = new UserSession();
        testSession.setId(sessionId);
        testSession.setUser(testUser);
        testSession.setToken(token);
        testSession.setRefreshToken(refreshToken);
        testSession.setUserAgent("Mozilla/5.0");
        testSession.setIpAddress("127.0.0.1");
        testSession.setLastActivityAt(LocalDateTime.now());
        testSession.setExpiresAt(LocalDateTime.now().plusDays(1));
    }

    @Test
    void getSessionById_WhenSessionExists_ShouldReturnSession() throws Exception {
        when(userSessionService.findById(sessionId)).thenReturn(Optional.of(testSession));

        mockMvc.perform(get("/sessions/{id}", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sessionId.toString()))
                .andExpect(jsonPath("$.token").value(token));
    }

    @Test
    void getSessionById_WhenSessionDoesNotExist_ShouldReturnNotFound() throws Exception {
        when(userSessionService.findById(sessionId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/sessions/{id}", sessionId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSessionByToken_WhenSessionExists_ShouldReturnSession() throws Exception {
        when(userSessionService.findByToken(token)).thenReturn(Optional.of(testSession));

        mockMvc.perform(get("/sessions/token/{token}", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token));
    }

    @Test
    void getSessionByRefreshToken_WhenSessionExists_ShouldReturnSession() throws Exception {
        when(userSessionService.findByRefreshToken(refreshToken)).thenReturn(Optional.of(testSession));

        mockMvc.perform(get("/sessions/refresh-token/{refreshToken}", refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").value(refreshToken));
    }

    @Test
    void getAllSessionsByUserId_ShouldReturnSessions() throws Exception {
        when(userSessionService.findAllByUserId(userId)).thenReturn(List.of(testSession));

        mockMvc.perform(get("/sessions/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(sessionId.toString()));
    }

    @Test
    void createSession_WithValidRequest_ShouldCreateSession() throws Exception {
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
        UserSessionController.CreateSessionRequest request = new UserSessionController.CreateSessionRequest(
                userId, token, refreshToken, expiresAt, "127.0.0.1", "Mozilla/5.0");

        when(userSessionService.createSession(
                userId, token, refreshToken, expiresAt, "127.0.0.1", "Mozilla/5.0"))
                        .thenReturn(testSession);

        mockMvc.perform(post("/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value(token));
    }

    @Test
    void updateLastActivity_ShouldUpdateActivity() throws Exception {
        doNothing().when(userSessionService).updateLastActivity(sessionId);

        mockMvc.perform(put("/sessions/{sessionId}/activity", sessionId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteSession_ShouldDeleteSession() throws Exception {
        doNothing().when(userSessionService).deleteById(sessionId);

        mockMvc.perform(delete("/sessions/{sessionId}", sessionId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAllUserSessions_ShouldDeleteAllSessions() throws Exception {
        doNothing().when(userSessionService).deleteAllByUserId(userId);

        mockMvc.perform(delete("/sessions/user/{userId}", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    void cleanupExpiredSessions_ShouldCleanupSessions() throws Exception {
        doNothing().when(userSessionService).cleanupExpiredSessions();

        mockMvc.perform(delete("/sessions/expired"))
                .andExpect(status().isNoContent());
    }
}

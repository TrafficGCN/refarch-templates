package de.muenchen.refarch.user.session;

import de.muenchen.refarch.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class UserSessionRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UserSession testSession;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser = entityManager.persist(testUser);
        entityManager.flush();

        LocalDateTime now = LocalDateTime.now();
        testSession = new UserSession();
        testSession.setUser(testUser);
        testSession.setToken("test-token");
        testSession.setRefreshToken("test-refresh-token");
        testSession.setUserAgent("Mozilla/5.0");
        testSession.setIpAddress("127.0.0.1");
        testSession.setLastActivityAt(now);
        testSession.setExpiresAt(now.plusDays(1));
        testSession.setCreatedAt(now);
        testSession.setUpdatedAt(now);

        testSession = entityManager.persist(testSession);
        entityManager.flush();
    }

    @Test
    void findByToken_WhenTokenExists_ShouldReturnSession() {
        Optional<UserSession> result = userSessionRepository.findByToken("test-token");

        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo("test-token");
    }

    @Test
    void findByToken_WhenTokenDoesNotExist_ShouldReturnEmpty() {
        Optional<UserSession> result = userSessionRepository.findByToken("non-existent-token");

        assertThat(result).isEmpty();
    }

    @Test
    void findByRefreshToken_WhenRefreshTokenExists_ShouldReturnSession() {
        Optional<UserSession> result = userSessionRepository.findByRefreshToken("test-refresh-token");

        assertThat(result).isPresent();
        assertThat(result.get().getRefreshToken()).isEqualTo("test-refresh-token");
    }

    @Test
    void findByRefreshToken_WhenRefreshTokenDoesNotExist_ShouldReturnEmpty() {
        Optional<UserSession> result = userSessionRepository.findByRefreshToken("non-existent-refresh-token");

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByUserId_ShouldReturnAllUserSessions() {
        // Create another session for the same user
        LocalDateTime now = LocalDateTime.now();
        UserSession anotherSession = new UserSession();
        anotherSession.setUser(testUser);
        anotherSession.setToken("another-token");
        anotherSession.setRefreshToken("another-refresh-token");
        anotherSession.setUserAgent("Chrome/90.0");
        anotherSession.setIpAddress("192.168.1.1");
        anotherSession.setLastActivityAt(now);
        anotherSession.setExpiresAt(now.plusDays(1));
        anotherSession.setCreatedAt(now);
        anotherSession.setUpdatedAt(now);
        entityManager.persist(anotherSession);
        entityManager.flush();

        List<UserSession> results = userSessionRepository.findAllByUserId(testUser.getId());

        assertThat(results).hasSize(2);
        assertThat(results).extracting(session -> session.getUser().getId())
                .containsOnly(testUser.getId());
    }

    @Test
    void deleteExpiredSessions_ShouldRemoveExpiredSessions() {
        // Create an expired session for a different user
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("password");
        anotherUser = entityManager.persist(anotherUser);

        LocalDateTime now = LocalDateTime.now();
        UserSession expiredSession = new UserSession();
        expiredSession.setUser(anotherUser);
        expiredSession.setToken("expired-token");
        expiredSession.setRefreshToken("expired-refresh-token");
        expiredSession.setUserAgent("Firefox/89.0");
        expiredSession.setIpAddress("10.0.0.1");
        expiredSession.setLastActivityAt(now.minusDays(2));
        expiredSession.setExpiresAt(now.minusDays(1));
        expiredSession.setCreatedAt(now.minusDays(2));
        expiredSession.setUpdatedAt(now.minusDays(2));
        entityManager.persist(expiredSession);
        entityManager.flush();

        userSessionRepository.deleteExpiredSessions(now);

        List<UserSession> remainingSessions = userSessionRepository.findAll();
        assertThat(remainingSessions).hasSize(1)
                .extracting(UserSession::getToken)
                .containsOnly("test-token");
    }

    @Test
    void deleteAllByUserId_ShouldDeleteAllUserSessions() {
        userSessionRepository.deleteAllByUserId(testUser.getId());
        entityManager.flush();

        List<UserSession> remainingSessions = userSessionRepository.findAllByUserId(testUser.getId());
        assertThat(remainingSessions).isEmpty();
    }
}

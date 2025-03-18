package de.muenchen.refarch.pages.users;

import de.muenchen.refarch.TestConstants;
import de.muenchen.refarch.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class PagesUsersRepositoryTest {

    @Container
    @ServiceConnection
    @SuppressWarnings("unused")
    private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER = new PostgreSQLContainer<>(
            DockerImageName.parse(TestConstants.TESTCONTAINERS_POSTGRES_IMAGE));

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PagesUsersRepository pagesUsersRepository;

    private User user;
    private UUID pageLinkId;
    private PagesUsers pagesUsers;

    @BeforeEach
    void setUp() {
        // Create and persist a user
        user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        entityManager.persist(user);

        // Create a page link ID
        pageLinkId = UUID.randomUUID();

        // Create and persist a pages_users entry
        pagesUsers = new PagesUsers();
        pagesUsers.setPageLinkId(pageLinkId);
        pagesUsers.setUser(user);
        entityManager.persist(pagesUsers);
        entityManager.flush();
    }

    @Test
    void findByPageLinkId_WhenExists_ShouldReturnList() {
        // Act
        final List<PagesUsers> result = pagesUsersRepository.findByPageLinkId(pageLinkId);

        // Assert
        assertThat(result)
                .hasSize(1)
                .first()
                .satisfies(pu -> {
                    assertThat(pu.getPageLinkId()).isEqualTo(pageLinkId);
                    assertThat(pu.getUser().getId()).isEqualTo(user.getId());
                });
    }

    @Test
    void findByPageLinkId_WhenDoesNotExist_ShouldReturnEmptyList() {
        // Act
        final List<PagesUsers> result = pagesUsersRepository.findByPageLinkId(UUID.randomUUID());

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findByUserId_WhenExists_ShouldReturnList() {
        // Act
        final List<PagesUsers> result = pagesUsersRepository.findByUserId(user.getId());

        // Assert
        assertThat(result)
                .hasSize(1)
                .first()
                .satisfies(pu -> {
                    assertThat(pu.getPageLinkId()).isEqualTo(pageLinkId);
                    assertThat(pu.getUser().getId()).isEqualTo(user.getId());
                });
    }

    @Test
    void findByUserId_WhenDoesNotExist_ShouldReturnEmptyList() {
        // Act
        final List<PagesUsers> result = pagesUsersRepository.findByUserId(UUID.randomUUID());

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void deleteByPageLinkIdAndUserId_WhenExists_ShouldDelete() {
        // Act
        pagesUsersRepository.deleteByPageLinkIdAndUserId(pageLinkId, user.getId());
        entityManager.flush();

        // Assert
        final PagesUsers foundPagesUsers = entityManager.find(PagesUsers.class, pagesUsers.getId());
        assertThat(foundPagesUsers).isNull();
    }

    @Test
    void save_ShouldCreatePagesUsers() {
        // Arrange
        final UUID newPageLinkId = UUID.randomUUID();
        final PagesUsers newPagesUsers = new PagesUsers();
        newPagesUsers.setPageLinkId(newPageLinkId);
        newPagesUsers.setUser(user);

        // Act
        final PagesUsers savedPagesUsers = pagesUsersRepository.save(newPagesUsers);

        // Assert
        final PagesUsers foundPagesUsers = entityManager.find(PagesUsers.class, savedPagesUsers.getId());
        assertThat(foundPagesUsers).isNotNull();
        assertThat(foundPagesUsers.getPageLinkId()).isEqualTo(newPageLinkId);
        assertThat(foundPagesUsers.getUser().getId()).isEqualTo(user.getId());
    }
}

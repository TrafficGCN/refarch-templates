package de.muenchen.refarch.posts.users;

import de.muenchen.refarch.user.User;
import de.muenchen.refarch.TestConstants;
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
class PostsUsersRepositoryTest {

    @Container
    @ServiceConnection
    @SuppressWarnings("unused")
    private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER = new PostgreSQLContainer<>(
            DockerImageName.parse(TestConstants.TESTCONTAINERS_POSTGRES_IMAGE));

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PostsUsersRepository postsUsersRepository;

    private User user;
    private UUID postLinkId;
    private PostsUsers postsUsers;

    @BeforeEach
    void setUp() {
        // Create and persist a user
        user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        entityManager.persist(user);

        // Create a post link ID
        postLinkId = UUID.randomUUID();

        // Create and persist a posts_users entry
        postsUsers = new PostsUsers();
        postsUsers.setPostLinkId(postLinkId);
        postsUsers.setUser(user);
        entityManager.persist(postsUsers);
        entityManager.flush();
    }

    @Test
    void shouldFindPostsUsersByPostLinkId() {
        // Act
        final List<PostsUsers> result = postsUsersRepository.findByPostLinkId(postLinkId);

        // Assert
        assertThat(result)
                .hasSize(1)
                .first()
                .satisfies(pu -> {
                    assertThat(pu.getPostLinkId()).isEqualTo(postLinkId);
                    assertThat(pu.getUser().getId()).isEqualTo(user.getId());
                });
    }

    @Test
    void shouldReturnEmptyListWhenPostLinkIdNotFound() {
        // Act
        final List<PostsUsers> result = postsUsersRepository.findByPostLinkId(UUID.randomUUID());

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindPostsUsersByUserId() {
        // Act
        final List<PostsUsers> result = postsUsersRepository.findByUserId(user.getId());

        // Assert
        assertThat(result)
                .hasSize(1)
                .first()
                .satisfies(pu -> {
                    assertThat(pu.getPostLinkId()).isEqualTo(postLinkId);
                    assertThat(pu.getUser().getId()).isEqualTo(user.getId());
                });
    }

    @Test
    void shouldReturnEmptyListWhenUserIdNotFound() {
        // Act
        final List<PostsUsers> result = postsUsersRepository.findByUserId(UUID.randomUUID());

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void shouldDeletePostsUsersByPostLinkIdAndUserId() {
        // Act
        postsUsersRepository.deleteByPostLinkIdAndUserId(postLinkId, user.getId());
        entityManager.flush();

        // Assert
        final PostsUsers foundPostsUsers = entityManager.find(PostsUsers.class, postsUsers.getId());
        assertThat(foundPostsUsers).isNull();
    }

    @Test
    void shouldSavePostsUsers() {
        // Arrange
        final UUID newPostLinkId = UUID.randomUUID();
        final PostsUsers newPostsUsers = new PostsUsers();
        newPostsUsers.setPostLinkId(newPostLinkId);
        newPostsUsers.setUser(user);

        // Act
        final PostsUsers savedPostsUsers = postsUsersRepository.save(newPostsUsers);

        // Assert
        final PostsUsers foundPostsUsers = entityManager.find(PostsUsers.class, savedPostsUsers.getId());
        assertThat(foundPostsUsers).isNotNull();
        assertThat(foundPostsUsers.getPostLinkId()).isEqualTo(newPostLinkId);
        assertThat(foundPostsUsers.getUser().getId()).isEqualTo(user.getId());
    }
}

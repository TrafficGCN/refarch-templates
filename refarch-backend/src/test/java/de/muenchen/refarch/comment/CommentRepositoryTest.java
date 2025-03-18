package de.muenchen.refarch.comment;

import de.muenchen.refarch.page.Page;
import de.muenchen.refarch.post.Post;
import de.muenchen.refarch.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class CommentRepositoryTest {

    private static final String POSTGRES_IMAGE = "postgres:16.0-alpine3.18";
    private static final String USER1_USERNAME = "user1";
    private static final String USER2_USERNAME = "user2";
    private static final String EMAIL_DOMAIN = "@example.com";
    private static final String DEFAULT_PASSWORD = "password123";
    private static final String POST_COMMENT_1 = "Post comment 1";
    private static final String POST_COMMENT_2 = "Post comment 2";
    private static final String PAGE_COMMENT_1 = "Page comment 1";
    private static final String PAGE_COMMENT_2 = "Page comment 2";

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(POSTGRES_IMAGE);

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentRepository commentRepository;

    private User user1;
    private User user2;
    private Post post1;
    private Page page1;
    private Comment postComment1;
    private Comment postComment2;
    private Comment pageComment1;
    private Comment pageComment2;

    @BeforeEach
    void setUp() {
        // Create users
        user1 = new User();
        user1.setUsername(USER1_USERNAME);
        user1.setEmail(USER1_USERNAME + EMAIL_DOMAIN);
        user1.setPassword(DEFAULT_PASSWORD);
        entityManager.persist(user1);

        user2 = new User();
        user2.setUsername(USER2_USERNAME);
        user2.setEmail(USER2_USERNAME + EMAIL_DOMAIN);
        user2.setPassword(DEFAULT_PASSWORD);
        entityManager.persist(user2);

        // Create posts
        post1 = new Post();
        post1.setCommentsEnabled(true);
        entityManager.persist(post1);

        final Post post2 = new Post();
        post2.setCommentsEnabled(true);
        entityManager.persist(post2);

        // Create pages
        page1 = new Page();
        page1.setCommentsEnabled(true);
        entityManager.persist(page1);

        final Page page2 = new Page();
        page2.setCommentsEnabled(true);
        entityManager.persist(page2);

        // Create comments for posts
        postComment1 = new Comment();
        postComment1.setContent(POST_COMMENT_1);
        postComment1.setUser(user1);
        postComment1.setPostId(post1.getId());
        entityManager.persist(postComment1);

        postComment2 = new Comment();
        postComment2.setContent(POST_COMMENT_2);
        postComment2.setUser(user1);
        postComment2.setPostId(post1.getId());
        entityManager.persist(postComment2);

        // Create comments for pages
        pageComment1 = new Comment();
        pageComment1.setContent(PAGE_COMMENT_1);
        pageComment1.setUser(user2);
        pageComment1.setPageId(page1.getId());
        entityManager.persist(pageComment1);

        pageComment2 = new Comment();
        pageComment2.setContent(PAGE_COMMENT_2);
        pageComment2.setUser(user2);
        pageComment2.setPageId(page1.getId());
        entityManager.persist(pageComment2);

        entityManager.flush();
    }

    @Test
    void findByPostId_ShouldReturnCommentsForPost() {
        final List<Comment> comments = commentRepository.findByPostId(post1.getId());
        assertThat(comments).hasSize(2);
        assertThat(comments).contains(postComment1, postComment2);
    }

    @Test
    void findByPageId_ShouldReturnCommentsForPage() {
        final List<Comment> comments = commentRepository.findByPageId(page1.getId());
        assertThat(comments).hasSize(2);
        assertThat(comments).contains(pageComment1, pageComment2);
    }

    @Test
    void findByUserId_ShouldReturnCommentsForUser() {
        final List<Comment> comments = commentRepository.findByUserId(user1.getId());
        assertThat(comments).hasSize(2);
        assertThat(comments).contains(postComment1, postComment2);
    }

    @Test
    void findByPostIdOrderByCreatedAtDesc_ShouldReturnCommentsOrderedByCreatedAt() {
        final List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtDesc(post1.getId());
        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getCreatedAt()).isAfterOrEqualTo(comments.get(1).getCreatedAt());
    }

    @Test
    void findByPageIdOrderByCreatedAtDesc_ShouldReturnCommentsOrderedByCreatedAt() {
        final List<Comment> comments = commentRepository.findByPageIdOrderByCreatedAtDesc(page1.getId());
        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getCreatedAt()).isAfterOrEqualTo(comments.get(1).getCreatedAt());
    }

    @Test
    void deleteByPageIdAndUserId_ShouldDeleteOnlyUserComments() {
        commentRepository.deleteByPageIdAndUserId(page1.getId(), user2.getId());
        final List<Comment> remainingComments = commentRepository.findByPageId(page1.getId());
        assertThat(remainingComments).isEmpty();
    }

    @Test
    void deleteByPostIdAndUserId_ShouldDeleteOnlyUserComments() {
        commentRepository.deleteByPostIdAndUserId(post1.getId(), user1.getId());
        final List<Comment> remainingComments = commentRepository.findByPostId(post1.getId());
        assertThat(remainingComments).isEmpty();
    }
}

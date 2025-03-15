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
public class CommentRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.0-alpine3.18");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentRepository commentRepository;

    private User user1;
    private User user2;
    private Post post1;
    private Post post2;
    private Page page1;
    private Page page2;
    private Comment postComment1;
    private Comment postComment2;
    private Comment pageComment1;
    private Comment pageComment2;

    @BeforeEach
    void setUp() {
        // Create users
        user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPassword("password123");
        entityManager.persist(user1);

        user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword("password123");
        entityManager.persist(user2);

        // Create posts
        post1 = new Post();
        post1.setCommentsEnabled(true);
        entityManager.persist(post1);

        post2 = new Post();
        post2.setCommentsEnabled(true);
        entityManager.persist(post2);

        // Create pages
        page1 = new Page();
        page1.setCommentsEnabled(true);
        entityManager.persist(page1);

        page2 = new Page();
        page2.setCommentsEnabled(true);
        entityManager.persist(page2);

        // Create comments for posts
        postComment1 = new Comment();
        postComment1.setContent("Post comment 1");
        postComment1.setUser(user1);
        postComment1.setPostId(post1.getId());
        entityManager.persist(postComment1);

        postComment2 = new Comment();
        postComment2.setContent("Post comment 2");
        postComment2.setUser(user1);
        postComment2.setPostId(post1.getId());
        entityManager.persist(postComment2);

        // Create comments for pages
        pageComment1 = new Comment();
        pageComment1.setContent("Page comment 1");
        pageComment1.setUser(user2);
        pageComment1.setPageId(page1.getId());
        entityManager.persist(pageComment1);

        pageComment2 = new Comment();
        pageComment2.setContent("Page comment 2");
        pageComment2.setUser(user2);
        pageComment2.setPageId(page1.getId());
        entityManager.persist(pageComment2);

        entityManager.flush();
    }

    @Test
    void findByPostId_ShouldReturnCommentsForPost() {
        List<Comment> comments = commentRepository.findByPostId(post1.getId());
        assertThat(comments).hasSize(2);
        assertThat(comments).contains(postComment1, postComment2);
    }

    @Test
    void findByPageId_ShouldReturnCommentsForPage() {
        List<Comment> comments = commentRepository.findByPageId(page1.getId());
        assertThat(comments).hasSize(2);
        assertThat(comments).contains(pageComment1, pageComment2);
    }

    @Test
    void findByUserId_ShouldReturnCommentsForUser() {
        List<Comment> comments = commentRepository.findByUser_Id(user1.getId());
        assertThat(comments).hasSize(2);
        assertThat(comments).contains(postComment1, postComment2);
    }

    @Test
    void findByPostIdOrderByCreatedAtDesc_ShouldReturnCommentsOrderedByCreatedAt() {
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtDesc(post1.getId());
        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getCreatedAt()).isAfterOrEqualTo(comments.get(1).getCreatedAt());
    }

    @Test
    void findByPageIdOrderByCreatedAtDesc_ShouldReturnCommentsOrderedByCreatedAt() {
        List<Comment> comments = commentRepository.findByPageIdOrderByCreatedAtDesc(page1.getId());
        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getCreatedAt()).isAfterOrEqualTo(comments.get(1).getCreatedAt());
    }

    @Test
    void deleteByPageIdAndUser_Id_ShouldDeleteOnlyUserComments() {
        commentRepository.deleteByPageIdAndUser_Id(page1.getId(), user2.getId());
        List<Comment> remainingComments = commentRepository.findByPageId(page1.getId());
        assertThat(remainingComments).isEmpty();
    }

    @Test
    void deleteByPostIdAndUser_Id_ShouldDeleteOnlyUserComments() {
        commentRepository.deleteByPostIdAndUser_Id(post1.getId(), user1.getId());
        List<Comment> remainingComments = commentRepository.findByPostId(post1.getId());
        assertThat(remainingComments).isEmpty();
    }
}

package de.muenchen.refarch.comment;

import de.muenchen.refarch.user.User;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CommentTest {

    @Test
    void testCommentCreation() {
        Comment comment = new Comment();
        UUID id = UUID.randomUUID();
        String content = "Test comment";
        UUID postId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        User user = new User();
        LocalDateTime now = LocalDateTime.now();

        comment.setId(id);
        comment.setContent(content);
        comment.setPostId(postId);
        comment.setPageId(pageId);
        comment.setUser(user);
        comment.setCreatedAt(now);
        comment.setUpdatedAt(now);

        assertThat(comment.getId()).isEqualTo(id);
        assertThat(comment.getContent()).isEqualTo(content);
        assertThat(comment.getPostId()).isEqualTo(postId);
        assertThat(comment.getPageId()).isEqualTo(pageId);
        assertThat(comment.getUser()).isEqualTo(user);
        assertThat(comment.getCreatedAt()).isEqualTo(now);
        assertThat(comment.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void testCommentBuilder() {
        UUID id = UUID.randomUUID();
        String content = "Test comment";
        UUID postId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        User user = new User();
        LocalDateTime now = LocalDateTime.now();

        Comment comment = Comment.builder()
                .id(id)
                .content(content)
                .postId(postId)
                .pageId(pageId)
                .user(user)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(comment.getId()).isEqualTo(id);
        assertThat(comment.getContent()).isEqualTo(content);
        assertThat(comment.getPostId()).isEqualTo(postId);
        assertThat(comment.getPageId()).isEqualTo(pageId);
        assertThat(comment.getUser()).isEqualTo(user);
        assertThat(comment.getCreatedAt()).isEqualTo(now);
        assertThat(comment.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void testPrePersist() {
        Comment comment = new Comment();
        comment.onCreate();

        assertThat(comment.getCreatedAt()).isNotNull();
        assertThat(comment.getUpdatedAt()).isEqualTo(comment.getCreatedAt());
    }

    @Test
    void testPreUpdate() {
        Comment comment = new Comment();
        comment.onCreate();
        LocalDateTime createdAt = comment.getCreatedAt();

        // Simulate some time passing
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        comment.onUpdate();

        assertThat(comment.getUpdatedAt()).isNotNull();
        assertThat(comment.getUpdatedAt()).isAfter(createdAt);
    }
}

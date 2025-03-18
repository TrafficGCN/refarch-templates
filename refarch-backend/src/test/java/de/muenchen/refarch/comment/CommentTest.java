package de.muenchen.refarch.comment;

import de.muenchen.refarch.user.User;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CommentTest {

    private static final String TEST_COMMENT_CONTENT = "Test comment";
    private static final long SLEEP_DURATION_MS = 10L;

    @Test
    void shouldReturnSameValuesAfterSettingFields() {
        final Comment comment = new Comment();
        final UUID id = UUID.randomUUID();
        final String content = TEST_COMMENT_CONTENT;
        final UUID postId = UUID.randomUUID();
        final UUID pageId = UUID.randomUUID();
        final User user = new User();
        final LocalDateTime now = LocalDateTime.now();

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
    void shouldCreateCommentWithAllFieldsWhenBuilding() {
        final UUID id = UUID.randomUUID();
        final String content = TEST_COMMENT_CONTENT;
        final UUID postId = UUID.randomUUID();
        final UUID pageId = UUID.randomUUID();
        final User user = new User();
        final LocalDateTime now = LocalDateTime.now();

        final Comment comment = Comment.builder()
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
    void shouldSetTimestampsWhenCreatingComment() {
        final Comment comment = new Comment();
        comment.onCreate();

        assertThat(comment.getCreatedAt()).isNotNull();
        assertThat(comment.getUpdatedAt()).isEqualTo(comment.getCreatedAt());
    }

    @Test
    @SuppressWarnings("PMD.DoNotUseThreads")
    void shouldUpdateTimestampWhenUpdatingComment() {
        final Comment comment = new Comment();
        comment.onCreate();
        final LocalDateTime createdAt = comment.getCreatedAt();

        try {
            Thread.sleep(SLEEP_DURATION_MS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        comment.onUpdate();

        assertThat(comment.getUpdatedAt()).isNotNull();
        assertThat(comment.getUpdatedAt()).isAfter(createdAt);
    }
}

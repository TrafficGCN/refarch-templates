package de.muenchen.refarch.post;

import de.muenchen.refarch.link.Link;
import de.muenchen.refarch.link.LinkScope;
import de.muenchen.refarch.post.content.PostContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PostTest {

    private static final String EXAMPLE_URL = "https://example.com";
    private static final String EXAMPLE_LINK_NAME = "Example Link";
    private static final String THUMBNAIL_PATH = "thumbnail.jpg";
    private static final String NEW_EXAMPLE_URL = "https://new-example.com";
    private static final String NEW_EXAMPLE_LINK_NAME = "New Example Link";
    private static final String NEW_THUMBNAIL_PATH = "new-thumbnail.jpg";
    private static final String TEST_TITLE = "Test Title";
    private static final String TEST_CONTENT = "Test Content";
    private static final String TITLE_1 = "Title 1";
    private static final String TITLE_2 = "Title 2";
    private static final long SLEEP_DURATION_MS = 10L;

    private Post post;
    private final Link link = new Link();
    private final UUID postId = UUID.randomUUID();
    private final UUID linkId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        link.setId(linkId);
        link.setUrl(EXAMPLE_URL);
        link.setName(EXAMPLE_LINK_NAME);
        link.setScope(LinkScope.EXTERNAL);

        post = new Post();
        post.setId(postId);
        post.setLink(link);
        post.setThumbnail(THUMBNAIL_PATH);
        post.setCommentsEnabled(true);
    }

    @Test
    void shouldHaveAllFieldsSetAfterCreation() {
        assertThat(post.getId()).isEqualTo(postId);
        assertThat(post.getLink()).isEqualTo(link);
        assertThat(post.getThumbnail()).isEqualTo(THUMBNAIL_PATH);
        assertThat(post.isCommentsEnabled()).isTrue();
        assertThat(post.getContents()).isEmpty();
    }

    @Test
    void shouldReflectNewValuesAfterUpdating() {
        final Link newLink = new Link();
        newLink.setId(UUID.randomUUID());
        newLink.setUrl(NEW_EXAMPLE_URL);
        newLink.setName(NEW_EXAMPLE_LINK_NAME);
        newLink.setScope(LinkScope.INTERNAL);

        post.setLink(newLink);
        post.setThumbnail(NEW_THUMBNAIL_PATH);
        post.setCommentsEnabled(false);

        assertThat(post.getLink()).isEqualTo(newLink);
        assertThat(post.getThumbnail()).isEqualTo(NEW_THUMBNAIL_PATH);
        assertThat(post.isCommentsEnabled()).isFalse();
    }

    @Test
    void shouldUpdateRelationshipsWhenAddingContent() {
        final PostContent content = new PostContent();
        content.setId(UUID.randomUUID());
        content.setTitle(TEST_TITLE);
        content.setContent(TEST_CONTENT);

        post.addContent(content);

        assertThat(post.getContents()).hasSize(1);
        assertThat(post.getContents()).contains(content);
        assertThat(content.getPost()).isEqualTo(post);
    }

    @Test
    void shouldClearRelationshipsWhenRemovingContent() {
        final PostContent content = new PostContent();
        content.setId(UUID.randomUUID());
        content.setTitle(TEST_TITLE);
        content.setContent(TEST_CONTENT);

        post.addContent(content);
        post.removeContent(content);

        assertThat(post.getContents()).isEmpty();
        assertThat(content.getPost()).isNull();
    }

    @Test
    void shouldHandleMultipleContentsCorrectly() {
        final PostContent content1 = new PostContent();
        content1.setId(UUID.randomUUID());
        content1.setTitle(TITLE_1);

        final PostContent content2 = new PostContent();
        content2.setId(UUID.randomUUID());
        content2.setTitle(TITLE_2);

        post.addContent(content1);
        post.addContent(content2);

        assertThat(post.getContents())
                .hasSize(2)
                .contains(content1, content2);
        assertThat(content1.getPost()).isEqualTo(post);
        assertThat(content2.getPost()).isEqualTo(post);

        post.removeContent(content1);

        assertThat(post.getContents())
                .hasSize(1)
                .contains(content2);
        assertThat(content1.getPost()).isNull();
        assertThat(content2.getPost()).isEqualTo(post);
    }

    @Test
    @SuppressWarnings("PMD.DoNotUseThreads")
    void shouldHaveTimestampsSetAfterCreation() {
        final Post newPost = new Post();
        final LocalDateTime now = LocalDateTime.now();
        newPost.setCreatedAt(now);
        newPost.setUpdatedAt(now);

        assertThat(newPost.getCreatedAt()).isEqualTo(now);
        assertThat(newPost.getUpdatedAt()).isEqualTo(now);

        try {
            Thread.sleep(SLEEP_DURATION_MS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        final LocalDateTime later = LocalDateTime.now();
        newPost.setUpdatedAt(later);

        assertThat(newPost.getUpdatedAt()).isNotNull();
        assertThat(newPost.getUpdatedAt()).isAfter(newPost.getCreatedAt());
    }
}

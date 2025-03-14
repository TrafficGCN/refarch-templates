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

    private Post post;
    private Link link;
    private UUID postId;
    private UUID linkId;

    @BeforeEach
    void setUp() {
        postId = UUID.randomUUID();
        linkId = UUID.randomUUID();

        link = new Link();
        link.setId(linkId);
        link.setUrl("https://example.com");
        link.setName("Example Link");
        link.setScope(LinkScope.external);

        post = new Post();
        post.setId(postId);
        post.setLink(link);
        post.setThumbnail("thumbnail.jpg");
        post.setCommentsEnabled(true);
    }

    @Test
    void shouldCreatePostWithAllFields() {
        assertThat(post.getId()).isEqualTo(postId);
        assertThat(post.getLink()).isEqualTo(link);
        assertThat(post.getThumbnail()).isEqualTo("thumbnail.jpg");
        assertThat(post.isCommentsEnabled()).isTrue();
        assertThat(post.getContents()).isEmpty();
    }

    @Test
    void shouldUpdateFields() {
        Link newLink = new Link();
        newLink.setId(UUID.randomUUID());
        newLink.setUrl("https://new-example.com");
        newLink.setName("New Example Link");
        newLink.setScope(LinkScope.internal);

        post.setLink(newLink);
        post.setThumbnail("new-thumbnail.jpg");
        post.setCommentsEnabled(false);

        assertThat(post.getLink()).isEqualTo(newLink);
        assertThat(post.getThumbnail()).isEqualTo("new-thumbnail.jpg");
        assertThat(post.isCommentsEnabled()).isFalse();
    }

    @Test
    void shouldAddContent() {
        PostContent content = new PostContent();
        content.setId(UUID.randomUUID());
        content.setTitle("Test Title");
        content.setContent("Test Content");

        post.addContent(content);

        assertThat(post.getContents()).hasSize(1);
        assertThat(post.getContents()).contains(content);
        assertThat(content.getPost()).isEqualTo(post);
    }

    @Test
    void shouldRemoveContent() {
        PostContent content = new PostContent();
        content.setId(UUID.randomUUID());
        content.setTitle("Test Title");
        content.setContent("Test Content");

        post.addContent(content);
        post.removeContent(content);

        assertThat(post.getContents()).isEmpty();
        assertThat(content.getPost()).isNull();
    }

    @Test
    void shouldManageMultipleContents() {
        PostContent content1 = new PostContent();
        content1.setId(UUID.randomUUID());
        content1.setTitle("Title 1");

        PostContent content2 = new PostContent();
        content2.setId(UUID.randomUUID());
        content2.setTitle("Title 2");

        post.addContent(content1);
        post.addContent(content2);

        assertThat(post.getContents()).hasSize(2);
        assertThat(post.getContents()).containsExactlyInAnyOrder(content1, content2);
    }

    @Test
    void shouldHaveTimestamps() {
        LocalDateTime now = LocalDateTime.now();

        post.setCreatedAt(now);
        post.setUpdatedAt(now);

        assertThat(post.getCreatedAt()).isEqualTo(now);
        assertThat(post.getUpdatedAt()).isEqualTo(now);
    }
}

package de.muenchen.refarch.post.content;

import de.muenchen.refarch.language.Language;
import de.muenchen.refarch.link.Link;
import de.muenchen.refarch.link.LinkScope;
import de.muenchen.refarch.post.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PostContentTest {

    private Post post;
    private PostContent content;

    @BeforeEach
    void setUp() {
        final Link link = new Link();
        link.setId(UUID.randomUUID());
        link.setUrl("https://example.com");
        link.setName("Example Link");
        link.setScope(LinkScope.EXTERNAL);

        final Language language = new Language();
        language.setId(UUID.randomUUID());
        language.setName("English");
        language.setAbbreviation("en");

        post = new Post();
        post.setId(UUID.randomUUID());
        post.setLink(link);
        post.setThumbnail("test.jpg");
        post.setCommentsEnabled(true);

        content = new PostContent();
        content.setId(UUID.randomUUID());
        content.setLanguage(language);
        content.setTitle("Test Title");
        content.setContent("Test Content");
        content.setShortDescription("Test Description");
        content.setKeywords("test,keywords");
    }

    @Test
    void setPost_ShouldSetPost() {
        // Act
        content.setPost(post);

        // Assert
        assertThat(content.getPost()).isEqualTo(post);
    }

    @Test
    void setLanguage_ShouldSetLanguage() {
        // Arrange
        final Language newLanguage = new Language();
        newLanguage.setId(UUID.randomUUID());
        newLanguage.setName("German");
        newLanguage.setAbbreviation("de");

        // Act
        content.setLanguage(newLanguage);

        // Assert
        assertThat(content.getLanguage()).isEqualTo(newLanguage);
    }

    @Test
    void setTitle_ShouldSetTitle() {
        // Arrange
        final String newTitle = "New Test Title";

        // Act
        content.setTitle(newTitle);

        // Assert
        assertThat(content.getTitle()).isEqualTo(newTitle);
    }

    @Test
    void setContent_ShouldSetContent() {
        // Arrange
        final String newContent = "New Test Content";

        // Act
        content.setContent(newContent);

        // Assert
        assertThat(content.getContent()).isEqualTo(newContent);
    }

    @Test
    void setShortDescription_ShouldSetShortDescription() {
        // Arrange
        final String newDescription = "New Test Description";

        // Act
        content.setShortDescription(newDescription);

        // Assert
        assertThat(content.getShortDescription()).isEqualTo(newDescription);
    }

    @Test
    void setKeywords_ShouldSetKeywords() {
        // Arrange
        final String newKeywords = "new,test,keywords";

        // Act
        content.setKeywords(newKeywords);

        // Assert
        assertThat(content.getKeywords()).isEqualTo(newKeywords);
    }
}

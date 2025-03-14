package de.muenchen.refarch.page;

import de.muenchen.refarch.language.Language;
import de.muenchen.refarch.link.Link;
import de.muenchen.refarch.page.content.PageContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PageTest {

    private Page page;
    private PageContent content;
    private Language language;

    @BeforeEach
    void setUp() {
        page = new Page();
        page.setCommentsEnabled(true);
        page.setThumbnail("test.jpg");

        language = new Language();
        language.setName("English");
        language.setAbbreviation("en");
        language.setFontAwesomeIcon("flag-usa");
        language.setMdiIcon("flag");

        content = new PageContent();
        content.setLanguage(language);
        content.setTitle("Test Title");
        content.setContent("Test Content");
    }

    @Test
    void addContent_ShouldAddContentToSetAndSetPage() {
        // Act
        page.addContent(content);

        // Assert
        assertThat(page.getContents()).contains(content);
        assertThat(content.getPage()).isEqualTo(page);
    }

    @Test
    void removeContent_ShouldRemoveContentFromSetAndUnsetPage() {
        // Arrange
        page.addContent(content);

        // Act
        page.removeContent(content);

        // Assert
        assertThat(page.getContents()).doesNotContain(content);
        assertThat(content.getPage()).isNull();
    }

    @Test
    void setLink_ShouldSetLink() {
        // Arrange
        Link link = new Link();
        link.setUrl("https://test.com");

        // Act
        page.setLink(link);

        // Assert
        assertThat(page.getLink()).isEqualTo(link);
    }

    @Test
    void setThumbnail_ShouldSetThumbnail() {
        // Arrange
        String thumbnail = "new-thumbnail.jpg";

        // Act
        page.setThumbnail(thumbnail);

        // Assert
        assertThat(page.getThumbnail()).isEqualTo(thumbnail);
    }

    @Test
    void setCommentsEnabled_ShouldSetCommentsEnabled() {
        // Act
        page.setCommentsEnabled(false);

        // Assert
        assertThat(page.isCommentsEnabled()).isFalse();
    }

    @Test
    void getContents_ShouldReturnEmptySetByDefault() {
        // Arrange
        Page newPage = new Page();

        // Assert
        assertThat(newPage.getContents()).isNotNull().isEmpty();
    }

    @Test
    void addMultipleContents_ShouldMaintainAllContents() {
        // Arrange
        PageContent content2 = new PageContent();
        content2.setLanguage(language);
        content2.setTitle("Second Title");
        content2.setContent("Second Content");

        // Act
        page.addContent(content);
        page.addContent(content2);

        // Assert
        assertThat(page.getContents())
                .hasSize(2)
                .contains(content, content2);
    }
}

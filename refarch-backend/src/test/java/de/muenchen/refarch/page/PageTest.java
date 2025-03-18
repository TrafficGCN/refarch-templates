package de.muenchen.refarch.page;

import de.muenchen.refarch.language.Language;
import de.muenchen.refarch.link.Link;
import de.muenchen.refarch.page.content.PageContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PageTest {

    private static final String TEST_THUMBNAIL = "test.jpg";
    private static final String ENGLISH = "English";
    private static final String EN = "en";
    private static final String FLAG_USA = "flag-usa";
    private static final String FLAG = "flag";
    private static final String TEST_TITLE = "Test Title";
    private static final String TEST_CONTENT = "Test Content";
    private static final String NEW_THUMBNAIL = "new-thumbnail.jpg";
    private static final String TEST_URL = "https://test.com";
    private static final String SECOND_TITLE = "Second Title";
    private static final String SECOND_CONTENT = "Second Content";

    private Page page;
    private final PageContent content = new PageContent();
    private final Language language = new Language();

    @BeforeEach
    void setUp() {
        page = new Page();
        page.setCommentsEnabled(true);
        page.setThumbnail(TEST_THUMBNAIL);

        language.setName(ENGLISH);
        language.setAbbreviation(EN);
        language.setFontAwesomeIcon(FLAG_USA);
        language.setMdiIcon(FLAG);

        content.setLanguage(language);
        content.setTitle(TEST_TITLE);
        content.setContent(TEST_CONTENT);
    }

    @Test
    void shouldAddContentToSetAndSetPage() {
        // Act
        page.addContent(content);

        // Assert
        assertThat(page.getContents()).contains(content);
        assertThat(content.getPage()).isEqualTo(page);
    }

    @Test
    void shouldRemoveContentFromSetAndUnsetPage() {
        // Arrange
        page.addContent(content);

        // Act
        page.removeContent(content);

        // Assert
        assertThat(page.getContents()).doesNotContain(content);
        assertThat(content.getPage()).isNull();
    }

    @Test
    void shouldUpdateLinkWhenSettingLink() {
        // Arrange
        final Link link = new Link();
        link.setUrl(TEST_URL);

        // Act
        page.setLink(link);

        // Assert
        assertThat(page.getLink()).isEqualTo(link);
    }

    @Test
    void shouldUpdateThumbnailWhenSettingThumbnail() {
        // Act
        page.setThumbnail(NEW_THUMBNAIL);

        // Assert
        assertThat(page.getThumbnail()).isEqualTo(NEW_THUMBNAIL);
    }

    @Test
    void shouldUpdateCommentsEnabledFlag() {
        // Act
        page.setCommentsEnabled(false);

        // Assert
        assertThat(page.isCommentsEnabled()).isFalse();
    }

    @Test
    void shouldReturnEmptySetByDefault() {
        // Arrange
        final Page newPage = new Page();

        // Assert
        assertThat(newPage.getContents()).isEmpty();
    }

    @Test
    void shouldMaintainAllContentsWhenAddingMultiple() {
        // Arrange
        final PageContent secondContent = new PageContent();
        secondContent.setLanguage(language);
        secondContent.setTitle(SECOND_TITLE);
        secondContent.setContent(SECOND_CONTENT);

        // Act
        page.addContent(content);
        page.addContent(secondContent);

        // Assert
        assertThat(page.getContents())
                .hasSize(2)
                .contains(content, secondContent);
        assertThat(content.getPage()).isEqualTo(page);
        assertThat(secondContent.getPage()).isEqualTo(page);
    }
}

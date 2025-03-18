package de.muenchen.refarch.page.content;

import de.muenchen.refarch.language.Language;
import de.muenchen.refarch.page.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PageContentTest {

    private PageContent pageContent;
    private Page page;
    private Language language;

    @BeforeEach
    void setUp() {
        pageContent = new PageContent();

        page = new Page();
        page.setCommentsEnabled(true);
        page.setThumbnail("test.jpg");

        language = new Language();
        language.setName("English");
        language.setAbbreviation("en");
        language.setFontAwesomeIcon("flag-usa");
        language.setMdiIcon("flag");
    }

    @Test
    void setPage_ShouldSetPage() {
        // Act
        pageContent.setPage(page);

        // Assert
        assertThat(pageContent.getPage()).isEqualTo(page);
    }

    @Test
    void setLanguage_ShouldSetLanguage() {
        // Act
        pageContent.setLanguage(language);

        // Assert
        assertThat(pageContent.getLanguage()).isEqualTo(language);
    }

    @Test
    void setTitle_ShouldSetTitle() {
        final String title = "Test Title";
        pageContent.setTitle(title);
        assertThat(pageContent.getTitle()).isEqualTo(title);
    }

    @Test
    void setContent_ShouldSetContent() {
        final String content = "Test Content";
        pageContent.setContent(content);
        assertThat(pageContent.getContent()).isEqualTo(content);
    }

    @Test
    void setShortDescription_ShouldSetShortDescription() {
        final String shortDescription = "Test Short Description";
        pageContent.setShortDescription(shortDescription);
        assertThat(pageContent.getShortDescription()).isEqualTo(shortDescription);
    }

    @Test
    void setKeywords_ShouldSetKeywords() {
        final String keywords = "test,keywords";
        pageContent.setKeywords(keywords);
        assertThat(pageContent.getKeywords()).isEqualTo(keywords);
    }

    @Test
    void newPageContent_ShouldHaveNullFields() {
        // Arrange
        final PageContent newPageContent = new PageContent();

        // Assert
        assertThat(newPageContent.getPage()).isNull();
        assertThat(newPageContent.getLanguage()).isNull();
        assertThat(newPageContent.getTitle()).isNull();
        assertThat(newPageContent.getContent()).isNull();
        assertThat(newPageContent.getShortDescription()).isNull();
        assertThat(newPageContent.getKeywords()).isNull();
    }

    @Test
    void setAllFields_ShouldSetAllFields() {
        // Arrange
        final String title = "Test Title";
        final String content = "Test Content";
        final String shortDescription = "Test Short Description";
        final String keywords = "test, keywords";

        // Act
        pageContent.setPage(page);
        pageContent.setLanguage(language);
        pageContent.setTitle(title);
        pageContent.setContent(content);
        pageContent.setShortDescription(shortDescription);
        pageContent.setKeywords(keywords);

        // Assert
        assertThat(pageContent.getPage()).isEqualTo(page);
        assertThat(pageContent.getLanguage()).isEqualTo(language);
        assertThat(pageContent.getTitle()).isEqualTo(title);
        assertThat(pageContent.getContent()).isEqualTo(content);
        assertThat(pageContent.getShortDescription()).isEqualTo(shortDescription);
        assertThat(pageContent.getKeywords()).isEqualTo(keywords);
    }
}

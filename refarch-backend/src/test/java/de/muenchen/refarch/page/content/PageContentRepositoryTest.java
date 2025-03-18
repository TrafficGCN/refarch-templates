package de.muenchen.refarch.page.content;

import de.muenchen.refarch.language.Language;
import de.muenchen.refarch.page.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class PageContentRepositoryTest {

    private static final String TEST_TITLE = "Test Title";
    private static final String TEST_CONTENT = "Test Content";
    private static final String SHORT_DESCRIPTION = "Short Description";
    private static final String KEYWORDS = "keywords";
    private static final String ENGLISH_LANGUAGE_NAME = "English";
    private static final String ENGLISH_LANGUAGE_ABBREV = "en";
    private static final String FLAG_USA_ICON = "flag-usa";
    private static final String FLAG_MDI_ICON = "flag";

    @Container
    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:16.0-alpine3.18");

    @Autowired
    private PageContentRepository pageContentRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Page page;
    private Language language;
    private LocalDateTime now;

    @DynamicPropertySource
    /* default */ static void setProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
    }

    @BeforeEach
    void setUp() {
        // Create and persist a page
        page = new Page();
        page.setCommentsEnabled(true);
        page = entityManager.persist(page);

        // Create and persist a language
        language = new Language();
        language.setName(ENGLISH_LANGUAGE_NAME);
        language.setAbbreviation(ENGLISH_LANGUAGE_ABBREV);
        language.setFontAwesomeIcon(FLAG_USA_ICON);
        language.setMdiIcon(FLAG_MDI_ICON);
        language = entityManager.persist(language);

        entityManager.flush();
    }

    @Test
    void findByPageIdAndLanguageId_WhenContentExists_ShouldReturnContent() {
        // Arrange
        final PageContent content = new PageContent();
        content.setPage(page);
        content.setLanguage(language);
        content.setTitle(TEST_TITLE);
        content.setContent(TEST_CONTENT);
        entityManager.persist(content);
        entityManager.flush();

        // Act
        final Optional<PageContent> foundContent = pageContentRepository.findByPageIdAndLanguageId(page.getId(), language.getId());

        // Assert
        assertThat(foundContent).isPresent();
        assertThat(foundContent.get().getTitle()).isEqualTo(TEST_TITLE);
        assertThat(foundContent.get().getContent()).isEqualTo(TEST_CONTENT);
    }

    @Test
    void findByPageIdAndLanguageId_WhenContentDoesNotExist_ShouldReturnEmpty() {
        // Act
        final Optional<PageContent> foundContent = pageContentRepository.findByPageIdAndLanguageId(page.getId(), UUID.randomUUID());

        // Assert
        assertThat(foundContent).isEmpty();
    }

    @Test
    void existsByPageIdAndLanguageId_WhenContentExists_ShouldReturnTrue() {
        // Arrange
        final PageContent content = new PageContent();
        content.setPage(page);
        content.setLanguage(language);
        content.setTitle(TEST_TITLE);
        content.setContent(TEST_CONTENT);
        entityManager.persist(content);
        entityManager.flush();

        // Act
        final boolean exists = pageContentRepository.existsByPageIdAndLanguageId(page.getId(), language.getId());

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByPageIdAndLanguageId_WhenContentDoesNotExist_ShouldReturnFalse() {
        // Act
        final boolean exists = pageContentRepository.existsByPageIdAndLanguageId(page.getId(), UUID.randomUUID());

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void save_ShouldPersistPageContent() {
        // Arrange
        final PageContent content = new PageContent();
        content.setPage(page);
        content.setLanguage(language);
        content.setTitle(TEST_TITLE);
        content.setContent(TEST_CONTENT);
        content.setShortDescription(SHORT_DESCRIPTION);
        content.setKeywords(KEYWORDS);

        // Act
        final PageContent savedContent = pageContentRepository.save(content);

        // Assert
        final PageContent foundContent = entityManager.find(PageContent.class, savedContent.getId());
        assertThat(foundContent).isNotNull();
        assertThat(foundContent.getTitle()).isEqualTo(TEST_TITLE);
        assertThat(foundContent.getContent()).isEqualTo(TEST_CONTENT);
        assertThat(foundContent.getShortDescription()).isEqualTo(SHORT_DESCRIPTION);
        assertThat(foundContent.getKeywords()).isEqualTo(KEYWORDS);
        assertThat(foundContent.getPage()).isEqualTo(page);
        assertThat(foundContent.getLanguage()).isEqualTo(language);
    }

    @Test
    void delete_ShouldRemovePageContent() {
        // Arrange
        final PageContent content = new PageContent();
        content.setPage(page);
        content.setLanguage(language);
        content.setTitle(TEST_TITLE);
        content.setContent(TEST_CONTENT);
        final PageContent persistedContent = entityManager.persist(content);
        entityManager.flush();

        // Act
        pageContentRepository.delete(persistedContent);
        entityManager.flush();

        // Assert
        final PageContent foundContent = entityManager.find(PageContent.class, persistedContent.getId());
        assertThat(foundContent).isNull();
    }
}

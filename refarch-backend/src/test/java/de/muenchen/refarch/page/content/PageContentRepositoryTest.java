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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class PageContentRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    private PageContentRepository pageContentRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Page page;
    private Language language;

    @BeforeEach
    void setUp() {
        // Create and persist a page
        page = new Page();
        page.setCommentsEnabled(true);
        page = entityManager.persist(page);

        // Create and persist a language
        language = new Language();
        language.setName("English");
        language.setAbbreviation("en");
        language.setFontAwesomeIcon("flag-usa");
        language.setMdiIcon("flag");
        language = entityManager.persist(language);

        entityManager.flush();
    }

    @Test
    void findByPageIdAndLanguageId_WhenContentExists_ShouldReturnContent() {
        // Arrange
        PageContent content = new PageContent();
        content.setPage(page);
        content.setLanguage(language);
        content.setTitle("Test Title");
        content.setContent("Test Content");
        content = entityManager.persist(content);
        entityManager.flush();

        // Act
        var foundContent = pageContentRepository.findByPageIdAndLanguageId(page.getId(), language.getId());

        // Assert
        assertThat(foundContent).isPresent();
        assertThat(foundContent.get().getTitle()).isEqualTo("Test Title");
        assertThat(foundContent.get().getContent()).isEqualTo("Test Content");
    }

    @Test
    void findByPageIdAndLanguageId_WhenContentDoesNotExist_ShouldReturnEmpty() {
        // Act
        var foundContent = pageContentRepository.findByPageIdAndLanguageId(page.getId(), UUID.randomUUID());

        // Assert
        assertThat(foundContent).isEmpty();
    }

    @Test
    void existsByPageIdAndLanguageId_WhenContentExists_ShouldReturnTrue() {
        // Arrange
        PageContent content = new PageContent();
        content.setPage(page);
        content.setLanguage(language);
        content.setTitle("Test Title");
        content.setContent("Test Content");
        entityManager.persist(content);
        entityManager.flush();

        // Act
        boolean exists = pageContentRepository.existsByPageIdAndLanguageId(page.getId(), language.getId());

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByPageIdAndLanguageId_WhenContentDoesNotExist_ShouldReturnFalse() {
        // Act
        boolean exists = pageContentRepository.existsByPageIdAndLanguageId(page.getId(), UUID.randomUUID());

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void save_ShouldPersistPageContent() {
        // Arrange
        PageContent content = new PageContent();
        content.setPage(page);
        content.setLanguage(language);
        content.setTitle("Test Title");
        content.setContent("Test Content");
        content.setShortDescription("Short Description");
        content.setKeywords("keywords");

        // Act
        PageContent savedContent = pageContentRepository.save(content);

        // Assert
        PageContent foundContent = entityManager.find(PageContent.class, savedContent.getId());
        assertThat(foundContent).isNotNull();
        assertThat(foundContent.getTitle()).isEqualTo("Test Title");
        assertThat(foundContent.getContent()).isEqualTo("Test Content");
        assertThat(foundContent.getShortDescription()).isEqualTo("Short Description");
        assertThat(foundContent.getKeywords()).isEqualTo("keywords");
        assertThat(foundContent.getPage()).isEqualTo(page);
        assertThat(foundContent.getLanguage()).isEqualTo(language);
    }

    @Test
    void delete_ShouldRemovePageContent() {
        // Arrange
        PageContent content = new PageContent();
        content.setPage(page);
        content.setLanguage(language);
        content.setTitle("Test Title");
        content.setContent("Test Content");
        content = entityManager.persist(content);
        entityManager.flush();

        // Act
        pageContentRepository.delete(content);
        entityManager.flush();

        // Assert
        PageContent foundContent = entityManager.find(PageContent.class, content.getId());
        assertThat(foundContent).isNull();
    }
}

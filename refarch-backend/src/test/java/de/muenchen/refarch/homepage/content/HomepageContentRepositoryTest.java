package de.muenchen.refarch.homepage.content;

import de.muenchen.refarch.language.Language;
import de.muenchen.refarch.homepage.Homepage;
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
class HomepageContentRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    private HomepageContentRepository homepageContentRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Homepage homepage;
    private Language language;

    @BeforeEach
    void setUp() {
        // Create and persist a homepage
        homepage = new Homepage();
        homepage.setThumbnail("test.jpg");
        homepage = entityManager.persist(homepage);

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
    void findByHomepageIdAndLanguageId_WhenContentExists_ShouldReturnContent() {
        // Arrange
        HomepageContent content = new HomepageContent();
        content.setHomepage(homepage);
        content.setLanguage(language);
        content.setWelcomeMessage("Welcome");
        content.setWelcomeMessageExtended("Extended Welcome");
        content.setExploreOurWork("Explore Our Work");
        content = entityManager.persist(content);
        entityManager.flush();

        // Act
        var foundContent = homepageContentRepository.findByHomepageIdAndLanguageId(homepage.getId(), language.getId());

        // Assert
        assertThat(foundContent).isPresent();
        assertThat(foundContent.get().getWelcomeMessage()).isEqualTo("Welcome");
        assertThat(foundContent.get().getWelcomeMessageExtended()).isEqualTo("Extended Welcome");
        assertThat(foundContent.get().getExploreOurWork()).isEqualTo("Explore Our Work");
    }

    @Test
    void findByHomepageIdAndLanguageId_WhenContentDoesNotExist_ShouldReturnEmpty() {
        // Act
        var foundContent = homepageContentRepository.findByHomepageIdAndLanguageId(homepage.getId(), UUID.randomUUID());

        // Assert
        assertThat(foundContent).isEmpty();
    }

    @Test
    void existsByHomepageIdAndLanguageId_WhenContentExists_ShouldReturnTrue() {
        // Arrange
        HomepageContent content = new HomepageContent();
        content.setHomepage(homepage);
        content.setLanguage(language);
        content.setWelcomeMessage("Welcome");
        entityManager.persist(content);
        entityManager.flush();

        // Act
        boolean exists = homepageContentRepository.existsByHomepageIdAndLanguageId(homepage.getId(), language.getId());

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByHomepageIdAndLanguageId_WhenContentDoesNotExist_ShouldReturnFalse() {
        // Act
        boolean exists = homepageContentRepository.existsByHomepageIdAndLanguageId(homepage.getId(), UUID.randomUUID());

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void save_ShouldPersistHomepageContent() {
        // Arrange
        HomepageContent content = new HomepageContent();
        content.setHomepage(homepage);
        content.setLanguage(language);
        content.setWelcomeMessage("Welcome");
        content.setWelcomeMessageExtended("Extended Welcome");
        content.setExploreOurWork("Explore Our Work");
        content.setGetInvolved("Get Involved");
        content.setImportantLinks("Important Links");
        content.setEcosystemLinks("Ecosystem Links");
        content.setBlog("Blog");
        content.setPapers("Papers");
        content.setReadMore("Read More");

        // Act
        HomepageContent savedContent = homepageContentRepository.save(content);

        // Assert
        HomepageContent foundContent = entityManager.find(HomepageContent.class, savedContent.getId());
        assertThat(foundContent).isNotNull();
        assertThat(foundContent.getWelcomeMessage()).isEqualTo("Welcome");
        assertThat(foundContent.getWelcomeMessageExtended()).isEqualTo("Extended Welcome");
        assertThat(foundContent.getExploreOurWork()).isEqualTo("Explore Our Work");
        assertThat(foundContent.getGetInvolved()).isEqualTo("Get Involved");
        assertThat(foundContent.getImportantLinks()).isEqualTo("Important Links");
        assertThat(foundContent.getEcosystemLinks()).isEqualTo("Ecosystem Links");
        assertThat(foundContent.getBlog()).isEqualTo("Blog");
        assertThat(foundContent.getPapers()).isEqualTo("Papers");
        assertThat(foundContent.getReadMore()).isEqualTo("Read More");
        assertThat(foundContent.getHomepage()).isEqualTo(homepage);
        assertThat(foundContent.getLanguage()).isEqualTo(language);
    }

    @Test
    void delete_ShouldRemoveHomepageContent() {
        // Arrange
        HomepageContent content = new HomepageContent();
        content.setHomepage(homepage);
        content.setLanguage(language);
        content.setWelcomeMessage("Welcome");
        content = entityManager.persist(content);
        entityManager.flush();

        // Act
        homepageContentRepository.delete(content);
        entityManager.flush();

        // Assert
        HomepageContent foundContent = entityManager.find(HomepageContent.class, content.getId());
        assertThat(foundContent).isNull();
    }
}

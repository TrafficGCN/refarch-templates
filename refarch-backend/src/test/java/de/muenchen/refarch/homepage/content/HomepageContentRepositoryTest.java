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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class HomepageContentRepositoryTest {

    private static final String TEST_THUMBNAIL = "test.jpg";
    private static final String ENGLISH_LANGUAGE_NAME = "English";
    private static final String ENGLISH_LANGUAGE_ABBREV = "en";
    private static final String FLAG_USA_ICON = "flag-usa";
    private static final String FLAG_MDI_ICON = "flag";
    private static final String WELCOME_MESSAGE = "Welcome";
    private static final String WELCOME_MESSAGE_EXTENDED = "Extended Welcome";
    private static final String EXPLORE_OUR_WORK = "Explore Our Work";
    private static final String GET_INVOLVED = "Get Involved";
    private static final String IMPORTANT_LINKS = "Important Links";
    private static final String ECOSYSTEM_LINKS = "Ecosystem Links";
    private static final String BLOG = "Blog";
    private static final String PAPERS = "Papers";
    private static final String READ_MORE = "Read More";
    private static final String NEW_WELCOME_MESSAGE = "New Welcome Message";
    private static final String NEW_WELCOME_MESSAGE_EXTENDED = "New Extended Welcome Message";

    @Container
    /* default */ static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:16.0-alpine3.18");

    @Autowired
    private HomepageContentRepository homepageContentRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Homepage homepage;
    private Language language;

    @DynamicPropertySource
    /* default */ static void setProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
    }

    @BeforeEach
    void setUp() {
        // Create and persist a homepage
        homepage = new Homepage();
        homepage.setThumbnail(TEST_THUMBNAIL);
        homepage = entityManager.persist(homepage);

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
    void findByHomepageIdAndLanguageId_WhenContentExists_ShouldReturnContent() {
        // Arrange
        final HomepageContent content = new HomepageContent();
        content.setHomepage(homepage);
        content.setLanguage(language);
        content.setWelcomeMessage(WELCOME_MESSAGE);
        content.setWelcomeMessageExtended(WELCOME_MESSAGE_EXTENDED);
        content.setExploreOurWork(EXPLORE_OUR_WORK);
        entityManager.persist(content);
        entityManager.flush();

        // Act
        final Optional<HomepageContent> foundContent = homepageContentRepository.findByHomepageIdAndLanguageId(homepage.getId(), language.getId());

        // Assert
        assertThat(foundContent).isPresent();
        assertThat(foundContent.get().getWelcomeMessage()).isEqualTo(WELCOME_MESSAGE);
        assertThat(foundContent.get().getWelcomeMessageExtended()).isEqualTo(WELCOME_MESSAGE_EXTENDED);
        assertThat(foundContent.get().getExploreOurWork()).isEqualTo(EXPLORE_OUR_WORK);
    }

    @Test
    void findByHomepageIdAndLanguageId_WhenContentDoesNotExist_ShouldReturnEmpty() {
        // Act
        final Optional<HomepageContent> foundContent = homepageContentRepository.findByHomepageIdAndLanguageId(homepage.getId(), UUID.randomUUID());

        // Assert
        assertThat(foundContent).isEmpty();
    }

    @Test
    void existsByHomepageIdAndLanguageId_WhenContentExists_ShouldReturnTrue() {
        // Arrange
        final HomepageContent content = new HomepageContent();
        content.setHomepage(homepage);
        content.setLanguage(language);
        content.setWelcomeMessage(WELCOME_MESSAGE);
        entityManager.persist(content);
        entityManager.flush();

        // Act
        final boolean exists = homepageContentRepository.existsByHomepageIdAndLanguageId(homepage.getId(), language.getId());

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByHomepageIdAndLanguageId_WhenContentDoesNotExist_ShouldReturnFalse() {
        // Act
        final boolean exists = homepageContentRepository.existsByHomepageIdAndLanguageId(homepage.getId(), UUID.randomUUID());

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void save_ShouldPersistHomepageContent() {
        // Arrange
        final HomepageContent content = new HomepageContent();
        content.setHomepage(homepage);
        content.setLanguage(language);
        content.setWelcomeMessage(WELCOME_MESSAGE);
        content.setWelcomeMessageExtended(WELCOME_MESSAGE_EXTENDED);
        content.setExploreOurWork(EXPLORE_OUR_WORK);
        content.setGetInvolved(GET_INVOLVED);
        content.setImportantLinks(IMPORTANT_LINKS);
        content.setEcosystemLinks(ECOSYSTEM_LINKS);
        content.setBlog(BLOG);
        content.setPapers(PAPERS);
        content.setReadMore(READ_MORE);

        // Act
        final HomepageContent savedContent = homepageContentRepository.save(content);

        // Assert
        final HomepageContent foundContent = entityManager.find(HomepageContent.class, savedContent.getId());
        assertThat(foundContent).isNotNull();
        assertThat(foundContent.getWelcomeMessage()).isEqualTo(WELCOME_MESSAGE);
        assertThat(foundContent.getWelcomeMessageExtended()).isEqualTo(WELCOME_MESSAGE_EXTENDED);
        assertThat(foundContent.getExploreOurWork()).isEqualTo(EXPLORE_OUR_WORK);
        assertThat(foundContent.getGetInvolved()).isEqualTo(GET_INVOLVED);
        assertThat(foundContent.getImportantLinks()).isEqualTo(IMPORTANT_LINKS);
        assertThat(foundContent.getEcosystemLinks()).isEqualTo(ECOSYSTEM_LINKS);
        assertThat(foundContent.getBlog()).isEqualTo(BLOG);
        assertThat(foundContent.getPapers()).isEqualTo(PAPERS);
        assertThat(foundContent.getReadMore()).isEqualTo(READ_MORE);
        assertThat(foundContent.getHomepage()).isEqualTo(homepage);
        assertThat(foundContent.getLanguage()).isEqualTo(language);
    }

    @Test
    void delete_ShouldRemoveHomepageContent() {
        // Arrange
        final HomepageContent content = new HomepageContent();
        content.setHomepage(homepage);
        content.setLanguage(language);
        content.setWelcomeMessage(WELCOME_MESSAGE);
        entityManager.persist(content);
        entityManager.flush();

        // Act
        homepageContentRepository.delete(content);
        entityManager.flush();

        // Assert
        final HomepageContent foundContent = entityManager.find(HomepageContent.class, content.getId());
        assertThat(foundContent).isNull();
    }

    @Test
    void whenCheckingExistsByLanguageId_shouldReturnTrue() {
        // Arrange
        final HomepageContent content = new HomepageContent();
        content.setHomepage(homepage);
        content.setLanguage(language);
        content.setWelcomeMessage(WELCOME_MESSAGE);
        entityManager.persist(content);
        entityManager.flush();

        // Act
        final boolean exists = homepageContentRepository.existsByHomepageIdAndLanguageId(homepage.getId(), language.getId());

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void whenCheckingNonexistentLanguageId_shouldReturnFalse() {
        // Act
        final boolean exists = homepageContentRepository.existsByHomepageIdAndLanguageId(homepage.getId(), UUID.randomUUID());

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void whenSavingNewContent_shouldCreateContent() {
        // Arrange
        final HomepageContent newContent = new HomepageContent();
        newContent.setHomepage(homepage);
        newContent.setLanguage(language);
        newContent.setWelcomeMessage(NEW_WELCOME_MESSAGE);
        newContent.setWelcomeMessageExtended(NEW_WELCOME_MESSAGE_EXTENDED);
        newContent.setExploreOurWork(EXPLORE_OUR_WORK);
        newContent.setGetInvolved(GET_INVOLVED);
        newContent.setImportantLinks(IMPORTANT_LINKS);
        newContent.setEcosystemLinks(ECOSYSTEM_LINKS);
        newContent.setBlog(BLOG);
        newContent.setPapers(PAPERS);
        newContent.setReadMore(READ_MORE);

        // Act
        final HomepageContent savedContent = homepageContentRepository.save(newContent);
        entityManager.flush();

        // Assert
        final HomepageContent foundContent = entityManager.find(HomepageContent.class, savedContent.getId());
        assertThat(foundContent).isNotNull();
        assertThat(foundContent.getWelcomeMessage()).isEqualTo(NEW_WELCOME_MESSAGE);
        assertThat(foundContent.getWelcomeMessageExtended()).isEqualTo(NEW_WELCOME_MESSAGE_EXTENDED);
        assertThat(foundContent.getExploreOurWork()).isEqualTo(EXPLORE_OUR_WORK);
        assertThat(foundContent.getGetInvolved()).isEqualTo(GET_INVOLVED);
        assertThat(foundContent.getImportantLinks()).isEqualTo(IMPORTANT_LINKS);
        assertThat(foundContent.getEcosystemLinks()).isEqualTo(ECOSYSTEM_LINKS);
        assertThat(foundContent.getBlog()).isEqualTo(BLOG);
        assertThat(foundContent.getPapers()).isEqualTo(PAPERS);
        assertThat(foundContent.getReadMore()).isEqualTo(READ_MORE);
        assertThat(foundContent.getLanguage()).isEqualTo(language);
    }

    @Test
    void whenFindingByLanguageId_shouldReturnContent() {
        // Arrange
        final HomepageContent content = new HomepageContent();
        content.setHomepage(homepage);
        content.setLanguage(language);
        content.setWelcomeMessage(WELCOME_MESSAGE);
        content.setWelcomeMessageExtended(WELCOME_MESSAGE_EXTENDED);
        content.setExploreOurWork(EXPLORE_OUR_WORK);
        content.setGetInvolved(GET_INVOLVED);
        content.setImportantLinks(IMPORTANT_LINKS);
        content.setEcosystemLinks(ECOSYSTEM_LINKS);
        content.setBlog(BLOG);
        content.setPapers(PAPERS);
        content.setReadMore(READ_MORE);
        entityManager.persist(content);
        entityManager.flush();

        // Act
        final Optional<HomepageContent> foundContent = homepageContentRepository.findByHomepageIdAndLanguageId(homepage.getId(), language.getId());

        // Assert
        assertThat(foundContent).isPresent();
        assertThat(foundContent.get().getWelcomeMessage()).isEqualTo(WELCOME_MESSAGE);
        assertThat(foundContent.get().getWelcomeMessageExtended()).isEqualTo(WELCOME_MESSAGE_EXTENDED);
        assertThat(foundContent.get().getExploreOurWork()).isEqualTo(EXPLORE_OUR_WORK);
        assertThat(foundContent.get().getGetInvolved()).isEqualTo(GET_INVOLVED);
        assertThat(foundContent.get().getImportantLinks()).isEqualTo(IMPORTANT_LINKS);
        assertThat(foundContent.get().getEcosystemLinks()).isEqualTo(ECOSYSTEM_LINKS);
        assertThat(foundContent.get().getBlog()).isEqualTo(BLOG);
        assertThat(foundContent.get().getPapers()).isEqualTo(PAPERS);
        assertThat(foundContent.get().getReadMore()).isEqualTo(READ_MORE);
        assertThat(foundContent.get().getLanguage()).isEqualTo(language);
    }
}

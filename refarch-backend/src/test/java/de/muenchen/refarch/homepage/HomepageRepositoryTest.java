package de.muenchen.refarch.homepage;

import de.muenchen.refarch.homepage.content.HomepageContent;
import de.muenchen.refarch.homepage.content.HomepageContentRepository;
import de.muenchen.refarch.language.Language;
import de.muenchen.refarch.link.Link;
import de.muenchen.refarch.link.LinkScope;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class HomepageRepositoryTest {

    @Container
    /* default */ static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    private HomepageRepository homepageRepository;

    @Autowired
    private HomepageContentRepository homepageContentRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Homepage homepage;
    private Link link;
    private Language language;
    private HomepageContent content;
    private LocalDateTime now;

    @DynamicPropertySource
    /* default */ static void setProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
    }

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        // Create and persist a link
        link = new Link();
        link.setUrl("https://example.com");
        link.setName("Example Link");
        link.setScope(LinkScope.EXTERNAL);
        link = entityManager.persist(link);

        // Create and persist a language
        language = new Language();
        language.setName("English");
        language.setAbbreviation("en");
        language.setFontAwesomeIcon("flag-usa");
        language.setMdiIcon("flag");
        language = entityManager.persist(language);

        // Create and persist a homepage
        homepage = new Homepage();
        homepage.setLink(link);
        homepage.setThumbnail("thumbnail.jpg");
        homepage.setCreatedAt(now);
        homepage.setUpdatedAt(now);
        homepage = entityManager.persist(homepage);

        // Create and persist homepage content
        content = new HomepageContent();
        content.setWelcomeMessage("Welcome");
        content.setWelcomeMessageExtended("Extended Welcome");
        content.setExploreOurWork("Explore Our Work");
        content.setGetInvolved("Get Involved");
        content.setImportantLinks("Important Links");
        content.setEcosystemLinks("Ecosystem Links");
        content.setBlog("Blog");
        content.setPapers("Papers");
        content.setReadMore("Read More");
        content.setCreatedAt(now);
        content.setUpdatedAt(now);

        // Establish bidirectional relationship
        homepage.addContent(content);
        content.setLanguage(language);
        entityManager.persist(content);
        entityManager.flush();
    }

    @Test
    void findById_ShouldReturnHomepage() {
        // Act
        final Optional<Homepage> foundHomepage = homepageRepository.findById(homepage.getId());

        // Assert
        assertThat(foundHomepage).isPresent();
        assertThat(foundHomepage.get().getId()).isEqualTo(homepage.getId());
        assertThat(foundHomepage.get().getLink()).isEqualTo(link);
        assertThat(foundHomepage.get().getThumbnail()).isEqualTo("thumbnail.jpg");
        assertThat(foundHomepage.get().getContents()).hasSize(1);
        assertThat(foundHomepage.get().getCreatedAt()).isNotNull();
        assertThat(foundHomepage.get().getUpdatedAt()).isNotNull();
    }

    @Test
    void save_ShouldCreateHomepage() {
        // Arrange
        final Homepage newHomepage = new Homepage();
        newHomepage.setLink(link);
        newHomepage.setThumbnail("new-thumbnail.jpg");
        newHomepage.setCreatedAt(now);
        newHomepage.setUpdatedAt(now);

        // Act
        final Homepage savedHomepage = homepageRepository.save(newHomepage);

        // Assert
        final Homepage foundHomepage = entityManager.find(Homepage.class, savedHomepage.getId());
        assertThat(foundHomepage).isNotNull();
        assertThat(foundHomepage.getLink()).isEqualTo(link);
        assertThat(foundHomepage.getThumbnail()).isEqualTo("new-thumbnail.jpg");
        assertThat(foundHomepage.getCreatedAt()).isEqualTo(now);
        assertThat(foundHomepage.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void delete_ShouldRemoveHomepageAndContent() {
        // Act
        homepageRepository.delete(homepage);
        entityManager.flush();

        // Assert
        final Homepage foundHomepage = entityManager.find(Homepage.class, homepage.getId());
        final HomepageContent foundContent = entityManager.find(HomepageContent.class, content.getId());
        final Link foundLink = entityManager.find(Link.class, link.getId());
        final Language foundLanguage = entityManager.find(Language.class, language.getId());

        assertThat(foundHomepage).isNull();
        assertThat(foundContent).isNull();
        assertThat(foundLink).isNotNull();
        assertThat(foundLanguage).isNotNull();
    }
}

package de.muenchen.refarch.link;

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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class LinkRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:16.0-alpine3.18");

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Link link;

    @DynamicPropertySource
    /* default */ static void setProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
    }

    @BeforeEach
    void setUp() {
        // Create and persist a link
        link = new Link();
        link.setUrl("https://example.com");
        link.setName("Example Link");
        link.setScope(LinkScope.EXTERNAL);
        link.setFontAwesomeIcon("fa-link");
        link.setMdiIcon("mdi-link");
        link.setType("navigation");
        link = entityManager.persist(link);
        entityManager.flush();
    }

    @Test
    void findById_ShouldReturnLink() {
        // Act
        final Optional<Link> foundLink = linkRepository.findById(link.getId());

        // Assert
        assertThat(foundLink).isPresent();
        assertThat(foundLink.get().getId()).isEqualTo(link.getId());
        assertThat(foundLink.get().getUrl()).isEqualTo("https://example.com");
        assertThat(foundLink.get().getName()).isEqualTo("Example Link");
        assertThat(foundLink.get().getScope()).isEqualTo(LinkScope.EXTERNAL);
        assertThat(foundLink.get().getFontAwesomeIcon()).isEqualTo("fa-link");
        assertThat(foundLink.get().getMdiIcon()).isEqualTo("mdi-link");
        assertThat(foundLink.get().getType()).isEqualTo("navigation");
    }

    @Test
    void findById_WhenLinkDoesNotExist_ShouldReturnEmpty() {
        // Act
        final Optional<Link> foundLink = linkRepository.findById(UUID.randomUUID());

        // Assert
        assertThat(foundLink).isEmpty();
    }

    @Test
    void findByScope_ShouldReturnLinks() {
        // Arrange
        final Link internalLink = new Link();
        internalLink.setUrl("/internal");
        internalLink.setName("Internal Link");
        internalLink.setScope(LinkScope.INTERNAL);
        internalLink.setFontAwesomeIcon("fa-internal");
        internalLink.setMdiIcon("mdi-internal");
        internalLink.setType("internal");
        entityManager.persist(internalLink);
        entityManager.flush();

        // Act
        final List<Link> externalLinks = linkRepository.findByScope(LinkScope.EXTERNAL);
        final List<Link> internalLinks = linkRepository.findByScope(LinkScope.INTERNAL);

        // Assert
        assertThat(externalLinks)
                .hasSize(1)
                .extracting(Link::getName)
                .containsExactly("Example Link");

        assertThat(internalLinks)
                .hasSize(1)
                .extracting(Link::getName)
                .containsExactly("Internal Link");
    }

    @Test
    void save_ShouldCreateLink() {
        // Arrange
        final Link newLink = new Link();
        newLink.setUrl("https://new-example.com");
        newLink.setName("New Link");
        newLink.setScope(LinkScope.EXTERNAL);
        newLink.setFontAwesomeIcon("fa-external-link");
        newLink.setMdiIcon("mdi-external-link");
        newLink.setType("external");

        // Act
        final Link savedLink = linkRepository.save(newLink);

        // Assert
        final Link foundLink = entityManager.find(Link.class, savedLink.getId());
        assertThat(foundLink).isNotNull();
        assertThat(foundLink.getUrl()).isEqualTo("https://new-example.com");
        assertThat(foundLink.getName()).isEqualTo("New Link");
        assertThat(foundLink.getScope()).isEqualTo(LinkScope.EXTERNAL);
        assertThat(foundLink.getFontAwesomeIcon()).isEqualTo("fa-external-link");
        assertThat(foundLink.getMdiIcon()).isEqualTo("mdi-external-link");
        assertThat(foundLink.getType()).isEqualTo("external");
    }

    @Test
    void update_ShouldUpdateLink() {
        // Arrange
        link.setUrl("https://updated-example.com");
        link.setName("Updated Link");
        link.setScope(LinkScope.INTERNAL);
        link.setFontAwesomeIcon("fa-internal-link");
        link.setMdiIcon("mdi-internal-link");
        link.setType("internal");

        // Act
        final Link updatedLink = linkRepository.save(link);

        // Assert
        final Link foundLink = entityManager.find(Link.class, updatedLink.getId());
        assertThat(foundLink).isNotNull();
        assertThat(foundLink.getUrl()).isEqualTo("https://updated-example.com");
        assertThat(foundLink.getName()).isEqualTo("Updated Link");
        assertThat(foundLink.getScope()).isEqualTo(LinkScope.INTERNAL);
        assertThat(foundLink.getFontAwesomeIcon()).isEqualTo("fa-internal-link");
        assertThat(foundLink.getMdiIcon()).isEqualTo("mdi-internal-link");
        assertThat(foundLink.getType()).isEqualTo("internal");
    }

    @Test
    void delete_ShouldRemoveLink() {
        // Act
        linkRepository.delete(link);
        entityManager.flush();

        // Assert
        final Link foundLink = entityManager.find(Link.class, link.getId());
        assertThat(foundLink).isNull();
    }

    @Test
    void findAll_ShouldReturnAllLinks() {
        // Arrange
        final Link firstLink = new Link();
        firstLink.setUrl("https://test1.com");
        firstLink.setScope(LinkScope.INTERNAL);
        entityManager.persist(firstLink);

        final Link secondLink = new Link();
        secondLink.setUrl("https://test2.com");
        secondLink.setScope(LinkScope.EXTERNAL);
        entityManager.persist(secondLink);
        entityManager.flush();

        // Act
        final List<Link> allLinks = linkRepository.findAll();

        // Assert
        assertThat(allLinks).hasSize(3);
        assertThat(allLinks).containsExactlyInAnyOrder(link, firstLink, secondLink);
    }
}

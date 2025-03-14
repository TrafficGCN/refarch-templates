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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class LinkRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Link link;

    @BeforeEach
    void setUp() {
        // Create and persist a link
        link = new Link();
        link.setUrl("https://example.com");
        link.setName("Example Link");
        link.setScope(LinkScope.external);
        link.setFontAwesomeIcon("fa-link");
        link.setMdiIcon("mdi-link");
        link.setType("navigation");
        link = entityManager.persist(link);
        entityManager.flush();
    }

    @Test
    void findById_ShouldReturnLink() {
        // Act
        var foundLink = linkRepository.findById(link.getId());

        // Assert
        assertThat(foundLink).isPresent();
        assertThat(foundLink.get().getId()).isEqualTo(link.getId());
        assertThat(foundLink.get().getUrl()).isEqualTo("https://example.com");
        assertThat(foundLink.get().getName()).isEqualTo("Example Link");
        assertThat(foundLink.get().getScope()).isEqualTo(LinkScope.external);
        assertThat(foundLink.get().getFontAwesomeIcon()).isEqualTo("fa-link");
        assertThat(foundLink.get().getMdiIcon()).isEqualTo("mdi-link");
        assertThat(foundLink.get().getType()).isEqualTo("navigation");
    }

    @Test
    void findById_WhenLinkDoesNotExist_ShouldReturnEmpty() {
        // Act
        var foundLink = linkRepository.findById(UUID.randomUUID());

        // Assert
        assertThat(foundLink).isEmpty();
    }

    @Test
    void findByScope_ShouldReturnLinks() {
        // Arrange
        Link internalLink = new Link();
        internalLink.setUrl("/internal");
        internalLink.setName("Internal Link");
        internalLink.setScope(LinkScope.internal);
        internalLink.setFontAwesomeIcon("fa-internal");
        internalLink.setMdiIcon("mdi-internal");
        internalLink.setType("internal");
        entityManager.persist(internalLink);
        entityManager.flush();

        // Act
        List<Link> externalLinks = linkRepository.findByScope(LinkScope.external);
        List<Link> internalLinks = linkRepository.findByScope(LinkScope.internal);

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
        Link newLink = new Link();
        newLink.setUrl("https://new-example.com");
        newLink.setName("New Link");
        newLink.setScope(LinkScope.external);
        newLink.setFontAwesomeIcon("fa-external-link");
        newLink.setMdiIcon("mdi-external-link");
        newLink.setType("external");

        // Act
        Link savedLink = linkRepository.save(newLink);

        // Assert
        Link foundLink = entityManager.find(Link.class, savedLink.getId());
        assertThat(foundLink).isNotNull();
        assertThat(foundLink.getUrl()).isEqualTo("https://new-example.com");
        assertThat(foundLink.getName()).isEqualTo("New Link");
        assertThat(foundLink.getScope()).isEqualTo(LinkScope.external);
        assertThat(foundLink.getFontAwesomeIcon()).isEqualTo("fa-external-link");
        assertThat(foundLink.getMdiIcon()).isEqualTo("mdi-external-link");
        assertThat(foundLink.getType()).isEqualTo("external");
    }

    @Test
    void update_ShouldUpdateLink() {
        // Arrange
        link.setUrl("https://updated-example.com");
        link.setName("Updated Link");
        link.setScope(LinkScope.internal);
        link.setFontAwesomeIcon("fa-internal-link");
        link.setMdiIcon("mdi-internal-link");
        link.setType("internal");

        // Act
        Link updatedLink = linkRepository.save(link);

        // Assert
        Link foundLink = entityManager.find(Link.class, updatedLink.getId());
        assertThat(foundLink).isNotNull();
        assertThat(foundLink.getUrl()).isEqualTo("https://updated-example.com");
        assertThat(foundLink.getName()).isEqualTo("Updated Link");
        assertThat(foundLink.getScope()).isEqualTo(LinkScope.internal);
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
        Link foundLink = entityManager.find(Link.class, link.getId());
        assertThat(foundLink).isNull();
    }

    @Test
    void findAll_ShouldReturnAllLinks() {
        // Arrange
        Link secondLink = new Link();
        secondLink.setUrl("https://second-example.com");
        secondLink.setName("Second Link");
        secondLink.setScope(LinkScope.internal);
        secondLink.setFontAwesomeIcon("fa-second-link");
        secondLink.setMdiIcon("mdi-second-link");
        secondLink.setType("navigation");
        entityManager.persist(secondLink);
        entityManager.flush();

        // Act
        List<Link> allLinks = linkRepository.findAll();

        // Assert
        assertThat(allLinks).hasSize(2);
        assertThat(allLinks).extracting(Link::getName)
                .containsExactlyInAnyOrder("Example Link", "Second Link");
    }
}

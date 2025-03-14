package de.muenchen.refarch.page;

import de.muenchen.refarch.link.Link;
import de.muenchen.refarch.link.LinkScope;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
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
class PageRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void existsByLinkId_WhenLinkExists_ShouldReturnTrue() {
        // Arrange
        Link link = new Link();
        link.setUrl("https://test.com");
        link.setScope(LinkScope.internal);
        link = entityManager.persist(link);

        Page page = new Page();
        page.setLink(link);
        page.setCommentsEnabled(true);
        entityManager.persist(page);
        entityManager.flush();

        // Act
        boolean exists = pageRepository.existsByLinkId(link.getId());

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByLinkId_WhenLinkDoesNotExist_ShouldReturnFalse() {
        // Act
        boolean exists = pageRepository.existsByLinkId(UUID.randomUUID());

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void save_ShouldPersistPage() {
        // Arrange
        Page page = new Page();
        page.setThumbnail("thumbnail.jpg");
        page.setCommentsEnabled(true);

        // Act
        Page savedPage = pageRepository.save(page);

        // Assert
        Page foundPage = entityManager.find(Page.class, savedPage.getId());
        assertThat(foundPage).isNotNull();
        assertThat(foundPage.getThumbnail()).isEqualTo("thumbnail.jpg");
        assertThat(foundPage.isCommentsEnabled()).isTrue();
    }

    @Test
    void findById_WhenPageExists_ShouldReturnPage() {
        // Arrange
        Page page = new Page();
        page.setThumbnail("thumbnail.jpg");
        page.setCommentsEnabled(true);
        page = entityManager.persist(page);
        entityManager.flush();

        // Act
        var foundPage = pageRepository.findById(page.getId());

        // Assert
        assertThat(foundPage).isPresent();
        assertThat(foundPage.get().getThumbnail()).isEqualTo("thumbnail.jpg");
        assertThat(foundPage.get().isCommentsEnabled()).isTrue();
    }

    @Test
    void findById_WhenPageDoesNotExist_ShouldReturnEmpty() {
        // Act
        var foundPage = pageRepository.findById(UUID.randomUUID());

        // Assert
        assertThat(foundPage).isEmpty();
    }
}

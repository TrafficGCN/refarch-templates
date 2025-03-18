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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class PageRepositoryTest {

    private static final String THUMBNAIL_PATH = "thumbnail.jpg";

    @Container
    /* default */ static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private TestEntityManager entityManager;

    @DynamicPropertySource
    /* default */ static void setProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
    }

    @Test
    void existsByLinkId_WhenLinkExists_ShouldReturnTrue() {
        // Arrange
        final Link link = new Link();
        link.setUrl("https://test.com");
        link.setScope(LinkScope.INTERNAL);
        final Link persistedLink = entityManager.persist(link);

        final Page page = new Page();
        page.setLink(persistedLink);
        page.setCommentsEnabled(true);
        entityManager.persist(page);
        entityManager.flush();

        // Act
        final boolean exists = pageRepository.existsByLinkId(persistedLink.getId());

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByLinkId_WhenLinkDoesNotExist_ShouldReturnFalse() {
        // Act
        final boolean exists = pageRepository.existsByLinkId(UUID.randomUUID());

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void save_ShouldPersistPage() {
        // Arrange
        final Page page = new Page();
        page.setThumbnail(THUMBNAIL_PATH);
        page.setCommentsEnabled(true);

        // Act
        final Page savedPage = pageRepository.save(page);

        // Assert
        final Page foundPage = entityManager.find(Page.class, savedPage.getId());
        assertThat(foundPage).isNotNull();
        assertThat(foundPage.getThumbnail()).isEqualTo(THUMBNAIL_PATH);
        assertThat(foundPage.isCommentsEnabled()).isTrue();
    }

    @Test
    void findById_WhenPageExists_ShouldReturnPage() {
        // Arrange
        final Page page = new Page();
        page.setThumbnail(THUMBNAIL_PATH);
        page.setCommentsEnabled(true);
        final Page persistedPage = entityManager.persist(page);
        entityManager.flush();

        // Act
        final Optional<Page> foundPage = pageRepository.findById(persistedPage.getId());

        // Assert
        assertThat(foundPage).isPresent();
        assertThat(foundPage.get().getThumbnail()).isEqualTo(THUMBNAIL_PATH);
        assertThat(foundPage.get().isCommentsEnabled()).isTrue();
    }

    @Test
    void findById_WhenPageDoesNotExist_ShouldReturnEmpty() {
        // Act
        final Optional<Page> foundPage = pageRepository.findById(UUID.randomUUID());

        // Assert
        assertThat(foundPage).isEmpty();
    }
}

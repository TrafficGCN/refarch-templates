package de.muenchen.refarch.language;

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
class LanguageRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Language language;

    @BeforeEach
    void setUp() {
        // Create and persist a language
        language = new Language();
        language.setName("English");
        language.setAbbreviation("en");
        language.setFontAwesomeIcon("fa-flag-usa");
        language.setMdiIcon("mdi-flag");
        language = entityManager.persist(language);
        entityManager.flush();
    }

    @Test
    void findById_ShouldReturnLanguage() {
        // Act
        var foundLanguage = languageRepository.findById(language.getId());

        // Assert
        assertThat(foundLanguage).isPresent();
        assertThat(foundLanguage.get().getId()).isEqualTo(language.getId());
        assertThat(foundLanguage.get().getName()).isEqualTo("English");
        assertThat(foundLanguage.get().getAbbreviation()).isEqualTo("en");
        assertThat(foundLanguage.get().getFontAwesomeIcon()).isEqualTo("fa-flag-usa");
        assertThat(foundLanguage.get().getMdiIcon()).isEqualTo("mdi-flag");
    }

    @Test
    void findById_WhenLanguageDoesNotExist_ShouldReturnEmpty() {
        // Act
        var foundLanguage = languageRepository.findById(UUID.randomUUID());

        // Assert
        assertThat(foundLanguage).isEmpty();
    }

    @Test
    void existsByAbbreviation_WhenExists_ShouldReturnTrue() {
        // Act
        boolean exists = languageRepository.existsByAbbreviation("en");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByAbbreviation_WhenDoesNotExist_ShouldReturnFalse() {
        // Act
        boolean exists = languageRepository.existsByAbbreviation("fr");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void save_ShouldCreateLanguage() {
        // Arrange
        Language newLanguage = new Language();
        newLanguage.setName("German");
        newLanguage.setAbbreviation("de");
        newLanguage.setFontAwesomeIcon("fa-flag-germany");
        newLanguage.setMdiIcon("mdi-flag");

        // Act
        Language savedLanguage = languageRepository.save(newLanguage);

        // Assert
        Language foundLanguage = entityManager.find(Language.class, savedLanguage.getId());
        assertThat(foundLanguage).isNotNull();
        assertThat(foundLanguage.getName()).isEqualTo("German");
        assertThat(foundLanguage.getAbbreviation()).isEqualTo("de");
        assertThat(foundLanguage.getFontAwesomeIcon()).isEqualTo("fa-flag-germany");
        assertThat(foundLanguage.getMdiIcon()).isEqualTo("mdi-flag");
    }

    @Test
    void update_ShouldUpdateLanguage() {
        // Arrange
        language.setName("British English");
        language.setAbbreviation("en-GB");
        language.setFontAwesomeIcon("fa-flag-uk");
        language.setMdiIcon("mdi-flag-variant");

        // Act
        Language updatedLanguage = languageRepository.save(language);

        // Assert
        Language foundLanguage = entityManager.find(Language.class, updatedLanguage.getId());
        assertThat(foundLanguage).isNotNull();
        assertThat(foundLanguage.getName()).isEqualTo("British English");
        assertThat(foundLanguage.getAbbreviation()).isEqualTo("en-GB");
        assertThat(foundLanguage.getFontAwesomeIcon()).isEqualTo("fa-flag-uk");
        assertThat(foundLanguage.getMdiIcon()).isEqualTo("mdi-flag-variant");
    }

    @Test
    void delete_ShouldRemoveLanguage() {
        // Act
        languageRepository.delete(language);
        entityManager.flush();

        // Assert
        Language foundLanguage = entityManager.find(Language.class, language.getId());
        assertThat(foundLanguage).isNull();
    }

    @Test
    void findAll_ShouldReturnAllLanguages() {
        // Arrange
        Language secondLanguage = new Language();
        secondLanguage.setName("French");
        secondLanguage.setAbbreviation("fr");
        secondLanguage.setFontAwesomeIcon("fa-flag-france");
        secondLanguage.setMdiIcon("mdi-flag");
        entityManager.persist(secondLanguage);
        entityManager.flush();

        // Act
        List<Language> allLanguages = languageRepository.findAll();

        // Assert
        assertThat(allLanguages).hasSize(2);
        assertThat(allLanguages).extracting(Language::getName)
                .containsExactlyInAnyOrder("English", "French");
        assertThat(allLanguages).extracting(Language::getAbbreviation)
                .containsExactlyInAnyOrder("en", "fr");
    }
}

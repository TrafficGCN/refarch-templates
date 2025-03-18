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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class LanguageRepositoryTest {

    private static final String ENGLISH = "English";
    private static final String EN = "en";
    private static final String FA_FLAG_USA = "fa-flag-usa";
    private static final String MDI_FLAG = "mdi-flag";
    private static final String GERMAN = "German";
    private static final String DE = "de";
    private static final String FR = "fr";
    private static final String BRITISH_ENGLISH = "British English";
    private static final String EN_GB = "en-GB";
    private static final String FA_FLAG_UK = "fa-flag-uk";
    private static final String MDI_FLAG_VARIANT = "mdi-flag-variant";
    private static final String FRENCH = "French";
    private static final String FA_FLAG_FRANCE = "fa-flag-france";

    @Container
    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Language language;

    @DynamicPropertySource
    /* default */ static void setProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
    }

    @BeforeEach
    void setUp() {
        // Create and persist a language
        language = new Language();
        language.setName(ENGLISH);
        language.setAbbreviation(EN);
        language.setFontAwesomeIcon(FA_FLAG_USA);
        language.setMdiIcon(MDI_FLAG);
        language = entityManager.persist(language);
        entityManager.flush();
    }

    @Test
    void whenFindingById_shouldReturnLanguage() {
        // Act
        final Optional<Language> foundLanguage = languageRepository.findById(language.getId());

        // Assert
        assertThat(foundLanguage).isPresent();
        assertThat(foundLanguage.get().getId()).isEqualTo(language.getId());
        assertThat(foundLanguage.get().getName()).isEqualTo(ENGLISH);
        assertThat(foundLanguage.get().getAbbreviation()).isEqualTo(EN);
        assertThat(foundLanguage.get().getFontAwesomeIcon()).isEqualTo(FA_FLAG_USA);
        assertThat(foundLanguage.get().getMdiIcon()).isEqualTo(MDI_FLAG);
    }

    @Test
    void whenFindingByNonexistentId_shouldReturnEmpty() {
        // Act
        final Optional<Language> foundLanguage = languageRepository.findById(UUID.randomUUID());

        // Assert
        assertThat(foundLanguage).isEmpty();
    }

    @Test
    void whenCheckingExistsByAbbreviation_shouldReturnTrue() {
        // Act
        final boolean exists = languageRepository.existsByAbbreviation(EN);

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void whenCheckingNonexistentAbbreviation_shouldReturnFalse() {
        // Act
        final boolean exists = languageRepository.existsByAbbreviation(FR);

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void whenSavingNewLanguage_shouldCreateLanguage() {
        // Arrange
        final Language newLanguage = new Language();
        newLanguage.setName(GERMAN);
        newLanguage.setAbbreviation(DE);
        newLanguage.setFontAwesomeIcon(FA_FLAG_USA);
        newLanguage.setMdiIcon(MDI_FLAG);

        // Act
        final Language savedLanguage = languageRepository.save(newLanguage);
        entityManager.flush();

        // Assert
        assertThat(savedLanguage.getId()).isNotNull();
        assertThat(savedLanguage.getName()).isEqualTo(GERMAN);
        assertThat(savedLanguage.getAbbreviation()).isEqualTo(DE);
        assertThat(savedLanguage.getFontAwesomeIcon()).isEqualTo(FA_FLAG_USA);
        assertThat(savedLanguage.getMdiIcon()).isEqualTo(MDI_FLAG);
    }

    @Test
    void update_ShouldUpdateLanguage() {
        // Arrange
        language.setName("British English");
        language.setAbbreviation("en-GB");
        language.setFontAwesomeIcon("fa-flag-uk");
        language.setMdiIcon("mdi-flag-variant");

        // Act
        final Language updatedLanguage = languageRepository.save(language);

        // Assert
        final Language foundLanguage = entityManager.find(Language.class, updatedLanguage.getId());
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
        final Language foundLanguage = entityManager.find(Language.class, language.getId());
        assertThat(foundLanguage).isNull();
    }

    @Test
    void whenUpdatingLanguage_shouldSucceed() {
        // Arrange
        language.setName(BRITISH_ENGLISH);
        language.setAbbreviation(EN_GB);
        language.setFontAwesomeIcon(FA_FLAG_UK);
        language.setMdiIcon(MDI_FLAG_VARIANT);

        // Act
        final Language updatedLanguage = languageRepository.save(language);
        entityManager.flush();

        // Assert
        final Language foundLanguage = entityManager.find(Language.class, updatedLanguage.getId());
        assertThat(foundLanguage).isNotNull();
        assertThat(foundLanguage.getName()).isEqualTo(BRITISH_ENGLISH);
        assertThat(foundLanguage.getAbbreviation()).isEqualTo(EN_GB);
        assertThat(foundLanguage.getFontAwesomeIcon()).isEqualTo(FA_FLAG_UK);
        assertThat(foundLanguage.getMdiIcon()).isEqualTo(MDI_FLAG_VARIANT);
    }

    @Test
    void whenDeletingLanguage_shouldRemoveLanguage() {
        // Act
        languageRepository.delete(language);
        entityManager.flush();

        // Assert
        final Language foundLanguage = entityManager.find(Language.class, language.getId());
        assertThat(foundLanguage).isNull();
    }

    @Test
    void whenFindingAll_shouldReturnAllLanguages() {
        // Arrange
        final Language secondLanguage = new Language();
        secondLanguage.setName(FRENCH);
        secondLanguage.setAbbreviation(FR);
        secondLanguage.setFontAwesomeIcon(FA_FLAG_FRANCE);
        secondLanguage.setMdiIcon(MDI_FLAG);
        entityManager.persist(secondLanguage);
        entityManager.flush();

        // Act
        final List<Language> allLanguages = languageRepository.findAll();

        // Assert
        assertThat(allLanguages).hasSize(2);
        assertThat(allLanguages).extracting(Language::getName)
                .containsExactlyInAnyOrder(ENGLISH, FRENCH);
        assertThat(allLanguages).extracting(Language::getAbbreviation)
                .containsExactlyInAnyOrder(EN, FR);
    }
}

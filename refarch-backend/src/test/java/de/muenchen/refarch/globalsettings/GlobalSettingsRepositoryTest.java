package de.muenchen.refarch.globalsettings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class GlobalSettingsRepositoryTest {

    private static final String TEST_WEBSITE_NAME = "Test Website";
    private static final String INITIAL_WEBSITE_NAME = "Initial Name";
    private static final String UPDATED_WEBSITE_NAME = "Updated Name";
    private static final String DEFAULT_LANGUAGE = "en";

    @Container
    @ServiceConnection
    /* default */ private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16.0-alpine3.18");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GlobalSettingsRepository globalSettingsRepository;

    @BeforeEach
    void setUp() {
        // Clear all existing settings
        globalSettingsRepository.deleteAll();
        entityManager.flush();
    }

    @Test
    void findAll_ShouldReturnSettings() {
        // Create and persist test settings
        final GlobalSettings settings = new GlobalSettings();
        settings.setSessionDurationMinutes(480);
        settings.setWebsiteName(TEST_WEBSITE_NAME);
        settings.setGlobalCommentsEnabled(true);
        settings.setMaintenanceMode(false);
        settings.setMaxUploadSizeMb(10);
        settings.setDefaultLanguage(DEFAULT_LANGUAGE);
        settings.setMaxItemsPerPage(20);
        settings.setSsoAuthEnabled(false);
        settings.setPasswordAuthEnabled(true);
        entityManager.persist(settings);
        entityManager.flush();

        // Test findAll
        final List<GlobalSettings> result = globalSettingsRepository.findAll();

        assertThat(result).hasSize(1);
        final GlobalSettings foundSettings = result.get(0);
        assertThat(foundSettings.getWebsiteName()).isEqualTo(TEST_WEBSITE_NAME);
        assertThat(foundSettings.getSessionDurationMinutes()).isEqualTo(480);
        assertThat(foundSettings.getGlobalCommentsEnabled()).isTrue();
        assertThat(foundSettings.getMaintenanceMode()).isFalse();
        assertThat(foundSettings.getMaxUploadSizeMb()).isEqualTo(10);
        assertThat(foundSettings.getDefaultLanguage()).isEqualTo(DEFAULT_LANGUAGE);
        assertThat(foundSettings.getMaxItemsPerPage()).isEqualTo(20);
        assertThat(foundSettings.getSsoAuthEnabled()).isFalse();
        assertThat(foundSettings.getPasswordAuthEnabled()).isTrue();
    }

    @Test
    void save_ShouldPersistSettings() {
        // Create settings
        final GlobalSettings settings = new GlobalSettings();
        settings.setSessionDurationMinutes(480);
        settings.setWebsiteName(TEST_WEBSITE_NAME);
        settings.setGlobalCommentsEnabled(true);
        settings.setMaintenanceMode(false);
        settings.setMaxUploadSizeMb(10);
        settings.setDefaultLanguage(DEFAULT_LANGUAGE);
        settings.setMaxItemsPerPage(20);
        settings.setSsoAuthEnabled(false);
        settings.setPasswordAuthEnabled(true);

        // Save settings
        final GlobalSettings savedSettings = globalSettingsRepository.save(settings);

        // Verify save
        assertThat(savedSettings.getId()).isNotNull();
        assertThat(savedSettings.getCreatedAt()).isNotNull();
        assertThat(savedSettings.getUpdatedAt()).isNotNull();

        // Verify persistence
        final GlobalSettings persistedSettings = entityManager.find(GlobalSettings.class, savedSettings.getId());
        assertThat(persistedSettings).isNotNull();
        assertThat(persistedSettings.getWebsiteName()).isEqualTo(TEST_WEBSITE_NAME);
        assertThat(persistedSettings.getSsoAuthEnabled()).isFalse();
        assertThat(persistedSettings.getPasswordAuthEnabled()).isTrue();
    }

    @Test
    void update_ShouldUpdateSettings() {
        // Create and persist initial settings
        final GlobalSettings settings = new GlobalSettings();
        settings.setSessionDurationMinutes(480);
        settings.setWebsiteName(INITIAL_WEBSITE_NAME);
        settings.setGlobalCommentsEnabled(true);
        settings.setMaintenanceMode(false);
        settings.setMaxUploadSizeMb(10);
        settings.setDefaultLanguage(DEFAULT_LANGUAGE);
        settings.setMaxItemsPerPage(20);
        settings.setSsoAuthEnabled(false);
        settings.setPasswordAuthEnabled(true);
        entityManager.persist(settings);
        entityManager.flush();

        // Update settings
        settings.setWebsiteName(UPDATED_WEBSITE_NAME);
        settings.setSsoAuthEnabled(true);
        settings.setPasswordAuthEnabled(false);
        final GlobalSettings updatedSettings = globalSettingsRepository.save(settings);

        // Verify update
        assertThat(updatedSettings.getWebsiteName()).isEqualTo(UPDATED_WEBSITE_NAME);
        assertThat(updatedSettings.getSsoAuthEnabled()).isTrue();
        assertThat(updatedSettings.getPasswordAuthEnabled()).isFalse();
        assertThat(updatedSettings.getUpdatedAt()).isNotNull();
        assertThat(updatedSettings.getUpdatedAt()).isAfterOrEqualTo(updatedSettings.getCreatedAt());

        // Verify persistence
        final GlobalSettings persistedSettings = entityManager.find(GlobalSettings.class, settings.getId());
        assertThat(persistedSettings.getWebsiteName()).isEqualTo(UPDATED_WEBSITE_NAME);
        assertThat(persistedSettings.getSsoAuthEnabled()).isTrue();
        assertThat(persistedSettings.getPasswordAuthEnabled()).isFalse();
    }
}

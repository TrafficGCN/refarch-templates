package de.muenchen.refarch.globalsettings;

import de.muenchen.refarch.globalsettings.dto.GlobalSettingsRequestDTO;
import de.muenchen.refarch.globalsettings.dto.GlobalSettingsResponseDTO;
import de.muenchen.refarch.security.DynamicSecurityService.GlobalSettingsChangedEvent;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalSettingsServiceTest {

    @Mock
    private GlobalSettingsRepository globalSettingsRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private GlobalSettingsService globalSettingsService;

    private UUID settingsId;
    private GlobalSettings settings;
    private GlobalSettingsRequestDTO requestDTO;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        settingsId = UUID.randomUUID();
        now = LocalDateTime.now();

        settings = new GlobalSettings();
        settings.setId(settingsId);
        settings.setSessionDurationMinutes(480);
        settings.setLogoUrl("https://example.com/logo.png");
        settings.setWebsiteName("Test Website");
        settings.setGlobalCommentsEnabled(true);
        settings.setMaintenanceMode(false);
        settings.setMaxUploadSizeMb(10);
        settings.setDefaultLanguage("en");
        settings.setAnalyticsTrackingId("UA-12345");
        settings.setContactEmail("contact@example.com");
        settings.setMetaDescription("Test website description");
        settings.setMaxItemsPerPage(20);
        settings.setSsoAuthEnabled(false);
        settings.setPasswordAuthEnabled(true);
        settings.setCreatedAt(now);
        settings.setUpdatedAt(now);

        requestDTO = new GlobalSettingsRequestDTO(
                480,
                "https://example.com/logo.png",
                "Test Website",
                true,
                false,
                10,
                "en",
                "UA-12345",
                "contact@example.com",
                "Test website description",
                20,
                false,
                true);
    }

    @Test
    void getCurrentSettings_ShouldReturnSettings() {
        when(globalSettingsRepository.findAll()).thenReturn(List.of(settings));

        GlobalSettingsResponseDTO result = globalSettingsService.getCurrentSettings();

        assertThat(result.id()).isEqualTo(settingsId);
        assertThat(result.websiteName()).isEqualTo("Test Website");
        assertThat(result.sessionDurationMinutes()).isEqualTo(480);
        assertThat(result.globalCommentsEnabled()).isTrue();
        assertThat(result.maintenanceMode()).isFalse();
        assertThat(result.maxUploadSizeMb()).isEqualTo(10);
        assertThat(result.defaultLanguage()).isEqualTo("en");
        assertThat(result.analyticsTrackingId()).isEqualTo("UA-12345");
        assertThat(result.contactEmail()).isEqualTo("contact@example.com");
        assertThat(result.metaDescription()).isEqualTo("Test website description");
        assertThat(result.maxItemsPerPage()).isEqualTo(20);
        assertThat(result.ssoAuthEnabled()).isFalse();
        assertThat(result.passwordAuthEnabled()).isTrue();
        assertThat(result.createdAt()).isEqualTo(now);
        assertThat(result.updatedAt()).isEqualTo(now);
    }

    @Test
    void getCurrentSettings_ShouldThrowException_WhenSettingsNotFound() {
        when(globalSettingsRepository.findAll()).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> globalSettingsService.getCurrentSettings())
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Global settings not found");
    }

    @Test
    void updateSettings_ShouldCreateNewSettings_WhenNoSettingsExist() {
        when(globalSettingsRepository.findAll()).thenReturn(Collections.emptyList());
        when(globalSettingsRepository.save(any(GlobalSettings.class))).thenReturn(settings);

        GlobalSettingsResponseDTO result = globalSettingsService.updateSettings(requestDTO);

        assertThat(result.id()).isEqualTo(settingsId);
        assertThat(result.websiteName()).isEqualTo("Test Website");
        assertThat(result.sessionDurationMinutes()).isEqualTo(480);
        verify(eventPublisher).publishEvent(any(GlobalSettingsChangedEvent.class));
    }

    @Test
    void updateSettings_ShouldUpdateExistingSettings() {
        when(globalSettingsRepository.findAll()).thenReturn(List.of(settings));
        when(globalSettingsRepository.save(any(GlobalSettings.class))).thenReturn(settings);

        GlobalSettingsResponseDTO result = globalSettingsService.updateSettings(requestDTO);

        assertThat(result.id()).isEqualTo(settingsId);
        assertThat(result.websiteName()).isEqualTo("Test Website");
        assertThat(result.sessionDurationMinutes()).isEqualTo(480);
        verify(eventPublisher).publishEvent(any(GlobalSettingsChangedEvent.class));
    }
}

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

    private static final String TEST_WEBSITE_NAME = "Test Website";
    private static final String LOGO_URL = "https://example.com/logo.png";
    private static final String DEFAULT_LANGUAGE = "en";
    private static final String ANALYTICS_TRACKING_ID = "UA-12345";
    private static final String CONTACT_EMAIL = "contact@example.com";
    private static final String META_DESCRIPTION = "Test website description";

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
        settings.setLogoUrl(LOGO_URL);
        settings.setWebsiteName(TEST_WEBSITE_NAME);
        settings.setGlobalCommentsEnabled(true);
        settings.setMaintenanceMode(false);
        settings.setMaxUploadSizeMb(10);
        settings.setDefaultLanguage(DEFAULT_LANGUAGE);
        settings.setAnalyticsTrackingId(ANALYTICS_TRACKING_ID);
        settings.setContactEmail(CONTACT_EMAIL);
        settings.setMetaDescription(META_DESCRIPTION);
        settings.setMaxItemsPerPage(20);
        settings.setSsoAuthEnabled(false);
        settings.setPasswordAuthEnabled(true);
        settings.setCreatedAt(now);
        settings.setUpdatedAt(now);

        requestDTO = new GlobalSettingsRequestDTO(
                480,
                LOGO_URL,
                TEST_WEBSITE_NAME,
                true,
                false,
                10,
                DEFAULT_LANGUAGE,
                ANALYTICS_TRACKING_ID,
                CONTACT_EMAIL,
                META_DESCRIPTION,
                20,
                false,
                true);
    }

    @Test
    void shouldFetchCurrentSettings() {
        when(globalSettingsRepository.findAll()).thenReturn(List.of(settings));

        final GlobalSettingsResponseDTO result = globalSettingsService.getCurrentSettings();

        assertThat(result.id()).isEqualTo(settingsId);
        assertThat(result.websiteName()).isEqualTo(TEST_WEBSITE_NAME);
        assertThat(result.sessionDurationMinutes()).isEqualTo(480);
        assertThat(result.globalCommentsEnabled()).isTrue();
        assertThat(result.maintenanceMode()).isFalse();
        assertThat(result.maxUploadSizeMb()).isEqualTo(10);
        assertThat(result.defaultLanguage()).isEqualTo(DEFAULT_LANGUAGE);
        assertThat(result.analyticsTrackingId()).isEqualTo(ANALYTICS_TRACKING_ID);
        assertThat(result.contactEmail()).isEqualTo(CONTACT_EMAIL);
        assertThat(result.metaDescription()).isEqualTo(META_DESCRIPTION);
        assertThat(result.maxItemsPerPage()).isEqualTo(20);
        assertThat(result.ssoAuthEnabled()).isFalse();
        assertThat(result.passwordAuthEnabled()).isTrue();
        assertThat(result.createdAt()).isEqualTo(now);
        assertThat(result.updatedAt()).isEqualTo(now);
    }

    @Test
    void shouldFailWhenSettingsNotFound() {
        when(globalSettingsRepository.findAll()).thenReturn(Collections.emptyList());

        assertThatThrownBy(globalSettingsService::getCurrentSettings)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Global settings not found");
    }

    @Test
    void shouldUpdateAndPublishSettings() {
        when(globalSettingsRepository.findAll()).thenReturn(List.of(settings));
        when(globalSettingsRepository.save(any(GlobalSettings.class))).thenReturn(settings);

        final GlobalSettingsResponseDTO result = globalSettingsService.updateSettings(requestDTO);

        assertThat(result.id()).isEqualTo(settingsId);
        assertThat(result.websiteName()).isEqualTo(TEST_WEBSITE_NAME);
        assertThat(result.sessionDurationMinutes()).isEqualTo(480);
        verify(eventPublisher).publishEvent(any(GlobalSettingsChangedEvent.class));
    }
}

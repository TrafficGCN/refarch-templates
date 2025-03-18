package de.muenchen.refarch.security;

import de.muenchen.refarch.globalsettings.GlobalSettingsService;
import de.muenchen.refarch.globalsettings.GlobalSettings;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("!no-security")
public class DynamicSecurityService {

    private final GlobalSettingsService globalSettingsService;
    private final AtomicBoolean ssoEnabled = new AtomicBoolean(false);

    // Initialize the status on startup
    @PostConstruct
    public void init() {
        ssoEnabled.set(globalSettingsService.getCurrentSettings().ssoAuthEnabled());
        log.info("Initial SSO status: {}", ssoEnabled.get());
    }

    public boolean isSsoEnabled() {
        return ssoEnabled.get();
    }

    @EventListener(GlobalSettingsChangedEvent.class)
    public void onGlobalSettingsChanged(final GlobalSettingsChangedEvent event) {
        final boolean newSsoEnabled = event.getNewSettings().getSsoAuthEnabled();
        final boolean oldValue = ssoEnabled.get();
        if (oldValue != newSsoEnabled) {
            log.info("SSO status changed from {} to {}", oldValue, newSsoEnabled);
            ssoEnabled.set(newSsoEnabled);
        }
    }

    private static GlobalSettings copyGlobalSettings(final GlobalSettings settings) {
        if (settings == null) {
            return null;
        }
        return GlobalSettings.builder()
                .id(settings.getId())
                .sessionDurationMinutes(settings.getSessionDurationMinutes())
                .logoUrl(settings.getLogoUrl())
                .websiteName(settings.getWebsiteName())
                .globalCommentsEnabled(settings.getGlobalCommentsEnabled())
                .maintenanceMode(settings.getMaintenanceMode())
                .maxUploadSizeMb(settings.getMaxUploadSizeMb())
                .defaultLanguage(settings.getDefaultLanguage())
                .analyticsTrackingId(settings.getAnalyticsTrackingId())
                .contactEmail(settings.getContactEmail())
                .metaDescription(settings.getMetaDescription())
                .maxItemsPerPage(settings.getMaxItemsPerPage())
                .ssoAuthEnabled(settings.getSsoAuthEnabled())
                .passwordAuthEnabled(settings.getPasswordAuthEnabled())
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }

    // Event class for global settings changes
    public record GlobalSettingsChangedEvent(GlobalSettings newSettings) {
        public GlobalSettingsChangedEvent {
            // Defensive copy in the canonical constructor
            newSettings = copyGlobalSettings(newSettings);
        }

        @Override
        public GlobalSettings newSettings() {
            return copyGlobalSettings(newSettings);
        }

        public GlobalSettings getNewSettings() {
            return copyGlobalSettings(newSettings);
        }
    }
}

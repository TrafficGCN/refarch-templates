package de.muenchen.refarch.security;

import de.muenchen.refarch.globalsettings.GlobalSettingsService;
import de.muenchen.refarch.globalsettings.GlobalSettings;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("!no-security")
public class DynamicSecurityService {

    private final GlobalSettingsService globalSettingsService;
    private volatile boolean ssoEnabled;

    // Initialize the status on startup
    @PostConstruct
    public void init() {
        ssoEnabled = globalSettingsService.getCurrentSettings().ssoAuthEnabled();
        log.info("Initial SSO status: {}", ssoEnabled);
    }

    public boolean isSsoEnabled() {
        return ssoEnabled;
    }

    @EventListener(GlobalSettingsChangedEvent.class)
    public void onGlobalSettingsChanged(GlobalSettingsChangedEvent event) {
        boolean newSsoEnabled = event.getNewSettings().getSsoAuthEnabled();
        if (ssoEnabled != newSsoEnabled) {
            log.info("SSO status changed from {} to {}", ssoEnabled, newSsoEnabled);
            ssoEnabled = newSsoEnabled;
        }
    }

    // Event class for global settings changes
    public record GlobalSettingsChangedEvent(GlobalSettings newSettings) {
        public GlobalSettings getNewSettings() {
            return newSettings;
        }
    }
}

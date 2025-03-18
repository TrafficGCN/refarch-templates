package de.muenchen.refarch.globalsettings;

import de.muenchen.refarch.globalsettings.dto.GlobalSettingsRequestDTO;
import de.muenchen.refarch.globalsettings.dto.GlobalSettingsResponseDTO;
import de.muenchen.refarch.security.Authorities;
import de.muenchen.refarch.security.DynamicSecurityService.GlobalSettingsChangedEvent;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GlobalSettingsService {
    private final GlobalSettingsRepository globalSettingsRepository;
    private final ApplicationEventPublisher eventPublisher;

    @PreAuthorize(Authorities.SETTINGS_READ)
    @Transactional(readOnly = true)
    public GlobalSettingsResponseDTO getCurrentSettings() {
        return globalSettingsRepository.findAll().stream()
                .findFirst()
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException("Global settings not found"));
    }

    @PreAuthorize(Authorities.SETTINGS_WRITE)
    @Transactional
    public GlobalSettingsResponseDTO updateSettings(final GlobalSettingsRequestDTO request) {
        final GlobalSettings settings = globalSettingsRepository.findAll().stream()
                .findFirst()
                .orElseGet(GlobalSettings::new);

        settings.setSessionDurationMinutes(request.sessionDurationMinutes());
        settings.setLogoUrl(request.logoUrl());
        settings.setWebsiteName(request.websiteName());
        settings.setGlobalCommentsEnabled(request.globalCommentsEnabled());
        settings.setMaintenanceMode(request.maintenanceMode());
        settings.setMaxUploadSizeMb(request.maxUploadSizeMb());
        settings.setDefaultLanguage(request.defaultLanguage());
        settings.setAnalyticsTrackingId(request.analyticsTrackingId());
        settings.setContactEmail(request.contactEmail());
        settings.setMetaDescription(request.metaDescription());
        settings.setMaxItemsPerPage(request.maxItemsPerPage());
        settings.setSsoAuthEnabled(request.ssoAuthEnabled());
        settings.setPasswordAuthEnabled(request.passwordAuthEnabled());

        final GlobalSettings savedSettings = globalSettingsRepository.save(settings);
        eventPublisher.publishEvent(new GlobalSettingsChangedEvent(savedSettings));
        return mapToResponseDTO(savedSettings);
    }

    private GlobalSettingsResponseDTO mapToResponseDTO(final GlobalSettings settings) {
        return new GlobalSettingsResponseDTO(
                settings.getId(),
                settings.getSessionDurationMinutes(),
                settings.getLogoUrl(),
                settings.getWebsiteName(),
                settings.getGlobalCommentsEnabled(),
                settings.getMaintenanceMode(),
                settings.getMaxUploadSizeMb(),
                settings.getDefaultLanguage(),
                settings.getAnalyticsTrackingId(),
                settings.getContactEmail(),
                settings.getMetaDescription(),
                settings.getMaxItemsPerPage(),
                settings.getSsoAuthEnabled(),
                settings.getPasswordAuthEnabled(),
                settings.getCreatedAt(),
                settings.getUpdatedAt());
    }
}

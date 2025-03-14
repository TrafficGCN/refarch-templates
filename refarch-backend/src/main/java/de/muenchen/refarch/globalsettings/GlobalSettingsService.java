package de.muenchen.refarch.globalsettings;

import de.muenchen.refarch.globalsettings.dto.GlobalSettingsRequestDTO;
import de.muenchen.refarch.globalsettings.dto.GlobalSettingsResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GlobalSettingsService {
    private final GlobalSettingsRepository globalSettingsRepository;

    @Transactional(readOnly = true)
    public GlobalSettingsResponseDTO getCurrentSettings() {
        return globalSettingsRepository.findAll().stream()
                .findFirst()
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException("Global settings not found"));
    }

    @Transactional
    public GlobalSettingsResponseDTO updateSettings(GlobalSettingsRequestDTO request) {
        GlobalSettings settings = globalSettingsRepository.findAll().stream()
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
        settings.setSsoEnabled(request.ssoEnabled());

        return mapToResponseDTO(globalSettingsRepository.save(settings));
    }

    private GlobalSettingsResponseDTO mapToResponseDTO(GlobalSettings settings) {
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
                settings.getSsoEnabled(),
                settings.getCreatedAt(),
                settings.getUpdatedAt());
    }
}

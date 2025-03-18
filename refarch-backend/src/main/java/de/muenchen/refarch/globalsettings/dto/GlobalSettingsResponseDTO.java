package de.muenchen.refarch.globalsettings.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record GlobalSettingsResponseDTO(
        UUID id,
        Integer sessionDurationMinutes,
        String logoUrl,
        String websiteName,
        Boolean globalCommentsEnabled,
        Boolean maintenanceMode,
        Integer maxUploadSizeMb,
        String defaultLanguage,
        String analyticsTrackingId,
        String contactEmail,
        String metaDescription,
        Integer maxItemsPerPage,
        Boolean ssoAuthEnabled,
        Boolean passwordAuthEnabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}

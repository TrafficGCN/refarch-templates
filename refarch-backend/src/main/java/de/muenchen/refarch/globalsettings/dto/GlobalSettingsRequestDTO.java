package de.muenchen.refarch.globalsettings.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record GlobalSettingsRequestDTO(
        @NotNull @Positive Integer sessionDurationMinutes,
        String logoUrl,
        @NotBlank String websiteName,
        @NotNull Boolean globalCommentsEnabled,
        @NotNull Boolean maintenanceMode,
        @NotNull @Positive Integer maxUploadSizeMb,
        @NotBlank String defaultLanguage,
        String analyticsTrackingId,
        String contactEmail,
        String metaDescription,
        @NotNull @Positive Integer maxItemsPerPage,
        @NotNull Boolean ssoAuthEnabled,
        @NotNull Boolean passwordAuthEnabled) {
}

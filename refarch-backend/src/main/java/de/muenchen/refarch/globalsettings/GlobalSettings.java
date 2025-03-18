package de.muenchen.refarch.globalsettings;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "global_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("PMD.TooManyFields")
public class GlobalSettings {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @Positive
    @Column(name = "session_duration_minutes", nullable = false)
    private Integer sessionDurationMinutes;

    @Column(name = "logo_url")
    private String logoUrl;

    @NotBlank
    @Column(name = "website_name", nullable = false)
    private String websiteName;

    @NotNull
    @Column(name = "global_comments_enabled", nullable = false)
    private Boolean globalCommentsEnabled;

    @NotNull
    @Column(name = "maintenance_mode", nullable = false)
    private Boolean maintenanceMode;

    @NotNull
    @Positive
    @Column(name = "max_upload_size_mb", nullable = false)
    private Integer maxUploadSizeMb;

    @NotBlank
    @Column(name = "default_language", nullable = false)
    private String defaultLanguage;

    @Column(name = "analytics_tracking_id")
    private String analyticsTrackingId;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "meta_description")
    private String metaDescription;

    @NotNull
    @Positive
    @Column(name = "max_items_per_page", nullable = false)
    private Integer maxItemsPerPage;

    @NotNull
    @Column(name = "sso_auth_enabled", nullable = false)
    private Boolean ssoAuthEnabled;

    @NotNull
    @Column(name = "password_auth_enabled", nullable = false)
    private Boolean passwordAuthEnabled;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

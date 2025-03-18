package de.muenchen.refarch.user.bio;

import de.muenchen.refarch.common.BaseEntity;
import de.muenchen.refarch.common.EntityCopyUtils;
import de.muenchen.refarch.language.Language;
import de.muenchen.refarch.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * Entity representing a user's biography in a specific language.
 */
@Entity
@Table(name = "user_bios_i18n")
@Setter
public class UserBio extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    @Getter
    private String bio;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @Getter
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @Getter
    private LocalDateTime updatedAt;

    public User getUser() {
        return EntityCopyUtils.copyUser(user);
    }

    public Language getLanguage() {
        return EntityCopyUtils.copyLanguage(language);
    }
}

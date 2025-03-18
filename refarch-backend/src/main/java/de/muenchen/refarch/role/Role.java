package de.muenchen.refarch.role;

import de.muenchen.refarch.common.BaseEntity;
import de.muenchen.refarch.common.EntityCopyUtils;
import de.muenchen.refarch.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@SuppressWarnings("PMD.ShortClassName")
public class Role extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "roles")
    @Getter(AccessLevel.NONE)
    private Set<User> users = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Set<User> getUsers() {
        if (users == null) {
            return new HashSet<>();
        }
        final Set<User> usersCopy = new HashSet<>();
        for (final User user : users) {
            if (user != null) {
                usersCopy.add(EntityCopyUtils.copyUser(user));
            }
        }
        return usersCopy;
    }
}

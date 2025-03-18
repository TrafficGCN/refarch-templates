package de.muenchen.refarch.user;

import de.muenchen.refarch.common.BaseEntity;
import de.muenchen.refarch.role.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a user in the system.
 */
@Entity
@Table(name = "users")
@Setter
@SuppressWarnings("PMD.ShortClassName")
public class User extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    @Column(nullable = false)
    @Getter
    private String username;

    @NotBlank
    @Email
    @Column(nullable = false)
    @JsonIgnore // Never serialize the email
    private String email;

    @NotBlank
    @Column(nullable = false)
    @JsonIgnore // Never serialize the password
    private String password;

    @Column(name = "first_name")
    @Getter
    private String firstName;

    @Column(name = "last_name")
    @Getter
    private String lastName;

    @Getter
    private String title;

    @Getter
    private String affiliation;

    @Getter
    private String thumbnail;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @Getter
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @Getter
    private LocalDateTime updatedAt;

    public Set<Role> getRoles() {
        if (roles == null) {
            return new HashSet<>();
        }
        final Set<Role> rolesCopy = new HashSet<>();
        for (final Role role : roles) {
            if (role != null) {
                final Role roleCopy = new Role();
                roleCopy.setId(role.getId());
                roleCopy.setName(role.getName());
                roleCopy.setCreatedAt(role.getCreatedAt());
                roleCopy.setUpdatedAt(role.getUpdatedAt());
                rolesCopy.add(roleCopy);
            }
        }
        return rolesCopy;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}

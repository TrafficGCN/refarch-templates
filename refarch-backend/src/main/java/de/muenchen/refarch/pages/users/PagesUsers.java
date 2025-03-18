package de.muenchen.refarch.pages.users;

import de.muenchen.refarch.common.EntityCopyUtils;
import de.muenchen.refarch.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "pages_users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagesUsers {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "page_link_id", nullable = false)
    private UUID pageLinkId;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Getter(AccessLevel.NONE)
    private User user;

    public User getUser() {
        return EntityCopyUtils.copyUser(user);
    }
}

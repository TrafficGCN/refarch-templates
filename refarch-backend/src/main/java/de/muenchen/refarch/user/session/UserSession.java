package de.muenchen.refarch.user.session;

import de.muenchen.refarch.core.BaseEntity;
import de.muenchen.refarch.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
@Getter
@Setter
public class UserSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(unique = true)
    private String refreshToken;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime lastActivityAt;

    @Column(length = 45)
    private String ipAddress;

    private String userAgent;
}

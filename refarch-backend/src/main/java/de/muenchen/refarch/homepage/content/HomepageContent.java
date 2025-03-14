package de.muenchen.refarch.homepage.content;

import de.muenchen.refarch.homepage.Homepage;
import de.muenchen.refarch.language.Language;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "homepage_content_i18n", uniqueConstraints = {
                @UniqueConstraint(columnNames = { "homepage_id", "language_id" })
        }
)
@Getter
@Setter
@NoArgsConstructor
public class HomepageContent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homepage_id", nullable = false)
    private Homepage homepage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(name = "welcome_message", nullable = false)
    private String welcomeMessage;

    @Column(name = "welcome_message_extended")
    private String welcomeMessageExtended;

    @Column(name = "explore_our_work")
    private String exploreOurWork;

    @Column(name = "get_involved")
    private String getInvolved;

    @Column(name = "important_links")
    private String importantLinks;

    @Column(name = "ecosystem_links")
    private String ecosystemLinks;

    @Column(name = "blog")
    private String blog;

    @Column(name = "papers")
    private String papers;

    @Column(name = "read_more")
    private String readMore;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

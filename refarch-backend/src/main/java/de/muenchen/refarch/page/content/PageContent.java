package de.muenchen.refarch.page.content;

import de.muenchen.refarch.common.EntityCopyUtils;
import de.muenchen.refarch.language.Language;
import de.muenchen.refarch.page.Page;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "pages_content_i18n",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = { "page_id", "language_id" })
        }
)
@Getter
@Setter
public class PageContent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    private Page page;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    @Getter(AccessLevel.NONE)
    private Language language;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(name = "short_description")
    private String shortDescription;

    private String keywords;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Page getPage() {
        return EntityCopyUtils.copyPage(page);
    }

    public Language getLanguage() {
        return EntityCopyUtils.copyLanguage(language);
    }
}

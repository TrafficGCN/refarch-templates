package de.muenchen.refarch.page;

import de.muenchen.refarch.common.EntityCopyUtils;
import de.muenchen.refarch.link.Link;
import de.muenchen.refarch.page.content.PageContent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "pages")
@Getter
@Setter
@SuppressWarnings("PMD.ShortClassName")
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "link_id")
    @Getter(AccessLevel.NONE)
    private Link link;

    @Column(length = 510)
    private String thumbnail;

    @Column(name = "comments_enabled")
    private boolean commentsEnabled = true;

    @Column(name = "published")
    private boolean published = false;

    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter(AccessLevel.NONE)
    private Set<PageContent> contents = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Link getLink() {
        return EntityCopyUtils.copyLink(link);
    }

    public Set<PageContent> getContents() {
        return contents == null ? Collections.emptySet()
                : Collections.unmodifiableSet(new HashSet<>(contents));
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || (o instanceof Page other && Objects.equals(id, other.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Helper methods for managing bidirectional relationship
    public void addContent(final PageContent content) {
        contents.add(content);
        content.setPage(this);
    }

    public void removeContent(final PageContent content) {
        contents.remove(content);
        content.setPage(null);
    }
}

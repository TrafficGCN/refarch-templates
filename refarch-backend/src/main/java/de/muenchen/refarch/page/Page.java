package de.muenchen.refarch.page;

import de.muenchen.refarch.link.Link;
import de.muenchen.refarch.page.content.PageContent;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "pages")
@Getter
@Setter
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "link_id")
    private Link link;

    @Column(length = 510)
    private String thumbnail;

    @Column(name = "comments_enabled")
    private boolean commentsEnabled = true;

    @Column(name = "published")
    private boolean published = false;

    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PageContent> contents = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods for managing bidirectional relationship
    public void addContent(PageContent content) {
        contents.add(content);
        content.setPage(this);
    }

    public void removeContent(PageContent content) {
        contents.remove(content);
        content.setPage(null);
    }
}

package de.muenchen.refarch.homepage;

import de.muenchen.refarch.common.EntityCopyUtils;
import de.muenchen.refarch.homepage.content.HomepageContent;
import de.muenchen.refarch.link.Link;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "homepage")
@Getter
@Setter
@NoArgsConstructor
public class Homepage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @OneToOne
    @JoinColumn(name = "link_id")
    @Getter(AccessLevel.NONE)
    private Link link;

    @Column(length = 510)
    private String thumbnail;

    @OneToMany(mappedBy = "homepage", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter(AccessLevel.NONE)
    private Set<HomepageContent> contents = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Link getLink() {
        return EntityCopyUtils.copyLink(link);
    }

    public Set<HomepageContent> getContents() {
        return contents == null ? Collections.emptySet()
                : Collections.unmodifiableSet(new HashSet<>(contents));
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || (o instanceof Homepage other && Objects.equals(id, other.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void addContent(final HomepageContent content) {
        contents.add(content);
        content.setHomepage(this);
    }

    public void removeContent(final HomepageContent content) {
        contents.remove(content);
        content.setHomepage(null);
    }
}

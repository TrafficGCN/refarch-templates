package de.muenchen.refarch.homepage;

import de.muenchen.refarch.homepage.content.HomepageContent;
import de.muenchen.refarch.link.Link;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
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
    private Link link;

    @Column(length = 510)
    private String thumbnail;

    @OneToMany(mappedBy = "homepage", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<HomepageContent> contents = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void addContent(HomepageContent content) {
        contents.add(content);
        content.setHomepage(this);
    }

    public void removeContent(HomepageContent content) {
        contents.remove(content);
        content.setHomepage(null);
    }
}

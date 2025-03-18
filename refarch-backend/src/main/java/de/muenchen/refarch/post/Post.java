package de.muenchen.refarch.post;

import de.muenchen.refarch.common.BaseEntity;
import de.muenchen.refarch.common.EntityCopyUtils;
import de.muenchen.refarch.link.Link;
import de.muenchen.refarch.post.content.PostContent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "posts")
@Getter
@Setter
@SuppressWarnings("PMD.ShortClassName")
public class Post extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "link_id")
    @Getter(AccessLevel.NONE)
    private Link link;

    private String thumbnail;

    @Column(name = "comments_enabled")
    private boolean commentsEnabled = true;

    @Column(name = "published")
    private boolean published = false;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter(AccessLevel.NONE)
    private Set<PostContent> contents = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Link getLink() {
        return EntityCopyUtils.copyLink(link);
    }

    public Set<PostContent> getContents() {
        return contents == null ? Collections.emptySet() : Collections.unmodifiableSet(contents);
    }

    public void addContent(final PostContent content) {
        contents.add(content);
        content.setPost(this);
    }

    public void removeContent(final PostContent content) {
        contents.remove(content);
        content.setPost(null);
    }
}

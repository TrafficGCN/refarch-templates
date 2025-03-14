package de.muenchen.refarch.post.content;

import de.muenchen.refarch.language.Language;
import de.muenchen.refarch.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostContentRepository extends JpaRepository<PostContent, UUID> {
    List<PostContent> findAllByPost(Post post);

    Optional<PostContent> findByPostAndLanguage(Post post, Language language);

    void deleteAllByPost(Post post);
}

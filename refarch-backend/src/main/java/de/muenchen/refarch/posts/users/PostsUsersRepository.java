package de.muenchen.refarch.posts.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostsUsersRepository extends JpaRepository<PostsUsers, UUID> {
    List<PostsUsers> findByPostLinkId(UUID postLinkId);

    List<PostsUsers> findByUser_Id(UUID userId);

    void deleteByPostLinkIdAndUser_Id(UUID postLinkId, UUID userId);
}

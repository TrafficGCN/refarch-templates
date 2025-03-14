package de.muenchen.refarch.posts.users;

import de.muenchen.refarch.user.User;
import de.muenchen.refarch.user.UserService;
import de.muenchen.refarch.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostsUsersService {
    private final PostsUsersRepository postsUsersRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<PostsUsers> findByPostLinkId(UUID postLinkId) {
        return postsUsersRepository.findByPostLinkId(postLinkId);
    }

    @Transactional(readOnly = true)
    public List<PostsUsers> findByUserId(UUID userId) {
        return postsUsersRepository.findByUser_Id(userId);
    }

    @Transactional
    public PostsUsers assignUserToPost(UUID postLinkId, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        PostsUsers postsUsers = new PostsUsers();
        postsUsers.setPostLinkId(postLinkId);
        postsUsers.setUser(user);
        return postsUsersRepository.save(postsUsers);
    }

    @Transactional
    public void removeUserFromPost(UUID postLinkId, UUID userId) {
        postsUsersRepository.deleteByPostLinkIdAndUser_Id(postLinkId, userId);
    }
}

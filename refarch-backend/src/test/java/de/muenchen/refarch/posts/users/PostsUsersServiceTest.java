package de.muenchen.refarch.posts.users;

import de.muenchen.refarch.user.User;
import de.muenchen.refarch.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostsUsersServiceTest {

    @Mock
    private PostsUsersRepository postsUsersRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostsUsersService postsUsersService;

    private UUID postLinkId;
    private UUID userId;
    private User user;
    private PostsUsers postsUsers;

    @BeforeEach
    void setUp() {
        postLinkId = UUID.randomUUID();
        userId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        postsUsers = new PostsUsers();
        postsUsers.setId(UUID.randomUUID());
        postsUsers.setPostLinkId(postLinkId);
        postsUsers.setUser(user);
    }

    @Test
    void findByPostLinkId_ShouldReturnList() {
        when(postsUsersRepository.findByPostLinkId(postLinkId)).thenReturn(List.of(postsUsers));

        final List<PostsUsers> result = postsUsersService.findByPostLinkId(postLinkId);

        assertThat(result)
                .hasSize(1)
                .first()
                .satisfies(pu -> {
                    assertThat(pu.getPostLinkId()).isEqualTo(postLinkId);
                    assertThat(pu.getUser().getId()).isEqualTo(userId);
                });
        verify(postsUsersRepository).findByPostLinkId(postLinkId);
    }

    @Test
    void findByUserId_ShouldReturnList() {
        when(postsUsersRepository.findByUser_Id(userId)).thenReturn(List.of(postsUsers));

        final List<PostsUsers> result = postsUsersService.findByUserId(userId);

        assertThat(result)
                .hasSize(1)
                .first()
                .satisfies(pu -> {
                    assertThat(pu.getPostLinkId()).isEqualTo(postLinkId);
                    assertThat(pu.getUser().getId()).isEqualTo(userId);
                });
        verify(postsUsersRepository).findByUser_Id(userId);
    }

    @Test
    void assignUserToPost_ShouldCreateAssociation() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postsUsersRepository.save(any(PostsUsers.class))).thenReturn(postsUsers);

        final PostsUsers result = postsUsersService.assignUserToPost(postLinkId, userId);

        assertThat(result.getPostLinkId()).isEqualTo(postLinkId);
        assertThat(result.getUser().getId()).isEqualTo(userId);
        verify(userRepository).findById(userId);
        verify(postsUsersRepository).save(any(PostsUsers.class));
    }

    @Test
    void removeUserFromPost_ShouldDeleteAssociation() {
        doNothing().when(postsUsersRepository).deleteByPostLinkIdAndUser_Id(postLinkId, userId);

        postsUsersService.removeUserFromPost(postLinkId, userId);

        verify(postsUsersRepository).deleteByPostLinkIdAndUser_Id(postLinkId, userId);
    }
}

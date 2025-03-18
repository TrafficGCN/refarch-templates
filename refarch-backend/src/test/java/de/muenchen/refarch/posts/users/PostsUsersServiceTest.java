package de.muenchen.refarch.posts.users;

import de.muenchen.refarch.user.User;
import de.muenchen.refarch.user.UserRepository;
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
    private final PostsUsersRepository postsUsersRepository;

    @Mock
    private final UserRepository userRepository;

    @InjectMocks
    private final PostsUsersService postsUsersService;

    private final UUID postLinkId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final User user;
    private final PostsUsers postsUsers;

    /* default */ PostsUsersServiceTest() {
        this.postsUsersRepository = null; // Will be injected by Mockito
        this.userRepository = null; // Will be injected by Mockito
        this.postsUsersService = null; // Will be injected by Mockito

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
    void shouldReturnPostsUsersForPostLinkId() {
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
    void shouldReturnPostsUsersForUserId() {
        when(postsUsersRepository.findByUserId(userId)).thenReturn(List.of(postsUsers));

        final List<PostsUsers> result = postsUsersService.findByUserId(userId);

        assertThat(result)
                .hasSize(1)
                .first()
                .satisfies(pu -> {
                    assertThat(pu.getPostLinkId()).isEqualTo(postLinkId);
                    assertThat(pu.getUser().getId()).isEqualTo(userId);
                });
        verify(postsUsersRepository).findByUserId(userId);
    }

    @Test
    void shouldCreateAssociationBetweenUserAndPost() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postsUsersRepository.save(any(PostsUsers.class))).thenReturn(postsUsers);

        final PostsUsers result = postsUsersService.assignUserToPost(postLinkId, userId);

        assertThat(result.getPostLinkId()).isEqualTo(postLinkId);
        assertThat(result.getUser().getId()).isEqualTo(userId);
        verify(userRepository).findById(userId);
        verify(postsUsersRepository).save(any(PostsUsers.class));
    }

    @Test
    void shouldDeleteAssociationBetweenUserAndPost() {
        doNothing().when(postsUsersRepository).deleteByPostLinkIdAndUserId(postLinkId, userId);

        postsUsersService.removeUserFromPost(postLinkId, userId);

        verify(postsUsersRepository).deleteByPostLinkIdAndUserId(postLinkId, userId);
    }
}

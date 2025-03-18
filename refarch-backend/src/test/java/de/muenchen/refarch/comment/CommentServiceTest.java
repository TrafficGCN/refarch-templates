package de.muenchen.refarch.comment;

import de.muenchen.refarch.comment.dto.CommentResponseDTO;
import de.muenchen.refarch.user.User;
import de.muenchen.refarch.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    private static final String TEST_COMMENT = "Test comment";
    private static final String UPDATED_COMMENT = "Updated comment";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private User user;
    private Comment comment;
    private UUID userId;
    private UUID postId;
    private UUID pageId;
    private UUID commentId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        postId = UUID.randomUUID();
        pageId = UUID.randomUUID();
        commentId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setUsername(TEST_USERNAME);
        user.setEmail(TEST_EMAIL);

        comment = new Comment();
        comment.setId(commentId);
        comment.setContent(TEST_COMMENT);
        comment.setUser(user);
        comment.setPostId(postId);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void findByPostId_ShouldReturnComments() {
        when(commentRepository.findByPostIdOrderByCreatedAtDesc(postId))
                .thenReturn(Arrays.asList(comment));

        final List<CommentResponseDTO> result = commentService.findByPostId(postId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(commentId);
        assertThat(result.get(0).content()).isEqualTo(TEST_COMMENT);
        verify(commentRepository).findByPostIdOrderByCreatedAtDesc(postId);
    }

    @Test
    void findByPageId_ShouldReturnComments() {
        comment.setPostId(null);
        comment.setPageId(pageId);
        when(commentRepository.findByPageIdOrderByCreatedAtDesc(pageId))
                .thenReturn(Arrays.asList(comment));

        final List<CommentResponseDTO> result = commentService.findByPageId(pageId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(commentId);
        assertThat(result.get(0).content()).isEqualTo(TEST_COMMENT);
        verify(commentRepository).findByPageIdOrderByCreatedAtDesc(pageId);
    }

    @Test
    void createPostComment_ShouldCreateComment() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        final CommentResponseDTO result = commentService.createPostComment(userId, postId, TEST_COMMENT);

        assertThat(result.id()).isEqualTo(commentId);
        assertThat(result.content()).isEqualTo(TEST_COMMENT);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createPageComment_ShouldCreateComment() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        comment.setPostId(null);
        comment.setPageId(pageId);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        final CommentResponseDTO result = commentService.createPageComment(userId, pageId, TEST_COMMENT);

        assertThat(result.id()).isEqualTo(commentId);
        assertThat(result.content()).isEqualTo(TEST_COMMENT);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void updateComment_ShouldUpdateComment() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        final CommentResponseDTO result = commentService.updateComment(commentId, userId, UPDATED_COMMENT);

        assertThat(result.id()).isEqualTo(commentId);
        assertThat(result.content()).isEqualTo(UPDATED_COMMENT);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void updateComment_ShouldThrowException_WhenUserNotAuthorized() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        final UUID differentUserId = UUID.randomUUID();
        assertThatThrownBy(() -> commentService.updateComment(commentId, differentUserId, UPDATED_COMMENT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User is not authorized to update this comment");
    }

    @Test
    void deleteComment_ShouldDeleteComment() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        commentService.deleteComment(commentId, userId);

        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_ShouldThrowException_WhenUserNotAuthorized() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        final UUID differentUserId = UUID.randomUUID();
        assertThatThrownBy(() -> commentService.deleteComment(commentId, differentUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User is not authorized to delete this comment");
    }

    @Test
    void findByUserId_ShouldReturnComments() {
        when(commentRepository.findByUserId(userId))
                .thenReturn(Arrays.asList(comment));

        final List<CommentResponseDTO> result = commentService.findByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(commentId);
        assertThat(result.get(0).content()).isEqualTo(TEST_COMMENT);
        verify(commentRepository).findByUserId(userId);
    }

    @Test
    void createComment_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createPostComment(userId, postId, TEST_COMMENT))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found with id: " + userId);
    }
}

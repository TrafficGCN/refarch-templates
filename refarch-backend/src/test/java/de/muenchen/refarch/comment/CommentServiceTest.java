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
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        comment = new Comment();
        comment.setId(commentId);
        comment.setContent("Test comment");
        comment.setUser(user);
        comment.setPostId(postId);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void findByPostId_ShouldReturnComments() {
        when(commentRepository.findByPostIdOrderByCreatedAtDesc(postId))
                .thenReturn(Arrays.asList(comment));

        List<CommentResponseDTO> result = commentService.findByPostId(postId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(commentId);
        assertThat(result.get(0).content()).isEqualTo("Test comment");
        verify(commentRepository).findByPostIdOrderByCreatedAtDesc(postId);
    }

    @Test
    void findByPageId_ShouldReturnComments() {
        comment.setPostId(null);
        comment.setPageId(pageId);
        when(commentRepository.findByPageIdOrderByCreatedAtDesc(pageId))
                .thenReturn(Arrays.asList(comment));

        List<CommentResponseDTO> result = commentService.findByPageId(pageId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(commentId);
        assertThat(result.get(0).content()).isEqualTo("Test comment");
        verify(commentRepository).findByPageIdOrderByCreatedAtDesc(pageId);
    }

    @Test
    void createPostComment_ShouldCreateComment() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentResponseDTO result = commentService.createPostComment(userId, postId, "Test comment");

        assertThat(result.id()).isEqualTo(commentId);
        assertThat(result.content()).isEqualTo("Test comment");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createPageComment_ShouldCreateComment() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        comment.setPostId(null);
        comment.setPageId(pageId);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentResponseDTO result = commentService.createPageComment(userId, pageId, "Test comment");

        assertThat(result.id()).isEqualTo(commentId);
        assertThat(result.content()).isEqualTo("Test comment");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void updateComment_ShouldUpdateComment() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentResponseDTO result = commentService.updateComment(commentId, userId, "Updated comment");

        assertThat(result.id()).isEqualTo(commentId);
        assertThat(result.content()).isEqualTo("Updated comment");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void updateComment_ShouldThrowException_WhenUserNotAuthorized() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        UUID differentUserId = UUID.randomUUID();
        assertThatThrownBy(() -> commentService.updateComment(commentId, differentUserId, "Updated comment"))
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

        UUID differentUserId = UUID.randomUUID();
        assertThatThrownBy(() -> commentService.deleteComment(commentId, differentUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User is not authorized to delete this comment");
    }

    @Test
    void findByUserId_ShouldReturnComments() {
        when(commentRepository.findByUser_Id(userId))
                .thenReturn(Arrays.asList(comment));

        List<CommentResponseDTO> result = commentService.findByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(commentId);
        assertThat(result.get(0).content()).isEqualTo("Test comment");
        verify(commentRepository).findByUser_Id(userId);
    }

    @Test
    void createComment_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createPostComment(userId, postId, "Test comment"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found with id: " + userId);
    }
}

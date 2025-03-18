package de.muenchen.refarch.comment;

import de.muenchen.refarch.comment.dto.CommentResponseDTO;
import de.muenchen.refarch.security.Authorities;
import de.muenchen.refarch.user.User;
import de.muenchen.refarch.user.UserRepository;
import de.muenchen.refarch.user.dto.UserResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @PreAuthorize(Authorities.COMMENT_READ)
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> findByPostId(final UUID postId) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @PreAuthorize(Authorities.COMMENT_READ)
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> findByPageId(final UUID pageId) {
        return commentRepository.findByPageIdOrderByCreatedAtDesc(pageId)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @PreAuthorize(Authorities.COMMENT_READ)
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> findByUserId(final UUID userId) {
        return commentRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @PreAuthorize(Authorities.COMMENT_WRITE)
    @Transactional
    public CommentResponseDTO createPostComment(final UUID userId, final UUID postId, final String content) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        final Comment comment = new Comment();
        comment.setContent(content);
        comment.setPostId(postId);
        comment.setUser(user);

        return mapToResponseDTO(commentRepository.save(comment));
    }

    @PreAuthorize(Authorities.COMMENT_WRITE)
    @Transactional
    public CommentResponseDTO createPageComment(final UUID userId, final UUID pageId, final String content) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        final Comment comment = new Comment();
        comment.setContent(content);
        comment.setPageId(pageId);
        comment.setUser(user);

        return mapToResponseDTO(commentRepository.save(comment));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @userSecurity.isCurrentUser(#userId)")
    @Transactional
    public CommentResponseDTO updateComment(final UUID commentId, final UUID userId, final String content) {
        final Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + commentId));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("User is not authorized to update this comment");
        }

        comment.setContent(content);

        return mapToResponseDTO(commentRepository.save(comment));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @userSecurity.isCurrentUser(#userId)")
    @Transactional
    public void deleteComment(final UUID commentId, final UUID userId) {
        final Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + commentId));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("User is not authorized to delete this comment");
        }

        commentRepository.delete(comment);
    }

    private CommentResponseDTO mapToResponseDTO(final Comment comment) {
        final User user = comment.getUser();
        final UserResponseDTO userResponseDTO = new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getTitle(),
                user.getAffiliation(),
                user.getThumbnail(),
                user.getCreatedAt(),
                user.getUpdatedAt());

        return new CommentResponseDTO(
                comment.getId(),
                comment.getContent(),
                comment.getPostId(),
                comment.getPageId(),
                userResponseDTO,
                comment.getCreatedAt(),
                comment.getUpdatedAt());
    }
}

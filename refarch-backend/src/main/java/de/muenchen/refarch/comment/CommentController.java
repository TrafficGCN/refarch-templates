package de.muenchen.refarch.comment;

import de.muenchen.refarch.comment.dto.CommentRequestDTO;
import de.muenchen.refarch.comment.dto.CommentResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponseDTO>> getCommentsByPost(@PathVariable final UUID postId) {
        return ResponseEntity.ok(commentService.findByPostId(postId));
    }

    @GetMapping("/page/{pageId}")
    public ResponseEntity<List<CommentResponseDTO>> getCommentsByPage(@PathVariable final UUID pageId) {
        return ResponseEntity.ok(commentService.findByPageId(pageId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CommentResponseDTO>> getCommentsByUser(@PathVariable final UUID userId) {
        return ResponseEntity.ok(commentService.findByUserId(userId));
    }

    @PostMapping("/post/{postId}/user/{userId}")
    public ResponseEntity<CommentResponseDTO> createPostComment(
            @PathVariable final UUID postId,
            @PathVariable final UUID userId,
            @Valid @RequestBody final CommentRequestDTO request) {
        final CommentResponseDTO comment = commentService.createPostComment(userId, postId, request.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @PostMapping("/page/{pageId}/user/{userId}")
    public ResponseEntity<CommentResponseDTO> createPageComment(
            @PathVariable final UUID pageId,
            @PathVariable final UUID userId,
            @Valid @RequestBody final CommentRequestDTO request) {
        final CommentResponseDTO comment = commentService.createPageComment(userId, pageId, request.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @PutMapping("/{commentId}/user/{userId}")
    public ResponseEntity<CommentResponseDTO> updateComment(
            @PathVariable final UUID commentId,
            @PathVariable final UUID userId,
            @Valid @RequestBody final CommentRequestDTO request) {
        return ResponseEntity.ok(commentService.updateComment(commentId, userId, request.content()));
    }

    @DeleteMapping("/{commentId}/user/{userId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable final UUID commentId,
            @PathVariable final UUID userId) {
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}

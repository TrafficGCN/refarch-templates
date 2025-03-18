package de.muenchen.refarch.post;

import de.muenchen.refarch.post.dto.PostRequestDTO;
import de.muenchen.refarch.post.dto.PostResponseDTO;
import de.muenchen.refarch.post.content.dto.PostContentRequestDTO;
import de.muenchen.refarch.post.content.dto.PostContentResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @GetMapping
    public ResponseEntity<List<PostResponseDTO>> getAllPosts() {
        return ResponseEntity.ok(postService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDTO> getPostById(@PathVariable final UUID id) {
        return ResponseEntity.ok(postService.findById(id));
    }

    @PostMapping
    public ResponseEntity<PostResponseDTO> createPost(@Valid @RequestBody final PostRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponseDTO> updatePost(
            @PathVariable final UUID id,
            @Valid @RequestBody final PostRequestDTO request) {
        return ResponseEntity.ok(postService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable final UUID id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{postId}/content")
    public ResponseEntity<List<PostContentResponseDTO>> getAllPostContent(@PathVariable final UUID postId) {
        return ResponseEntity.ok(postService.findAllContentByPost(postId));
    }

    @GetMapping("/{postId}/content/{languageId}")
    public ResponseEntity<PostContentResponseDTO> getPostContent(
            @PathVariable final UUID postId,
            @PathVariable final UUID languageId) {
        return ResponseEntity.ok(postService.findContentByPostAndLanguage(postId, languageId));
    }

    @PostMapping("/{postId}/content")
    public ResponseEntity<PostContentResponseDTO> createPostContent(
            @PathVariable final UUID postId,
            @Valid @RequestBody final PostContentRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createContent(postId, request));
    }

    @PutMapping("/{postId}/content/{languageId}")
    public ResponseEntity<PostContentResponseDTO> updatePostContent(
            @PathVariable final UUID postId,
            @PathVariable final UUID languageId,
            @Valid @RequestBody final PostContentRequestDTO request) {
        return ResponseEntity.ok(postService.updateContent(postId, languageId, request));
    }

    @DeleteMapping("/{postId}/content/{languageId}")
    public ResponseEntity<Void> deletePostContent(
            @PathVariable final UUID postId,
            @PathVariable final UUID languageId) {
        postService.deleteContent(postId, languageId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<Void> publishPost(@PathVariable final UUID id) {
        postService.updatePublished(id, true);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/unpublish")
    public ResponseEntity<Void> unpublishPost(@PathVariable final UUID id) {
        postService.updatePublished(id, false);
        return ResponseEntity.noContent().build();
    }
}

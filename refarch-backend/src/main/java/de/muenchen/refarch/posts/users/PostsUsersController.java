package de.muenchen.refarch.posts.users;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts-users")
@RequiredArgsConstructor
public class PostsUsersController {
    private final PostsUsersService postsUsersService;

    @GetMapping("/post/{postLinkId}")
    public ResponseEntity<List<PostsUsers>> getUsersByPost(@PathVariable UUID postLinkId) {
        return ResponseEntity.ok(postsUsersService.findByPostLinkId(postLinkId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostsUsers>> getPostsByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(postsUsersService.findByUserId(userId));
    }

    @PostMapping("/{postLinkId}/user/{userId}")
    public ResponseEntity<PostsUsers> assignUserToPost(
            @PathVariable UUID postLinkId,
            @PathVariable UUID userId) {
        return ResponseEntity.ok(postsUsersService.assignUserToPost(postLinkId, userId));
    }

    @DeleteMapping("/{postLinkId}/user/{userId}")
    public ResponseEntity<Void> removeUserFromPost(
            @PathVariable UUID postLinkId,
            @PathVariable UUID userId) {
        postsUsersService.removeUserFromPost(postLinkId, userId);
        return ResponseEntity.noContent().build();
    }
}

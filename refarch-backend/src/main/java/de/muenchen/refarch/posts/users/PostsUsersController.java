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
    public ResponseEntity<List<PostsUsers>> getUsersByPost(@PathVariable final UUID postLinkId) {
        return ResponseEntity.ok(postsUsersService.findByPostLinkId(postLinkId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostsUsers>> getPostsByUser(@PathVariable final UUID userId) {
        return ResponseEntity.ok(postsUsersService.findByUserId(userId));
    }

    @PostMapping("/{postLinkId}/user/{userId}")
    public ResponseEntity<PostsUsers> assignUserToPost(
            @PathVariable final UUID postLinkId,
            @PathVariable final UUID userId) {
        return ResponseEntity.ok(postsUsersService.assignUserToPost(postLinkId, userId));
    }

    @DeleteMapping("/{postLinkId}/user/{userId}")
    public ResponseEntity<Void> removeUserFromPost(
            @PathVariable final UUID postLinkId,
            @PathVariable final UUID userId) {
        postsUsersService.removeUserFromPost(postLinkId, userId);
        return ResponseEntity.noContent().build();
    }
}

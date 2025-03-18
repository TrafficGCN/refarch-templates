package de.muenchen.refarch.pages.users;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pages-users")
@RequiredArgsConstructor
public class PagesUsersController {
    private final PagesUsersService pagesUsersService;

    @GetMapping("/page/{pageLinkId}")
    public ResponseEntity<List<PagesUsers>> getUsersByPage(@PathVariable final UUID pageLinkId) {
        return ResponseEntity.ok(pagesUsersService.findByPageLinkId(pageLinkId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PagesUsers>> getPagesByUser(@PathVariable final UUID userId) {
        return ResponseEntity.ok(pagesUsersService.findByUserId(userId));
    }

    @PostMapping("/{pageLinkId}/user/{userId}")
    public ResponseEntity<PagesUsers> assignUserToPage(
            @PathVariable final UUID pageLinkId,
            @PathVariable final UUID userId) {
        return ResponseEntity.ok(pagesUsersService.assignUserToPage(pageLinkId, userId));
    }

    @DeleteMapping("/{pageLinkId}/user/{userId}")
    public ResponseEntity<Void> removeUserFromPage(
            @PathVariable final UUID pageLinkId,
            @PathVariable final UUID userId) {
        pagesUsersService.removeUserFromPage(pageLinkId, userId);
        return ResponseEntity.noContent().build();
    }
}

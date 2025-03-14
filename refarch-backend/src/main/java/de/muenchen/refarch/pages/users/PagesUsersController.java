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
    public ResponseEntity<List<PagesUsers>> getUsersByPage(@PathVariable UUID pageLinkId) {
        return ResponseEntity.ok(pagesUsersService.findByPageLinkId(pageLinkId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PagesUsers>> getPagesByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(pagesUsersService.findByUserId(userId));
    }

    @PostMapping("/{pageLinkId}/user/{userId}")
    public ResponseEntity<PagesUsers> assignUserToPage(
            @PathVariable UUID pageLinkId,
            @PathVariable UUID userId) {
        return ResponseEntity.ok(pagesUsersService.assignUserToPage(pageLinkId, userId));
    }

    @DeleteMapping("/{pageLinkId}/user/{userId}")
    public ResponseEntity<Void> removeUserFromPage(
            @PathVariable UUID pageLinkId,
            @PathVariable UUID userId) {
        pagesUsersService.removeUserFromPage(pageLinkId, userId);
        return ResponseEntity.noContent().build();
    }
}

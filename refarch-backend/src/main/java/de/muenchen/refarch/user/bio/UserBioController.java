package de.muenchen.refarch.user.bio;

import de.muenchen.refarch.user.bio.dto.UserBioRequestDTO;
import de.muenchen.refarch.user.bio.dto.UserBioResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user-bios")
@RequiredArgsConstructor
public class UserBioController {
    private final UserBioService userBioService;

    @GetMapping
    public ResponseEntity<List<UserBioResponseDTO>> getAllUserBios() {
        return ResponseEntity.ok(userBioService.getAllUserBios());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserBioResponseDTO> getUserBioById(@PathVariable final UUID id) {
        return ResponseEntity.ok(userBioService.getUserBioById(id));
    }

    @GetMapping("/user/{userId}/language/{languageId}")
    public ResponseEntity<UserBioResponseDTO> getUserBioByUserIdAndLanguageId(
            @PathVariable final UUID userId,
            @PathVariable final UUID languageId) {
        return ResponseEntity.ok(userBioService.getUserBioByUserIdAndLanguageId(userId, languageId));
    }

    @PostMapping
    public ResponseEntity<UserBioResponseDTO> createUserBio(@Valid @RequestBody final UserBioRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userBioService.createUserBio(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserBioResponseDTO> updateUserBio(
            @PathVariable final UUID id,
            @Valid @RequestBody final UserBioRequestDTO request) {
        return ResponseEntity.ok(userBioService.updateUserBio(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserBio(@PathVariable final UUID id) {
        userBioService.deleteUserBio(id);
        return ResponseEntity.noContent().build();
    }
}

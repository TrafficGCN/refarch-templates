package de.muenchen.refarch.homepage;

import de.muenchen.refarch.homepage.content.dto.HomepageContentRequestDTO;
import de.muenchen.refarch.homepage.content.dto.HomepageContentResponseDTO;
import de.muenchen.refarch.homepage.dto.HomepageRequestDTO;
import de.muenchen.refarch.homepage.dto.HomepageResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/homepages")
@RequiredArgsConstructor
public class HomepageController {

    private final HomepageService homepageService;

    @GetMapping
    public ResponseEntity<List<HomepageResponseDTO>> getAllHomepages() {
        return ResponseEntity.ok(homepageService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HomepageResponseDTO> getHomepageById(@PathVariable final UUID id) {
        return ResponseEntity.ok(homepageService.findById(id));
    }

    @PostMapping
    public ResponseEntity<HomepageResponseDTO> createHomepage(@RequestBody @Valid final HomepageRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(homepageService.create(requestDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HomepageResponseDTO> updateHomepage(
            @PathVariable final UUID id,
            @RequestBody @Valid final HomepageRequestDTO requestDTO) {
        return ResponseEntity.ok(homepageService.update(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHomepage(@PathVariable final UUID id) {
        homepageService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{homepageId}/content")
    public ResponseEntity<List<HomepageContentResponseDTO>> getAllHomepageContent(
            @PathVariable final UUID homepageId) {
        return ResponseEntity.ok(homepageService.findAllContentByHomepage(homepageId));
    }

    @GetMapping("/{homepageId}/content/{languageId}")
    public ResponseEntity<HomepageContentResponseDTO> getHomepageContent(
            @PathVariable final UUID homepageId,
            @PathVariable final UUID languageId) {
        return ResponseEntity.ok(homepageService.findContentByHomepageAndLanguage(homepageId, languageId));
    }

    @PostMapping("/{homepageId}/content")
    public ResponseEntity<HomepageContentResponseDTO> createHomepageContent(
            @PathVariable final UUID homepageId,
            @RequestBody @Valid final HomepageContentRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(homepageService.createContent(homepageId, requestDTO));
    }

    @PutMapping("/{homepageId}/content/{languageId}")
    public ResponseEntity<HomepageContentResponseDTO> updateHomepageContent(
            @PathVariable final UUID homepageId,
            @PathVariable final UUID languageId,
            @RequestBody @Valid final HomepageContentRequestDTO requestDTO) {
        return ResponseEntity.ok(homepageService.updateContent(homepageId, languageId, requestDTO));
    }

    @DeleteMapping("/{homepageId}/content/{languageId}")
    public ResponseEntity<Void> deleteHomepageContent(
            @PathVariable final UUID homepageId,
            @PathVariable final UUID languageId) {
        homepageService.deleteContent(homepageId, languageId);
        return ResponseEntity.noContent().build();
    }
}

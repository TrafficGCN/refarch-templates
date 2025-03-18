package de.muenchen.refarch.page;

import de.muenchen.refarch.page.dto.PageRequestDTO;
import de.muenchen.refarch.page.dto.PageResponseDTO;
import de.muenchen.refarch.page.content.dto.PageContentRequestDTO;
import de.muenchen.refarch.page.content.dto.PageContentResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pages")
@RequiredArgsConstructor
public class PageController {
    private final PageService pageService;

    @GetMapping
    public ResponseEntity<List<PageResponseDTO>> getAllPages() {
        return ResponseEntity.ok(pageService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PageResponseDTO> getPageById(@PathVariable final UUID id) {
        return ResponseEntity.ok(pageService.findById(id));
    }

    @PostMapping
    public ResponseEntity<PageResponseDTO> createPage(@Valid @RequestBody final PageRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pageService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PageResponseDTO> updatePage(
            @PathVariable final UUID id,
            @Valid @RequestBody final PageRequestDTO request) {
        return ResponseEntity.ok(pageService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePage(@PathVariable final UUID id) {
        pageService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{pageId}/content")
    public ResponseEntity<List<PageContentResponseDTO>> getAllPageContent(@PathVariable final UUID pageId) {
        return ResponseEntity.ok(pageService.findAllContentByPage(pageId));
    }

    @GetMapping("/{pageId}/content/{languageId}")
    public ResponseEntity<PageContentResponseDTO> getPageContent(
            @PathVariable final UUID pageId,
            @PathVariable final UUID languageId) {
        return ResponseEntity.ok(pageService.findContentByPageAndLanguage(pageId, languageId));
    }

    @PostMapping("/{pageId}/content")
    public ResponseEntity<PageContentResponseDTO> createPageContent(
            @PathVariable final UUID pageId,
            @Valid @RequestBody final PageContentRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pageService.createContent(pageId, request));
    }

    @PutMapping("/{pageId}/content/{languageId}")
    public ResponseEntity<PageContentResponseDTO> updatePageContent(
            @PathVariable final UUID pageId,
            @PathVariable final UUID languageId,
            @Valid @RequestBody final PageContentRequestDTO request) {
        return ResponseEntity.ok(pageService.updateContent(pageId, languageId, request));
    }

    @DeleteMapping("/{pageId}/content/{languageId}")
    public ResponseEntity<Void> deletePageContent(
            @PathVariable final UUID pageId,
            @PathVariable final UUID languageId) {
        pageService.deleteContent(pageId, languageId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<Void> publishPage(@PathVariable final UUID id) {
        pageService.updatePublished(id, true);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/unpublish")
    public ResponseEntity<Void> unpublishPage(@PathVariable final UUID id) {
        pageService.updatePublished(id, false);
        return ResponseEntity.noContent().build();
    }
}

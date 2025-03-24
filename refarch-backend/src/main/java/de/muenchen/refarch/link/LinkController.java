package de.muenchen.refarch.link;

import de.muenchen.refarch.link.dto.LinkRequestDTO;
import de.muenchen.refarch.link.dto.LinkResponseDTO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/links")
@RequiredArgsConstructor
public class LinkController {

    private final LinkService linkService;

    @GetMapping("/internal")
    public ResponseEntity<List<LinkResponseDTO>> getInternalLinks() {
        return ResponseEntity.ok(linkService.getInternalLinks());
    }

    @GetMapping
    public ResponseEntity<List<LinkResponseDTO>> getAllLinks() {
        return ResponseEntity.ok(linkService.getAllLinks());
    }

    @PostMapping
    public ResponseEntity<LinkResponseDTO> createLink(@Valid @RequestBody final LinkRequestDTO request) {
        return ResponseEntity.ok(linkService.createLink(request));
    }
}

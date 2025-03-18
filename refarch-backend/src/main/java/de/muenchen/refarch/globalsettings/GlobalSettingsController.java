package de.muenchen.refarch.globalsettings;

import de.muenchen.refarch.globalsettings.dto.GlobalSettingsRequestDTO;
import de.muenchen.refarch.globalsettings.dto.GlobalSettingsResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class GlobalSettingsController {
    private final GlobalSettingsService globalSettingsService;

    @GetMapping
    public ResponseEntity<GlobalSettingsResponseDTO> getSettings() {
        try {
            return ResponseEntity.ok(globalSettingsService.getCurrentSettings());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping
    public ResponseEntity<GlobalSettingsResponseDTO> updateSettings(@Valid @RequestBody final GlobalSettingsRequestDTO request) {
        return ResponseEntity.ok(globalSettingsService.updateSettings(request));
    }
}

package de.muenchen.refarch.language;

import de.muenchen.refarch.language.dto.LanguageRequestDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LanguageServiceTest {

    @Mock
    private LanguageRepository languageRepository;

    @InjectMocks
    private LanguageService languageService;

    private Language language;
    private LanguageRequestDTO requestDTO;
    private UUID languageId;

    @BeforeEach
    void setUp() {
        languageId = UUID.randomUUID();
        language = new Language();
        language.setId(languageId);
        language.setName("English");
        language.setAbbreviation("en");
        language.setFontAwesomeIcon("fa-language");
        language.setMdiIcon("mdi-language");

        requestDTO = new LanguageRequestDTO(
                "English",
                "en",
                "fa-language",
                "mdi-language");
    }

    @Test
    void shouldReturnAllLanguages() {
        when(languageRepository.findAll()).thenReturn(List.of(language));

        final List<Language> result = languageService.getAllLanguages();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(language);
        verify(languageRepository).findAll();
    }

    @Test
    void shouldReturnLanguageWhenExists() {
        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));

        final Language result = languageService.getLanguageById(languageId);

        assertThat(result).isEqualTo(language);
        verify(languageRepository).findById(languageId);
    }

    @Test
    void shouldThrowExceptionWhenLanguageDoesNotExist() {
        when(languageRepository.findById(languageId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> languageService.getLanguageById(languageId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Language not found with id: " + languageId);
        verify(languageRepository).findById(languageId);
    }

    @Test
    void shouldCreateLanguageWhenAbbreviationDoesNotExist() {
        when(languageRepository.existsByAbbreviation(requestDTO.abbreviation())).thenReturn(false);
        when(languageRepository.save(any(Language.class))).thenReturn(language);

        final Language result = languageService.createLanguage(requestDTO);

        assertThat(result).isEqualTo(language);
        verify(languageRepository).existsByAbbreviation(requestDTO.abbreviation());
        verify(languageRepository).save(any(Language.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingLanguageWithExistingAbbreviation() {
        when(languageRepository.existsByAbbreviation(requestDTO.abbreviation())).thenReturn(true);

        assertThatThrownBy(() -> languageService.createLanguage(requestDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Language with abbreviation " + requestDTO.abbreviation() + " already exists");
        verify(languageRepository).existsByAbbreviation(requestDTO.abbreviation());
        verify(languageRepository, never()).save(any(Language.class));
    }

    @Test
    void shouldUpdateLanguageWhenExistsAndAbbreviationDoesNotExist() {
        language.setAbbreviation("old");
        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));
        when(languageRepository.existsByAbbreviation(requestDTO.abbreviation())).thenReturn(false);
        when(languageRepository.save(any(Language.class))).thenReturn(language);

        final Language result = languageService.updateLanguage(languageId, requestDTO);

        assertThat(result).isEqualTo(language);
        verify(languageRepository).findById(languageId);
        verify(languageRepository).existsByAbbreviation(requestDTO.abbreviation());
        verify(languageRepository).save(any(Language.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonexistentLanguage() {
        when(languageRepository.findById(languageId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> languageService.updateLanguage(languageId, requestDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Language not found with id: " + languageId);
        verify(languageRepository).findById(languageId);
        verify(languageRepository, never()).existsByAbbreviation(any());
        verify(languageRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithExistingAbbreviation() {
        language.setAbbreviation("old");
        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));
        when(languageRepository.existsByAbbreviation(requestDTO.abbreviation())).thenReturn(true);

        assertThatThrownBy(() -> languageService.updateLanguage(languageId, requestDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Language with abbreviation " + requestDTO.abbreviation() + " already exists");
        verify(languageRepository).findById(languageId);
        verify(languageRepository).existsByAbbreviation(requestDTO.abbreviation());
        verify(languageRepository, never()).save(any());
    }

    @Test
    void shouldDeleteLanguageWhenExists() {
        when(languageRepository.existsById(languageId)).thenReturn(true);
        doNothing().when(languageRepository).deleteById(languageId);

        languageService.deleteLanguage(languageId);

        verify(languageRepository).existsById(languageId);
        verify(languageRepository).deleteById(languageId);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonexistentLanguage() {
        when(languageRepository.existsById(languageId)).thenReturn(false);

        assertThatThrownBy(() -> languageService.deleteLanguage(languageId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Language not found with id: " + languageId);
        verify(languageRepository).existsById(languageId);
        verify(languageRepository, never()).deleteById(any());
    }
}

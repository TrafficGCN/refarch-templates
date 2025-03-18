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

    private static final String ENGLISH = "English";
    private static final String EN = "en";
    private static final String FA_LANGUAGE = "fa-language";
    private static final String MDI_LANGUAGE = "mdi-language";
    private static final String LANGUAGE_NOT_FOUND = "Language not found with id: ";
    private static final String LANGUAGE_EXISTS = "Language with abbreviation %s already exists";

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
        language.setName(ENGLISH);
        language.setAbbreviation(EN);
        language.setFontAwesomeIcon(FA_LANGUAGE);
        language.setMdiIcon(MDI_LANGUAGE);

        requestDTO = new LanguageRequestDTO(
                ENGLISH,
                EN,
                FA_LANGUAGE,
                MDI_LANGUAGE);
    }

    @Test
    void whenGettingAllLanguages_ShouldReturnList() {
        when(languageRepository.findAll()).thenReturn(List.of(language));

        final List<Language> result = languageService.getAllLanguages();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(language);
        verify(languageRepository).findAll();
    }

    @Test
    void whenGettingExistingLanguage_ShouldReturnLanguage() {
        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));

        final Language result = languageService.getLanguageById(languageId);

        assertThat(result).isEqualTo(language);
        verify(languageRepository).findById(languageId);
    }

    @Test
    void whenGettingNonexistentLanguage_ShouldThrowException() {
        when(languageRepository.findById(languageId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> languageService.getLanguageById(languageId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(LANGUAGE_NOT_FOUND + languageId);
        verify(languageRepository).findById(languageId);
    }

    @Test
    void whenCreatingLanguageWithNewAbbreviation_ShouldSucceed() {
        when(languageRepository.existsByAbbreviation(requestDTO.abbreviation())).thenReturn(false);
        when(languageRepository.save(any(Language.class))).thenReturn(language);

        final Language result = languageService.createLanguage(requestDTO);

        assertThat(result).isEqualTo(language);
        verify(languageRepository).existsByAbbreviation(requestDTO.abbreviation());
        verify(languageRepository).save(any(Language.class));
    }

    @Test
    void whenCreatingLanguageWithExistingAbbreviation_ShouldThrowException() {
        when(languageRepository.existsByAbbreviation(requestDTO.abbreviation())).thenReturn(true);

        assertThatThrownBy(() -> languageService.createLanguage(requestDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(String.format(LANGUAGE_EXISTS, requestDTO.abbreviation()));
        verify(languageRepository).existsByAbbreviation(requestDTO.abbreviation());
        verify(languageRepository, never()).save(any(Language.class));
    }

    @Test
    void whenUpdatingExistingLanguageWithNewAbbreviation_ShouldSucceed() {
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
    void whenUpdatingNonexistentLanguage_ShouldThrowException() {
        when(languageRepository.findById(languageId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> languageService.updateLanguage(languageId, requestDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(LANGUAGE_NOT_FOUND + languageId);
        verify(languageRepository).findById(languageId);
        verify(languageRepository, never()).existsByAbbreviation(any());
        verify(languageRepository, never()).save(any());
    }

    @Test
    void whenUpdatingWithExistingAbbreviation_ShouldThrowException() {
        language.setAbbreviation("old");
        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));
        when(languageRepository.existsByAbbreviation(requestDTO.abbreviation())).thenReturn(true);

        assertThatThrownBy(() -> languageService.updateLanguage(languageId, requestDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(String.format(LANGUAGE_EXISTS, requestDTO.abbreviation()));
        verify(languageRepository).findById(languageId);
        verify(languageRepository).existsByAbbreviation(requestDTO.abbreviation());
        verify(languageRepository, never()).save(any());
    }

    @Test
    void whenDeletingExistingLanguage_ShouldSucceed() {
        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));

        languageService.deleteLanguage(languageId);

        verify(languageRepository).findById(languageId);
        verify(languageRepository).delete(language);
    }

    @Test
    void whenDeletingNonexistentLanguage_ShouldThrowException() {
        when(languageRepository.findById(languageId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> languageService.deleteLanguage(languageId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(LANGUAGE_NOT_FOUND + languageId);
        verify(languageRepository).findById(languageId);
        verify(languageRepository, never()).delete(any());
    }
}

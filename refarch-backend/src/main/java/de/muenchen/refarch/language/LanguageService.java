package de.muenchen.refarch.language;

import de.muenchen.refarch.language.dto.LanguageRequestDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LanguageService {
    private final LanguageRepository languageRepository;

    @Transactional(readOnly = true)
    public List<Language> getAllLanguages() {
        return languageRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Language getLanguageById(final UUID id) {
        return languageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Language not found with id: " + id));
    }

    @Transactional
    public Language createLanguage(final LanguageRequestDTO request) {
        if (languageRepository.existsByAbbreviation(request.abbreviation())) {
            throw new IllegalArgumentException("Language with abbreviation " + request.abbreviation() + " already exists");
        }

        final Language language = new Language();
        language.setName(request.name());
        language.setAbbreviation(request.abbreviation());
        language.setFontAwesomeIcon(request.fontAwesomeIcon());
        language.setMdiIcon(request.mdiIcon());

        return languageRepository.save(language);
    }

    @Transactional
    public Language updateLanguage(final UUID id, final LanguageRequestDTO request) {
        final Language language = getLanguageById(id);

        if (!language.getAbbreviation().equals(request.abbreviation()) &&
                languageRepository.existsByAbbreviation(request.abbreviation())) {
            throw new IllegalArgumentException("Language with abbreviation " + request.abbreviation() + " already exists");
        }

        language.setName(request.name());
        language.setAbbreviation(request.abbreviation());
        language.setFontAwesomeIcon(request.fontAwesomeIcon());
        language.setMdiIcon(request.mdiIcon());

        return languageRepository.save(language);
    }

    @Transactional
    public void deleteLanguage(final UUID id) {
        if (!languageRepository.existsById(id)) {
            throw new EntityNotFoundException("Language not found with id: " + id);
        }
        languageRepository.deleteById(id);
    }
}

package de.muenchen.refarch.user.bio;

import de.muenchen.refarch.language.Language;
import de.muenchen.refarch.language.LanguageRepository;
import de.muenchen.refarch.user.User;
import de.muenchen.refarch.user.UserRepository;
import de.muenchen.refarch.user.bio.dto.UserBioRequestDTO;
import de.muenchen.refarch.user.bio.dto.UserBioResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserBioService {
    private final UserBioRepository userBioRepository;
    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;

    @Transactional(readOnly = true)
    public List<UserBioResponseDTO> getAllUserBios() {
        return userBioRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserBioResponseDTO getUserBioById(final UUID id) {
        return userBioRepository.findById(id)
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException("User bio not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public UserBioResponseDTO getUserBioByUserIdAndLanguageId(final UUID userId, final UUID languageId) {
        return userBioRepository.findByUserIdAndLanguageId(userId, languageId)
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("User bio not found for user %s and language %s", userId, languageId)));
    }

    @Transactional
    public UserBioResponseDTO createUserBio(final UserBioRequestDTO request) {
        if (userBioRepository.existsByUserIdAndLanguageId(request.userId(), request.languageId())) {
            throw new IllegalArgumentException(
                    String.format("Bio already exists for user %s and language %s", request.userId(), request.languageId()));
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.userId()));

        Language language = languageRepository.findById(request.languageId())
                .orElseThrow(() -> new EntityNotFoundException("Language not found with id: " + request.languageId()));

        UserBio userBio = new UserBio();
        userBio.setUser(user);
        userBio.setLanguage(language);
        userBio.setBio(request.bio());

        return mapToResponseDTO(userBioRepository.save(userBio));
    }

    @Transactional
    public UserBioResponseDTO updateUserBio(UUID id, UserBioRequestDTO requestDTO) {
        UserBio userBio = userBioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User bio not found with id: " + id));

        // Check if the new user-language combination already exists (excluding current bio)
        if (!userBio.getUser().getId().equals(requestDTO.userId()) ||
                !userBio.getLanguage().getId().equals(requestDTO.languageId())) {
            if (userBioRepository.existsByUserIdAndLanguageId(requestDTO.userId(), requestDTO.languageId())) {
                throw new IllegalArgumentException(
                        String.format("Bio already exists for user %s and language %s",
                                requestDTO.userId(), requestDTO.languageId()));
            }
        }

        User user = userRepository.findById(requestDTO.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + requestDTO.userId()));

        Language language = languageRepository.findById(requestDTO.languageId())
                .orElseThrow(() -> new EntityNotFoundException("Language not found with id: " + requestDTO.languageId()));

        userBio.setUser(user);
        userBio.setLanguage(language);
        userBio.setBio(requestDTO.bio());

        UserBio savedBio = userBioRepository.save(userBio);
        return mapToResponseDTO(savedBio);
    }

    @Transactional
    public void deleteUserBio(final UUID id) {
        if (!userBioRepository.existsById(id)) {
            throw new EntityNotFoundException("User bio not found with id: " + id);
        }
        userBioRepository.deleteById(id);
    }

    private UserBioResponseDTO mapToResponseDTO(final UserBio userBio) {
        return new UserBioResponseDTO(
                userBio.getId(),
                userBio.getUser().getId(),
                userBio.getLanguage().getId(),
                userBio.getBio(),
                userBio.getCreatedAt(),
                userBio.getUpdatedAt());
    }
}

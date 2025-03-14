package de.muenchen.refarch.user.bio;

import de.muenchen.refarch.language.Language;
import de.muenchen.refarch.language.LanguageRepository;
import de.muenchen.refarch.user.User;
import de.muenchen.refarch.user.UserRepository;
import de.muenchen.refarch.user.bio.dto.UserBioRequestDTO;
import de.muenchen.refarch.user.bio.dto.UserBioResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserBioServiceTest {

    @Mock
    private UserBioRepository userBioRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LanguageRepository languageRepository;

    @InjectMocks
    private UserBioService userBioService;

    private UUID userId;
    private UUID languageId;
    private UUID bioId;
    private User user;
    private Language language;
    private UserBio userBio;
    private UserBioRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        languageId = UUID.randomUUID();
        bioId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setUsername("testuser");

        language = new Language();
        language.setId(languageId);
        language.setName("English");
        language.setAbbreviation("en");

        userBio = new UserBio();
        userBio.setId(bioId);
        userBio.setUser(user);
        userBio.setLanguage(language);
        userBio.setBio("Test bio");
        userBio.setCreatedAt(LocalDateTime.now());
        userBio.setUpdatedAt(LocalDateTime.now());

        requestDTO = new UserBioRequestDTO(userId, languageId, "Test bio");
    }

    @Test
    void getAllUserBios_ShouldReturnAllBios() {
        when(userBioRepository.findAll()).thenReturn(List.of(userBio));

        List<UserBioResponseDTO> result = userBioService.getAllUserBios();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(bioId);
        assertThat(result.get(0).userId()).isEqualTo(userId);
        assertThat(result.get(0).languageId()).isEqualTo(languageId);
        assertThat(result.get(0).bio()).isEqualTo("Test bio");
    }

    @Test
    void getUserBioById_WhenBioExists_ShouldReturnBio() {
        when(userBioRepository.findById(bioId)).thenReturn(Optional.of(userBio));

        UserBioResponseDTO result = userBioService.getUserBioById(bioId);

        assertThat(result.id()).isEqualTo(bioId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.languageId()).isEqualTo(languageId);
        assertThat(result.bio()).isEqualTo("Test bio");
    }

    @Test
    void getUserBioById_WhenBioDoesNotExist_ShouldThrowException() {
        when(userBioRepository.findById(bioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userBioService.getUserBioById(bioId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User bio not found with id: " + bioId);
    }

    @Test
    void getUserBioByUserIdAndLanguageId_WhenBioExists_ShouldReturnBio() {
        when(userBioRepository.findByUserIdAndLanguageId(userId, languageId))
                .thenReturn(Optional.of(userBio));

        UserBioResponseDTO result = userBioService.getUserBioByUserIdAndLanguageId(userId, languageId);

        assertThat(result.id()).isEqualTo(bioId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.languageId()).isEqualTo(languageId);
        assertThat(result.bio()).isEqualTo("Test bio");
    }

    @Test
    void getUserBioByUserIdAndLanguageId_WhenBioDoesNotExist_ShouldThrowException() {
        when(userBioRepository.findByUserIdAndLanguageId(userId, languageId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userBioService.getUserBioByUserIdAndLanguageId(userId, languageId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User bio not found for user")
                .hasMessageContaining("and language");
    }

    @Test
    void createUserBio_WhenUserAndLanguageExist_ShouldCreateBio() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));
        when(userBioRepository.existsByUserIdAndLanguageId(userId, languageId)).thenReturn(false);
        when(userBioRepository.save(any(UserBio.class))).thenReturn(userBio);

        UserBioResponseDTO result = userBioService.createUserBio(requestDTO);

        assertThat(result.id()).isEqualTo(bioId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.languageId()).isEqualTo(languageId);
        assertThat(result.bio()).isEqualTo("Test bio");
        verify(userBioRepository).save(any(UserBio.class));
    }

    @Test
    void createUserBio_WhenBioAlreadyExists_ShouldThrowException() {
        when(userBioRepository.existsByUserIdAndLanguageId(userId, languageId)).thenReturn(true);

        assertThatThrownBy(() -> userBioService.createUserBio(requestDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bio already exists for user");
    }

    @Test
    void createUserBio_WhenUserDoesNotExist_ShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userBioService.createUserBio(requestDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found with id: " + userId);
    }

    @Test
    void createUserBio_WhenLanguageDoesNotExist_ShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.findById(languageId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userBioService.createUserBio(requestDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Language not found with id: " + languageId);
    }

    @Test
    void updateUserBio_WhenBioExists_ShouldUpdateBio() {
        when(userBioRepository.findById(bioId)).thenReturn(Optional.of(userBio));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));
        when(userBioRepository.save(any(UserBio.class))).thenReturn(userBio);

        UserBioResponseDTO result = userBioService.updateUserBio(bioId, requestDTO);

        assertThat(result.id()).isEqualTo(bioId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.languageId()).isEqualTo(languageId);
        assertThat(result.bio()).isEqualTo("Test bio");
        verify(userBioRepository).save(any(UserBio.class));
    }

    @Test
    void updateUserBio_WhenBioDoesNotExist_ShouldThrowException() {
        when(userBioRepository.findById(bioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userBioService.updateUserBio(bioId, requestDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User bio not found with id: " + bioId);
    }

    @Test
    void updateUserBio_WhenNewUserLanguageCombinationExists_ShouldThrowException() {
        // Create a different user and language for the existing bio
        User existingUser = new User();
        existingUser.setId(UUID.randomUUID());
        Language existingLanguage = new Language();
        existingLanguage.setId(UUID.randomUUID());

        // Set up the existing bio with different user/language
        UserBio existingBio = new UserBio();
        existingBio.setId(bioId);
        existingBio.setUser(existingUser);
        existingBio.setLanguage(existingLanguage);
        existingBio.setBio("Existing bio");

        // Create request with new user/language combination
        UserBioRequestDTO updateRequest = new UserBioRequestDTO(userId, languageId, "Updated bio");

        when(userBioRepository.findById(bioId)).thenReturn(Optional.of(existingBio));
        when(userBioRepository.existsByUserIdAndLanguageId(userId, languageId)).thenReturn(true);

        assertThatThrownBy(() -> userBioService.updateUserBio(bioId, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bio already exists for user");
    }

    @Test
    void deleteUserBio_WhenBioExists_ShouldDeleteBio() {
        when(userBioRepository.existsById(bioId)).thenReturn(true);

        userBioService.deleteUserBio(bioId);

        verify(userBioRepository).deleteById(bioId);
    }

    @Test
    void deleteUserBio_WhenBioDoesNotExist_ShouldThrowException() {
        when(userBioRepository.existsById(bioId)).thenReturn(false);

        assertThatThrownBy(() -> userBioService.deleteUserBio(bioId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User bio not found with id: " + bioId);
        verify(userBioRepository, never()).deleteById(any());
    }
}

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

    private static final String TEST_BIO = "Test bio";
    private static final String TEST_USERNAME = "testuser";
    private static final String ENGLISH = "English";
    private static final String EN = "en";
    private static final String BIO_NOT_FOUND_WITH_ID = "User bio not found with id: ";
    private static final String BIO_ALREADY_EXISTS = "Bio already exists for user";
    private static final String USER_NOT_FOUND = "User not found with id: ";
    private static final String LANGUAGE_NOT_FOUND = "Language not found with id: ";

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
        user.setUsername(TEST_USERNAME);

        language = new Language();
        language.setId(languageId);
        language.setName(ENGLISH);
        language.setAbbreviation(EN);

        userBio = new UserBio();
        userBio.setId(bioId);
        userBio.setUser(user);
        userBio.setLanguage(language);
        userBio.setBio(TEST_BIO);
        userBio.setCreatedAt(LocalDateTime.now());
        userBio.setUpdatedAt(LocalDateTime.now());

        requestDTO = new UserBioRequestDTO(userId, languageId, TEST_BIO);
    }

    @Test
    void shouldFetchAllBios() {
        when(userBioRepository.findAll()).thenReturn(List.of(userBio));

        final List<UserBioResponseDTO> result = userBioService.getAllUserBios();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(bioId);
        assertThat(result.get(0).userId()).isEqualTo(userId);
        assertThat(result.get(0).languageId()).isEqualTo(languageId);
        assertThat(result.get(0).bio()).isEqualTo(TEST_BIO);
    }

    @Test
    void shouldFindExistingBio() {
        when(userBioRepository.findById(bioId)).thenReturn(Optional.of(userBio));

        final UserBioResponseDTO result = userBioService.getUserBioById(bioId);

        assertThat(result.id()).isEqualTo(bioId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.languageId()).isEqualTo(languageId);
        assertThat(result.bio()).isEqualTo(TEST_BIO);
    }

    @Test
    void shouldFailWhenBioNotFound() {
        when(userBioRepository.findById(bioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userBioService.getUserBioById(bioId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(BIO_NOT_FOUND_WITH_ID + bioId);
    }

    @Test
    void shouldFindBioByUserAndLanguage() {
        when(userBioRepository.findByUserIdAndLanguageId(userId, languageId))
                .thenReturn(Optional.of(userBio));

        final UserBioResponseDTO result = userBioService.getUserBioByUserIdAndLanguageId(userId, languageId);

        assertThat(result.id()).isEqualTo(bioId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.languageId()).isEqualTo(languageId);
        assertThat(result.bio()).isEqualTo(TEST_BIO);
    }

    @Test
    void shouldFailWhenBioNotFoundByUserAndLanguage() {
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

        final UserBioResponseDTO result = userBioService.createUserBio(requestDTO);

        assertThat(result.id()).isEqualTo(bioId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.languageId()).isEqualTo(languageId);
        assertThat(result.bio()).isEqualTo(TEST_BIO);
        verify(userBioRepository).save(any(UserBio.class));
    }

    @Test
    void createUserBio_WhenBioAlreadyExists_ShouldThrowException() {
        when(userBioRepository.existsByUserIdAndLanguageId(userId, languageId)).thenReturn(true);

        assertThatThrownBy(() -> userBioService.createUserBio(requestDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(BIO_ALREADY_EXISTS);
    }

    @Test
    void createUserBio_WhenUserDoesNotExist_ShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userBioService.createUserBio(requestDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(USER_NOT_FOUND + userId);
    }

    @Test
    void createUserBio_WhenLanguageDoesNotExist_ShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.findById(languageId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userBioService.createUserBio(requestDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(LANGUAGE_NOT_FOUND + languageId);
    }

    @Test
    void updateUserBio_WhenBioExists_ShouldUpdateBio() {
        when(userBioRepository.findById(bioId)).thenReturn(Optional.of(userBio));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));
        when(userBioRepository.save(any(UserBio.class))).thenReturn(userBio);

        final UserBioResponseDTO result = userBioService.updateUserBio(bioId, requestDTO);

        assertThat(result.id()).isEqualTo(bioId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.languageId()).isEqualTo(languageId);
        assertThat(result.bio()).isEqualTo(TEST_BIO);
        verify(userBioRepository).save(any(UserBio.class));
    }

    @Test
    void updateUserBio_WhenBioDoesNotExist_ShouldThrowException() {
        when(userBioRepository.findById(bioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userBioService.updateUserBio(bioId, requestDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(BIO_NOT_FOUND_WITH_ID + bioId);
    }

    @Test
    void updateUserBio_WhenNewUserLanguageCombinationExists_ShouldThrowException() {
        // Create a different user and language for the existing bio
        final User existingUser = new User();
        existingUser.setId(UUID.randomUUID());
        final Language existingLanguage = new Language();
        existingLanguage.setId(UUID.randomUUID());

        // Set up the existing bio with different user/language
        final UserBio existingBio = new UserBio();
        existingBio.setId(bioId);
        existingBio.setUser(existingUser);
        existingBio.setLanguage(existingLanguage);
        existingBio.setBio("Existing bio");

        // Create request with new user/language combination
        final UserBioRequestDTO updateRequest = new UserBioRequestDTO(userId, languageId, "Updated bio");

        when(userBioRepository.findById(bioId)).thenReturn(Optional.of(existingBio));
        when(userBioRepository.existsByUserIdAndLanguageId(userId, languageId)).thenReturn(true);

        assertThatThrownBy(() -> userBioService.updateUserBio(bioId, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(BIO_ALREADY_EXISTS);
    }

    @Test
    void deleteUserBio_WhenBioExists_ShouldDeleteBio() {
        when(userBioRepository.existsById(bioId)).thenReturn(true);

        userBioService.deleteUserBio(bioId);

        verify(userBioRepository).existsById(bioId);
        verify(userBioRepository).deleteById(bioId);
    }

    @Test
    void deleteUserBio_WhenBioDoesNotExist_ShouldThrowException() {
        when(userBioRepository.existsById(bioId)).thenReturn(false);

        assertThatThrownBy(() -> userBioService.deleteUserBio(bioId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(BIO_NOT_FOUND_WITH_ID + bioId);
    }
}

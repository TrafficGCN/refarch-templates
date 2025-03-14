package de.muenchen.refarch.user;

import de.muenchen.refarch.user.dto.UserRequestDTO;
import de.muenchen.refarch.user.dto.UserResponseDTO;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserRequestDTO requestDTO;
    private UserResponseDTO responseDTO;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setTitle("Dr.");
        user.setAffiliation("Test Department");
        user.setThumbnail("thumbnail.jpg");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        requestDTO = new UserRequestDTO(
                "testuser",
                "test@example.com",
                "Test",
                "User",
                "Dr.",
                "Test Department",
                "thumbnail.jpg");

        responseDTO = new UserResponseDTO(
                userId,
                "testuser",
                "test@example.com",
                "Test",
                "User",
                "Dr.",
                "Test Department",
                "thumbnail.jpg",
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    @Test
    void shouldReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        final List<UserResponseDTO> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(responseDTO);
        verify(userRepository).findAll();
    }

    @Test
    void shouldReturnUserWhenExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        final UserResponseDTO result = userService.getUserById(userId);

        assertThat(result).isEqualTo(responseDTO);
        verify(userRepository).findById(userId);
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found with id: " + userId);
        verify(userRepository).findById(userId);
    }

    @Test
    void shouldCreateUserWhenUsernameAndEmailDoNotExist() {
        when(userRepository.existsByUsername(requestDTO.username())).thenReturn(false);
        when(userRepository.existsByEmail(requestDTO.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        final UserResponseDTO result = userService.createUser(requestDTO);

        assertThat(result).isEqualTo(responseDTO);
        verify(userRepository).existsByUsername(requestDTO.username());
        verify(userRepository).existsByEmail(requestDTO.email());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingUserWithExistingUsername() {
        when(userRepository.existsByUsername(requestDTO.username())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(requestDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User with username " + requestDTO.username() + " already exists");
        verify(userRepository).existsByUsername(requestDTO.username());
        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenCreatingUserWithExistingEmail() {
        when(userRepository.existsByUsername(requestDTO.username())).thenReturn(false);
        when(userRepository.existsByEmail(requestDTO.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(requestDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User with email " + requestDTO.email() + " already exists");
        verify(userRepository).existsByUsername(requestDTO.username());
        verify(userRepository).existsByEmail(requestDTO.email());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldUpdateUserWhenExistsAndUsernameAndEmailDoNotExist() {
        // Create a request with different username and email
        UserRequestDTO updateRequest = new UserRequestDTO(
                "newusername",
                "newemail@example.com",
                requestDTO.firstName(),
                requestDTO.lastName(),
                requestDTO.title(),
                requestDTO.affiliation(),
                requestDTO.thumbnail());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername(updateRequest.username())).thenReturn(false);
        when(userRepository.existsByEmail(updateRequest.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        final UserResponseDTO result = userService.updateUser(userId, updateRequest);

        assertThat(result).isEqualTo(responseDTO);
        verify(userRepository).findById(userId);
        verify(userRepository).existsByUsername(updateRequest.username());
        verify(userRepository).existsByEmail(updateRequest.email());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonexistentUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(userId, requestDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found with id: " + userId);
        verify(userRepository).findById(userId);
        verify(userRepository, never()).existsByUsername(any());
        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithExistingUsername() {
        // Create a request with a different username
        UserRequestDTO updateRequest = new UserRequestDTO(
                "newusername",
                requestDTO.email(),
                requestDTO.firstName(),
                requestDTO.lastName(),
                requestDTO.title(),
                requestDTO.affiliation(),
                requestDTO.thumbnail());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername(updateRequest.username())).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(userId, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User with username " + updateRequest.username() + " already exists");
        verify(userRepository).findById(userId);
        verify(userRepository).existsByUsername(updateRequest.username());
        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithExistingEmail() {
        // Create a request with a different email
        UserRequestDTO updateRequest = new UserRequestDTO(
                requestDTO.username(),
                "newemail@example.com",
                requestDTO.firstName(),
                requestDTO.lastName(),
                requestDTO.title(),
                requestDTO.affiliation(),
                requestDTO.thumbnail());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername(updateRequest.username())).thenReturn(false);
        when(userRepository.existsByEmail(updateRequest.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(userId, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User with email " + updateRequest.email() + " already exists");
        verify(userRepository).findById(userId);
        verify(userRepository).existsByUsername(updateRequest.username());
        verify(userRepository).existsByEmail(updateRequest.email());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldDeleteUserWhenExists() {
        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);

        userService.deleteUser(userId);

        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonexistentUser() {
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found with id: " + userId);
        verify(userRepository).existsById(userId);
        verify(userRepository, never()).deleteById(any());
    }
}

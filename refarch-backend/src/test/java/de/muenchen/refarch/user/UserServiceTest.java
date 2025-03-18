package de.muenchen.refarch.user;

import de.muenchen.refarch.role.Role;
import de.muenchen.refarch.role.RoleRepository;
import de.muenchen.refarch.user.dto.UserRequestDTO;
import de.muenchen.refarch.user.dto.UserResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserRequestDTO requestDTO;
    private UserResponseDTO responseDTO;
    private UUID userId;
    private Role defaultRole;

    private static final String TEST_PASSWORD = "testPassword123";
    private static final String ALREADY_EXISTS_SUFFIX = " already exists";

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(TEST_PASSWORD);
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
                TEST_PASSWORD,
                "Test",
                "User",
                "Dr.",
                "Test Department",
                "thumbnail.jpg");

        responseDTO = new UserResponseDTO(
                userId,
                "testuser",
                "Test",
                "User",
                "Dr.",
                "Test Department",
                "thumbnail.jpg",
                user.getCreatedAt(),
                user.getUpdatedAt());

        defaultRole = new Role();
        defaultRole.setId(UUID.randomUUID());
        defaultRole.setName("ROLE_USER");
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
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        final UserResponseDTO result = userService.createUser(requestDTO);

        assertThat(result).isEqualTo(responseDTO);
        verify(userRepository).existsByUsername(requestDTO.username());
        verify(userRepository).existsByEmail(requestDTO.email());
        verify(roleRepository).findByName("ROLE_USER");
        verify(passwordEncoder).encode(requestDTO.password());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingUserWithExistingUsername() {
        when(userRepository.existsByUsername(requestDTO.username())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(requestDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User with username " + requestDTO.username() + ALREADY_EXISTS_SUFFIX);
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
                .hasMessage("User with email " + requestDTO.email() + ALREADY_EXISTS_SUFFIX);
        verify(userRepository).existsByUsername(requestDTO.username());
        verify(userRepository).existsByEmail(requestDTO.email());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldUpdateUserWhenExistsAndUsernameAndEmailDoNotExist() {
        // Create a request with different username and email
        final UserRequestDTO updateRequest = new UserRequestDTO(
                "newusername",
                "newemail@example.com",
                TEST_PASSWORD,
                requestDTO.firstName(),
                requestDTO.lastName(),
                requestDTO.title(),
                requestDTO.affiliation(),
                requestDTO.thumbnail());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername(updateRequest.username())).thenReturn(false);
        when(userRepository.existsByEmail(updateRequest.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        final UserResponseDTO result = userService.updateUser(userId, updateRequest);

        assertThat(result).isEqualTo(responseDTO);
        verify(userRepository).findById(userId);
        verify(userRepository).existsByUsername(updateRequest.username());
        verify(userRepository).existsByEmail(updateRequest.email());
        verify(passwordEncoder).encode(updateRequest.password());
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
        final UserRequestDTO updateRequest = new UserRequestDTO(
                "newusername",
                requestDTO.email(),
                TEST_PASSWORD,
                requestDTO.firstName(),
                requestDTO.lastName(),
                requestDTO.title(),
                requestDTO.affiliation(),
                requestDTO.thumbnail());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername(updateRequest.username())).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(userId, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User with username " + updateRequest.username() + ALREADY_EXISTS_SUFFIX);
        verify(userRepository).findById(userId);
        verify(userRepository).existsByUsername(updateRequest.username());
        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithExistingEmail() {
        // Create a request with a different email
        final UserRequestDTO updateRequest = new UserRequestDTO(
                requestDTO.username(),
                "newemail@example.com",
                TEST_PASSWORD,
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
                .hasMessage("User with email " + updateRequest.email() + ALREADY_EXISTS_SUFFIX);
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

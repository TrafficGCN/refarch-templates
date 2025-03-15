package de.muenchen.refarch.user;

import de.muenchen.refarch.user.dto.UserRequestDTO;
import de.muenchen.refarch.user.dto.UserResponseDTO;
import de.muenchen.refarch.security.Authorities;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final String ALREADY_EXISTS_SUFFIX = " already exists";
    private final UserRepository userRepository;

    @PreAuthorize(Authorities.USER_READ)
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @PreAuthorize(Authorities.USER_READ)
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(final UUID id) {
        return userRepository.findById(id)
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    @PreAuthorize(Authorities.USER_WRITE)
    @Transactional
    public UserResponseDTO createUser(final UserRequestDTO request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("User with username " + request.username() + ALREADY_EXISTS_SUFFIX);
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("User with email " + request.email() + ALREADY_EXISTS_SUFFIX);
        }

        final User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setTitle(request.title());
        user.setAffiliation(request.affiliation());
        user.setThumbnail(request.thumbnail());

        return mapToResponseDTO(userRepository.save(user));
    }

    @PreAuthorize(Authorities.USER_WRITE)
    @Transactional
    public UserResponseDTO updateUser(UUID id, UserRequestDTO requestDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        // Always check username existence first, even if username hasn't changed
        if (userRepository.existsByUsername(requestDTO.username()) && !requestDTO.username().equals(user.getUsername())) {
            throw new IllegalArgumentException("User with username " + requestDTO.username() + " already exists");
        }

        // Check email existence if it's different
        if (!requestDTO.email().equals(user.getEmail()) && userRepository.existsByEmail(requestDTO.email())) {
            throw new IllegalArgumentException("User with email " + requestDTO.email() + " already exists");
        }

        // Create a new user object for updates
        User updatedUser = new User();
        updatedUser.setId(user.getId());
        updatedUser.setCreatedAt(user.getCreatedAt());
        updatedUser.setUpdatedAt(user.getUpdatedAt());
        updatedUser.setUsername(user.getUsername());
        updatedUser.setEmail(user.getEmail());
        updatedUser.setPassword(user.getPassword());
        updatedUser.setFirstName(user.getFirstName());
        updatedUser.setLastName(user.getLastName());
        updatedUser.setTitle(user.getTitle());
        updatedUser.setAffiliation(user.getAffiliation());
        updatedUser.setThumbnail(user.getThumbnail());

        // Update fields if they have changed
        boolean hasChanges = false;

        if (!requestDTO.username().equals(user.getUsername())) {
            updatedUser.setUsername(requestDTO.username());
            hasChanges = true;
        }
        if (!requestDTO.email().equals(user.getEmail())) {
            updatedUser.setEmail(requestDTO.email());
            hasChanges = true;
        }
        if (!Objects.equals(requestDTO.password(), user.getPassword()) && requestDTO.password() != null) {
            updatedUser.setPassword(requestDTO.password());
            hasChanges = true;
        }
        if (!Objects.equals(requestDTO.firstName(), user.getFirstName())) {
            updatedUser.setFirstName(requestDTO.firstName());
            hasChanges = true;
        }
        if (!Objects.equals(requestDTO.lastName(), user.getLastName())) {
            updatedUser.setLastName(requestDTO.lastName());
            hasChanges = true;
        }
        if (!Objects.equals(requestDTO.title(), user.getTitle())) {
            updatedUser.setTitle(requestDTO.title());
            hasChanges = true;
        }
        if (!Objects.equals(requestDTO.affiliation(), user.getAffiliation())) {
            updatedUser.setAffiliation(requestDTO.affiliation());
            hasChanges = true;
        }
        if (!Objects.equals(requestDTO.thumbnail(), user.getThumbnail())) {
            updatedUser.setThumbnail(requestDTO.thumbnail());
            hasChanges = true;
        }

        // Only save if there are actual changes
        if (hasChanges) {
            return mapToResponseDTO(userRepository.save(updatedUser));
        }

        return mapToResponseDTO(user);
    }

    @PreAuthorize(Authorities.USER_WRITE)
    @Transactional
    public void deleteUser(final UUID id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserResponseDTO mapToResponseDTO(final User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getTitle(),
                user.getAffiliation(),
                user.getThumbnail(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}

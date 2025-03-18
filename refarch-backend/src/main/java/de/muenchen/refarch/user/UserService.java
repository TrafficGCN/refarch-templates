package de.muenchen.refarch.user;

import de.muenchen.refarch.user.dto.UserRequestDTO;
import de.muenchen.refarch.user.dto.UserResponseDTO;
import de.muenchen.refarch.security.Authorities;
import de.muenchen.refarch.role.Role;
import de.muenchen.refarch.role.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final String ALREADY_EXISTS_SUFFIX = " already exists";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

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

    //@PreAuthorize(Authorities.USER_WRITE)
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
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setTitle(request.title());
        user.setAffiliation(request.affiliation());
        user.setThumbnail(request.thumbnail());

        // Assign default ROLE_USER role
        final Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("Default ROLE_USER not found"));
        user.setRoles(Set.of(userRole));

        return mapToResponseDTO(userRepository.save(user));
    }

    private void validateUserUpdate(final User existingUser, final UserRequestDTO requestDTO) {
        // Check username existence if it's different
        if (userRepository.existsByUsername(requestDTO.username()) && !Objects.equals(requestDTO.username(), existingUser.getUsername())) {
            throw new IllegalArgumentException("User with username " + requestDTO.username() + " already exists");
        }

        // Check email existence if it's different
        if (!Objects.equals(requestDTO.email(), existingUser.getEmail()) && userRepository.existsByEmail(requestDTO.email())) {
            throw new IllegalArgumentException("User with email " + requestDTO.email() + " already exists");
        }
    }

    private User createUpdatedUser(final User existingUser, final UserRequestDTO requestDTO) {
        final User updatedUser = initializeUpdatedUser(existingUser);
        return updateUserFields(updatedUser, existingUser, requestDTO);
    }

    private User initializeUpdatedUser(final User existingUser) {
        final User updatedUser = new User();
        updatedUser.setId(existingUser.getId());
        updatedUser.setCreatedAt(existingUser.getCreatedAt());
        updatedUser.setUpdatedAt(existingUser.getUpdatedAt());
        updatedUser.setRoles(existingUser.getRoles());

        // Copy all existing fields
        updatedUser.setUsername(existingUser.getUsername());
        updatedUser.setEmail(existingUser.getEmail());
        updatedUser.setPassword(existingUser.getPassword());
        updatedUser.setFirstName(existingUser.getFirstName());
        updatedUser.setLastName(existingUser.getLastName());
        updatedUser.setTitle(existingUser.getTitle());
        updatedUser.setAffiliation(existingUser.getAffiliation());
        updatedUser.setThumbnail(existingUser.getThumbnail());

        return updatedUser;
    }

    private User updateUserFields(final User updatedUser, final User existingUser, final UserRequestDTO requestDTO) {
        boolean hasChanges = false;

        hasChanges |= updateField(updatedUser::setUsername, requestDTO.username(), existingUser.getUsername());
        hasChanges |= updateField(updatedUser::setEmail, requestDTO.email(), existingUser.getEmail());

        if (requestDTO.password() != null) {
            updatedUser.setPassword(passwordEncoder.encode(requestDTO.password()));
            hasChanges = true;
        }

        hasChanges |= updateField(updatedUser::setFirstName, requestDTO.firstName(), existingUser.getFirstName());
        hasChanges |= updateField(updatedUser::setLastName, requestDTO.lastName(), existingUser.getLastName());
        hasChanges |= updateField(updatedUser::setTitle, requestDTO.title(), existingUser.getTitle());
        hasChanges |= updateField(updatedUser::setAffiliation, requestDTO.affiliation(), existingUser.getAffiliation());
        hasChanges |= updateField(updatedUser::setThumbnail, requestDTO.thumbnail(), existingUser.getThumbnail());

        return hasChanges ? updatedUser : existingUser;
    }

    private boolean updateField(final java.util.function.Consumer<String> setter, final String newValue, final String oldValue) {
        if (!Objects.equals(newValue, oldValue)) {
            setter.accept(newValue);
            return true;
        }
        return false;
    }

    @PreAuthorize(Authorities.USER_WRITE)
    @Transactional
    public UserResponseDTO updateUser(final UUID id, final UserRequestDTO requestDTO) {
        final User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        validateUserUpdate(existingUser, requestDTO);
        final User userToSave = createUpdatedUser(existingUser, requestDTO);

        // We intentionally use == here because updateUserFields returns the same instance
        // if no changes were made, and a new instance if there were changes
        @SuppressWarnings("PMD.CompareObjectsWithEquals")
        final boolean noChanges = userToSave == existingUser;
        return noChanges ? mapToResponseDTO(existingUser) : mapToResponseDTO(userRepository.save(userToSave));
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

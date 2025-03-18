package de.muenchen.refarch.user;

import de.muenchen.refarch.role.Role;
import de.muenchen.refarch.role.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserRoleService {
    private static final String USER_NOT_FOUND_MESSAGE = "User not found with id: ";
    private static final String ROLE_NOT_FOUND_MESSAGE = "Role not found with id: ";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER_MANAGER')")
    @Transactional(readOnly = true)
    public Set<Role> getUserRoles(final UUID userId) {
        return userRepository.findById(userId)
                .map(User::getRoles)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE + userId));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public Set<Role> addRoleToUser(final UUID userId, final UUID roleId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE + userId));

        final Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException(ROLE_NOT_FOUND_MESSAGE + roleId));

        user.getRoles().add(role);
        return userRepository.save(user).getRoles();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public Set<Role> removeRoleFromUser(final UUID userId, final UUID roleId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE + userId));

        final Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException(ROLE_NOT_FOUND_MESSAGE + roleId));

        user.getRoles().remove(role);
        return userRepository.save(user).getRoles();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public void setUserRoles(final UUID userId, final Set<UUID> roleIds) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE + userId));

        final Set<Role> roles = roleRepository.findAllById(roleIds).stream().collect(java.util.stream.Collectors.toSet());
        if (roles.size() != roleIds.size()) {
            throw new EntityNotFoundException("One or more roles not found");
        }

        user.setRoles(roles);
        userRepository.save(user);
    }
}

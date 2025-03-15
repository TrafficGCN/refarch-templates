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
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER_MANAGER')")
    @Transactional(readOnly = true)
    public Set<Role> getUserRoles(UUID userId) {
        return userRepository.findById(userId)
                .map(User::getRoles)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public Set<Role> addRoleToUser(UUID userId, UUID roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));

        user.getRoles().add(role);
        return userRepository.save(user).getRoles();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public Set<Role> removeRoleFromUser(UUID userId, UUID roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));

        user.getRoles().remove(role);
        return userRepository.save(user).getRoles();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public Set<Role> setUserRoles(UUID userId, Set<UUID> roleIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Set<Role> roles = roleRepository.findAllById(roleIds).stream().collect(java.util.stream.Collectors.toSet());
        if (roles.size() != roleIds.size()) {
            throw new EntityNotFoundException("One or more roles not found");
        }

        user.setRoles(roles);
        return userRepository.save(user).getRoles();
    }
}

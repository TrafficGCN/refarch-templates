package de.muenchen.refarch.role;

import de.muenchen.refarch.role.dto.RoleRequestDTO;
import de.muenchen.refarch.role.dto.RoleResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<RoleResponseDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoleResponseDTO getRoleById(final UUID id) {
        return roleRepository.findById(id)
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public RoleResponseDTO getRoleByName(final String name) {
        return roleRepository.findByName(name)
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with name: " + name));
    }

    @Transactional
    public RoleResponseDTO createRole(final RoleRequestDTO request) {
        if (roleRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Role with name " + request.name() + " already exists");
        }

        final Role role = new Role();
        role.setName(request.name());

        return mapToResponseDTO(roleRepository.save(role));
    }

    @Transactional
    public RoleResponseDTO updateRole(final UUID id, final RoleRequestDTO request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));

        if (!role.getName().equals(request.name()) && roleRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Role with name " + request.name() + " already exists");
        }

        role.setName(request.name());
        return mapToResponseDTO(roleRepository.save(role));
    }

    @Transactional
    public void deleteRole(final UUID id) {
        if (!roleRepository.existsById(id)) {
            throw new EntityNotFoundException("Role not found with id: " + id);
        }
        roleRepository.deleteById(id);
    }

    private RoleResponseDTO mapToResponseDTO(final Role role) {
        return new RoleResponseDTO(
                role.getId(),
                role.getName(),
                role.getCreatedAt(),
                role.getUpdatedAt());
    }
}

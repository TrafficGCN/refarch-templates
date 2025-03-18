package de.muenchen.refarch.role;

import de.muenchen.refarch.role.dto.RoleRequestDTO;
import de.muenchen.refarch.role.dto.RoleResponseDTO;
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
class RoleServiceTest {

    private static final String ROLE_TEST = "ROLE_TEST";
    private static final String ROLE_NEW = "ROLE_NEW";

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    private UUID roleId;
    private Role role;
    private RoleRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        roleId = UUID.randomUUID();

        role = new Role();
        role.setId(roleId);
        role.setName(ROLE_TEST);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());

        requestDTO = new RoleRequestDTO(ROLE_TEST);
    }

    @Test
    void shouldFetchAllRoles() {
        when(roleRepository.findAll()).thenReturn(List.of(role));

        final List<RoleResponseDTO> result = roleService.getAllRoles();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(roleId);
        assertThat(result.get(0).name()).isEqualTo(ROLE_TEST);
    }

    @Test
    void shouldFindRoleById() {
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        final RoleResponseDTO result = roleService.getRoleById(roleId);

        assertThat(result.id()).isEqualTo(roleId);
        assertThat(result.name()).isEqualTo(ROLE_TEST);
    }

    @Test
    void shouldFailWhenRoleNotFoundById() {
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.getRoleById(roleId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Role not found with id: " + roleId);
    }

    @Test
    void shouldFindRoleByName() {
        when(roleRepository.findByName(ROLE_TEST)).thenReturn(Optional.of(role));

        final RoleResponseDTO result = roleService.getRoleByName(ROLE_TEST);

        assertThat(result.id()).isEqualTo(roleId);
        assertThat(result.name()).isEqualTo(ROLE_TEST);
    }

    @Test
    void shouldFailWhenRoleNotFoundByName() {
        when(roleRepository.findByName(ROLE_TEST)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.getRoleByName(ROLE_TEST))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Role not found with name: " + ROLE_TEST);
    }

    @Test
    void shouldCreateRoleWithUniqueName() {
        when(roleRepository.existsByName(ROLE_TEST)).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        final RoleResponseDTO result = roleService.createRole(requestDTO);

        assertThat(result.id()).isEqualTo(roleId);
        assertThat(result.name()).isEqualTo(ROLE_TEST);
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void shouldFailToCreateDuplicateRole() {
        when(roleRepository.existsByName(ROLE_TEST)).thenReturn(true);

        assertThatThrownBy(() -> roleService.createRole(requestDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Role with name " + ROLE_TEST + " already exists");
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void shouldUpdateRoleWithUniqueName() {
        final RoleRequestDTO updateRequest = new RoleRequestDTO(ROLE_NEW);

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleRepository.existsByName(ROLE_NEW)).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            final Role savedRole = invocation.getArgument(0);
            savedRole.setId(roleId);
            savedRole.setName(ROLE_NEW);
            return savedRole;
        });

        final RoleResponseDTO result = roleService.updateRole(roleId, updateRequest);

        assertThat(result.id()).isEqualTo(roleId);
        assertThat(result.name()).isEqualTo(ROLE_NEW);
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void shouldFailToUpdateNonexistentRole() {
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.updateRole(roleId, requestDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Role not found with id: " + roleId);
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void shouldFailToUpdateWithExistingName() {
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleRepository.existsByName(ROLE_NEW)).thenReturn(true);

        final RoleRequestDTO updateRequest = new RoleRequestDTO(ROLE_NEW);

        assertThatThrownBy(() -> roleService.updateRole(roleId, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Role with name " + ROLE_NEW + " already exists");
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void shouldDeleteExistingRole() {
        when(roleRepository.existsById(roleId)).thenReturn(true);

        roleService.deleteRole(roleId);

        verify(roleRepository).deleteById(roleId);
    }

    @Test
    void shouldFailToDeleteNonexistentRole() {
        when(roleRepository.existsById(roleId)).thenReturn(false);

        assertThatThrownBy(() -> roleService.deleteRole(roleId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Role not found with id: " + roleId);
        verify(roleRepository, never()).deleteById(any());
    }
}

package de.muenchen.refarch.role.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleRequestDTO(
        @NotBlank(message = "Role name cannot be blank") String name) {
}

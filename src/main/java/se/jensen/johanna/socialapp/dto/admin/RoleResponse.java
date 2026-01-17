package se.jensen.johanna.socialapp.dto.admin;

import se.jensen.johanna.socialapp.model.Role;

public record RoleResponse(
        String email,
        Role role
) {
}

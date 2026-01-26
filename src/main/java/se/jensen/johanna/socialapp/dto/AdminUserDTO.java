package se.jensen.johanna.socialapp.dto;

import se.jensen.johanna.socialapp.model.Role;

public record AdminUserDTO(
        String username,
        String email,
        Long userId,
        Role role,
        String bio,
        String profileImagePath
) {
}

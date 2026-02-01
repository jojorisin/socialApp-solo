package se.jensen.johanna.socialapp.dto;

import se.jensen.johanna.socialapp.model.Role;

public record LoginResponse(
        String accessToken,
        Long userId,
        Role role,
        String username
) {
}

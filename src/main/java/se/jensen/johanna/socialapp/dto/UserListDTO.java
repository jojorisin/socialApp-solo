package se.jensen.johanna.socialapp.dto;

public record UserListDTO(
        Long userId,
        String username,
        String profileImagePath
) {
}

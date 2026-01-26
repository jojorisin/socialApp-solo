package se.jensen.johanna.socialapp.dto;

public record MyDTO(
        Long userId,
        String profileImagePath,
        String username,
        String bio,
        String email
) {
}

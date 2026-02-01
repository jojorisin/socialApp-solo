package se.jensen.johanna.socialapp.dto;

public record LoginRequest(
        String username,
        String password
) {
}

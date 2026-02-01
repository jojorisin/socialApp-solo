package se.jensen.johanna.socialapp.dto;

public record LoginResult(
        LoginResponse loginResponse,
        String refreshToken
) {
}

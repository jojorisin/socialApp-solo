package se.jensen.johanna.socialapp.dto;

public record RefreshTokenResponse(
        String accessToken,
        String refreshToken
) {
}

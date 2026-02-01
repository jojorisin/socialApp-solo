package se.jensen.johanna.socialapp.dto;

public record RefreshResult(
        String accessToken,
        String refreshToken
) {
}

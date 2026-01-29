package se.jensen.johanna.socialapp.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Utility component for managing HTTP cookies within the application.
 * Specifically handles the creation and clearing of refresh token cookies.
 */

@Component
public class CookieUtils {
    /**
     * Duration in milliseconds before the refresh token expires.
     */

    @Value("${app.jwt.refresh-expiration-ms}")
    private Long refreshTokenDurationMs;

    /**
     * The SameSite attribute value for the cookie (e.g., "Strict", "Lax", "None").
     */
    @Value("${app.cookie.same-site}")
    private String sameSite;

    /**
     * Indicates whether the cookie should only be transmitted over secure (HTTPS) connections.
     */
    @Value("${app.cookie.secure}")
    private Boolean cookieSecure;

    /**
     * Creates a {@link ResponseCookie} containing the provided refresh token.
     * The cookie is configured as HttpOnly, with a specified path, max age, and security settings.
     *
     * @param refreshToken The refresh token string to be stored in the cookie.
     * @return A configured ResponseCookie object.
     */
    public ResponseCookie createRefreshCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(refreshTokenDurationMs / 1000)
                .sameSite(sameSite)
                .build();
    }

    /**
     * Generates a "clean" {@link ResponseCookie} intended to remove the refresh token from the client.
     * This is achieved by setting the cookie's value to an empty string and its max age to zero.
     *
     * @return A ResponseCookie object configured to expire the existing refresh token cookie.
     */
    public ResponseCookie getCleanResponseCookie() {
        return ResponseCookie.from("refreshToken", "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(cookieSecure)
                .build();
    }

}

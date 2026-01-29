package se.jensen.johanna.socialapp.util;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import se.jensen.johanna.socialapp.exception.JwtAuthenticationException;

/**
 * Utility class for processing and extracting information from JSON Web Tokens (JWT).
 * This component is primarily used to retrieve user identification from authenticated requests.
 */

@Component
public class JwtUtils {

    /**
     * Extracts the user ID from the subject ('sub') claim of the provided JWT.
     * The subject is expected to be a numeric string representing the user's unique identifier.
     *
     * @param jwt the {@link Jwt} object containing the token claims
     * @return the user ID as a {@link Long}
     * @throws JwtAuthenticationException if the subject claim is missing, empty,
     *                                    or cannot be parsed as a Long
     */
    public Long extractUserId(Jwt jwt) {
        String sub = jwt.getSubject();
        if (sub == null || sub.isBlank()) {
            throw new JwtAuthenticationException("JWT Subject is missing");
        }
        try {
            return Long.parseLong(sub);
        } catch (NumberFormatException e) {
            throw new JwtAuthenticationException("Invalid User ID format in JWT");
        }

    }

}

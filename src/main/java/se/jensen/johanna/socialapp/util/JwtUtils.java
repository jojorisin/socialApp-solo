package se.jensen.johanna.socialapp.util;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import se.jensen.johanna.socialapp.exception.JwtAuthenticationException;

@Component
public class JwtUtils {


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

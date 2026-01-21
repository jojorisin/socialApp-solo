package se.jensen.johanna.socialapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import se.jensen.johanna.socialapp.model.User;
import se.jensen.johanna.socialapp.security.MyUserDetails;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service responsible for generating JSON Web Tokens (JWT) used for authentication and authorization.
 * It provides methods to create tokens based on user entities or Spring Security authentication objects.
 */

@Transactional
@Service
@RequiredArgsConstructor
public class TokenService {
    @Value("${app.jwt.expiration-minutes:15}")
    private Long expirationMinutes;

    private final JwtEncoder jwtEncoder;

    /**
     * Generates a JWT for a specific {@link User} entity.
     * This method generates a new token when the old JWT has expired
     *
     * @param user the user entity for whom the token is generated
     * @return a signed JWT string containing user identity and roles
     */


    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expirationMinutes, ChronoUnit.MINUTES);
        List<String> scope = List.of("ROLE_" + user.getRole().name());
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getUserId().toString())
                .claim("name", user.getUsername())
                .claim("scope", scope)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();


    }

    /**
     * Generates a JWT based on the provided {@link Authentication} object.
     * This method extracts the user details and granted authorities from the authentication context.
     *
     * @param authentication the authentication object containing user principal and authorities
     * @return a signed JWT string
     */

    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expirationMinutes, ChronoUnit.MINUTES);
        List<String> scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();

        Long userId = ((MyUserDetails) authentication.getPrincipal()).getUserId();
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(userId.toString())
                .claim("name", authentication.getName())
                .claim("scope", scope)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();


    }


}

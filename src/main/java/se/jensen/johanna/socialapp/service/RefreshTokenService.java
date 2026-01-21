package se.jensen.johanna.socialapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.jensen.johanna.socialapp.exception.NotFoundException;
import se.jensen.johanna.socialapp.exception.RefreshTokenException;
import se.jensen.johanna.socialapp.model.RefreshToken;
import se.jensen.johanna.socialapp.model.User;
import se.jensen.johanna.socialapp.repository.RefreshTokenRepository;
import se.jensen.johanna.socialapp.repository.UserRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for managing the lifecycle of Refresh Tokens.
 * Responsible for persisting tokens in the database, verifying their expiration,
 * and facilitating the issuance of new JWT Access Tokens.
 */

@Transactional
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${app.jwt.refresh-expiration-ms}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    /**
     * Finds a {@link RefreshToken} by its token string.
     *
     * @param refreshToken the token string to search for
     * @return an {@link Optional} containing the found RefreshToken, or empty if not found
     */

    public Optional<RefreshToken> findByToken(String refreshToken) {
        return refreshTokenRepository.findByToken(refreshToken);
    }

    /**
     * Creates a new {@link RefreshToken} for a specific user.
     * If a refresh token already exists for the user, it is deleted before creating a new one.
     *
     * @param userId the ID of the user for whom the token is created
     * @return the newly created and saved {@link RefreshToken}
     * @throws NotFoundException if the user with the given ID is not found
     */

    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));

        return refreshTokenRepository.save(refreshToken);


    }

    /**
     * Verifies if a {@link RefreshToken} has expired.
     * If the token is expired, it is removed from the database and an exception is thrown.
     *
     * @param refreshToken the token to verify
     * @return the token if it is still valid
     * @throws RefreshTokenException if the token has expired
     */

    public RefreshToken verifyExpiration(RefreshToken refreshToken) {
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RefreshTokenException("Refresh token has expired. Please Log in again.");
        }
        return refreshToken;
    }

    /**
     * Deletes a {@link RefreshToken} from the database based on its token string.
     *
     * @param refreshToken the token string to be deleted
     */

    public void deleteRefreshToken(String refreshToken) {
        findByToken(refreshToken).ifPresent(refreshTokenRepository::delete);

    }

}

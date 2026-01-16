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

@Transactional
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${app.jwt.refresh-expiration-ms}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

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

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RefreshTokenException("Refresh token has expired. Please Log in again.");
        }
        return token;
    }

}

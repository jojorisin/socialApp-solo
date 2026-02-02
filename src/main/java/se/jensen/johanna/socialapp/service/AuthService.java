package se.jensen.johanna.socialapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import se.jensen.johanna.socialapp.dto.LoginRequest;
import se.jensen.johanna.socialapp.dto.LoginResponse;
import se.jensen.johanna.socialapp.dto.LoginResult;
import se.jensen.johanna.socialapp.dto.RefreshResult;
import se.jensen.johanna.socialapp.exception.RefreshTokenException;
import se.jensen.johanna.socialapp.model.RefreshToken;
import se.jensen.johanna.socialapp.security.MyUserDetails;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final MyUserDetailsService myUserDetailsService;


    public LoginResult login(LoginRequest loginRequest) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                loginRequest.username(),
                loginRequest.password()

        );
        MyUserDetails userDetails = (MyUserDetails) auth.getPrincipal();
        String accessToken = tokenService.generateAccessToken(auth);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUserId());

        return new LoginResult(new LoginResponse(accessToken, userDetails.getUserId(), userDetails.getRole(), userDetails.getUsername()), refreshToken.getToken());


    }

    public RefreshResult refresh(String oldTokenStr) {
        RefreshToken oldToken = refreshTokenService.findByToken(oldTokenStr)
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> new RefreshTokenException("RefreshToken is not in database")
                );

        MyUserDetails userDetails = (MyUserDetails) myUserDetailsService.loadUserByUsername(oldToken.getUser().getUsername());
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(oldToken.getUser().getUserId());

        return new RefreshResult(tokenService.generateAccessToken(auth), newRefreshToken.getToken());

    }


}

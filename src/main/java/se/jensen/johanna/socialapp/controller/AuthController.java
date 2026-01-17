package se.jensen.johanna.socialapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import se.jensen.johanna.socialapp.dto.*;
import se.jensen.johanna.socialapp.exception.RefreshTokenException;
import se.jensen.johanna.socialapp.model.RefreshToken;
import se.jensen.johanna.socialapp.security.MyUserDetails;
import se.jensen.johanna.socialapp.service.RefreshTokenService;
import se.jensen.johanna.socialapp.service.TokenService;
import se.jensen.johanna.socialapp.service.UserService;

@CrossOrigin(origins = "http://localhost:5174")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> getToken(@RequestBody
                                                     LoginRequestDTO loginRequestDTO) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDTO.username(),
                        loginRequestDTO.password()
                ));


        String accessToken = tokenService.generateToken(auth);
        Long userId = ((MyUserDetails) auth.getPrincipal()).getUserId();
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userId);

        return ResponseEntity.ok().body(new LoginResponseDTO(accessToken, refreshToken.getToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody
                                                             @Valid
                                                             RefreshTokenRequest tokenRequest) {

        return refreshTokenService.findByToken(tokenRequest.refreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String newJwt = tokenService.generateToken(user);
                    return ResponseEntity.ok(new RefreshTokenResponse(newJwt, tokenRequest.refreshToken()));
                }).orElseThrow(() -> new RefreshTokenException("RefreshToken is not in database"));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterUserResponse> register(@RequestBody
                                                         RegisterUserRequest registerUserRequest) {
        RegisterUserResponse userResponse = userService.registerUser(registerUserRequest);
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerUserRequest.username(),
                        registerUserRequest.password()));

        String token = tokenService.generateToken(auth);
        Long userId = ((MyUserDetails) auth.getPrincipal()).getUserId();
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userId);
        userResponse.setAccessToken(token);
        userResponse.setRefreshToken(refreshToken.getToken());
        return ResponseEntity.ok().body(userResponse);

    }


    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Jwt jwt) {
        //logout metod, frontend rensar token
        return ResponseEntity.noContent().build();
    }


}

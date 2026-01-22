package se.jensen.johanna.socialapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import se.jensen.johanna.socialapp.dto.LoginRequestDTO;
import se.jensen.johanna.socialapp.dto.LoginResponseDTO;
import se.jensen.johanna.socialapp.dto.RefreshTokenResponse;
import se.jensen.johanna.socialapp.dto.RegisterUserRequest;
import se.jensen.johanna.socialapp.exception.RefreshTokenException;
import se.jensen.johanna.socialapp.model.RefreshToken;
import se.jensen.johanna.socialapp.security.MyUserDetails;
import se.jensen.johanna.socialapp.service.RefreshTokenService;
import se.jensen.johanna.socialapp.service.TokenService;
import se.jensen.johanna.socialapp.service.UserService;
import se.jensen.johanna.socialapp.util.CookieUtils;

/**
 * REST controller for authentication operations.
 * Handles user login, registration, token refresh, and logout.
 * All endpoints return JWT tokens in response-body and refresh tokens as HTTP-only cookies.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtils cookieUtils;

    /**
     * Authenticates a user and returns a JWT token
     * Sets a refresh token as an HTTP-only cookie
     *
     * @param loginRequestDTO {@link LoginRequestDTO} contains username and password
     * @return Response with {@link LoginResponseDTO}
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> getToken(@RequestBody
                                                     LoginRequestDTO loginRequestDTO) {


        LoginResponseDTO loginResponseDTO = createLoginResponse(getAuth(loginRequestDTO.username(), loginRequestDTO.password()));

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(loginResponseDTO.userId());
        ResponseCookie responseCookie = cookieUtils.createRefreshCookie(refreshToken.getToken());

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(loginResponseDTO);
    }

    /**
     * Authenticates the old token and returns a new JWT token and sets a new refresh token as a cookie
     *
     * @param oldTokenStr old token as a string
     * @return Response with {@link RefreshTokenResponse} contains JWT token
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            @CookieValue(name = "refreshToken") String oldTokenStr

    ) {

        RefreshToken oldToken = refreshTokenService.findByToken(oldTokenStr)
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> new RefreshTokenException("RefreshToken is not in database"));

        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(oldToken.getUser().getUserId());
        ResponseCookie responseCookie = cookieUtils.createRefreshCookie(newRefreshToken.getToken());
        String newJwt = tokenService.generateToken(oldToken.getUser());

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(new RefreshTokenResponse(newJwt));


    }

    /**
     * Creates a new user and authenticates them automatically for a smooth client experience.
     * Sets a refresh token as an HTTP-only cookie.
     *
     * @param registerUserRequest contains username, password and user details
     * @return Response with {@link LoginResponseDTO} with JWT token and user information
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponseDTO> register(@RequestBody
                                                     RegisterUserRequest registerUserRequest) {

        userService.registerUser(registerUserRequest);
        LoginResponseDTO loginResponseDTO = createLoginResponse(getAuth(registerUserRequest.username(), registerUserRequest.password()));
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(loginResponseDTO.userId());
        ResponseCookie responseCookie = cookieUtils.createRefreshCookie(refreshToken.getToken());


        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(loginResponseDTO);

    }

    /**
     * Logs out a user by deleting their refresh token and clearing the cookie.
     *
     * @param refreshTokenStr the refresh token from cookie, optional
     * @return empty Response with clear header
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenStr

    ) {
        if (refreshTokenStr != null) {
            refreshTokenService.deleteRefreshToken(refreshTokenStr);
        }
        ResponseCookie cleanCookie = cookieUtils.getCleanResponseCookie();

        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cleanCookie.toString()).build();
    }

    /**
     * private Help method that receives an Authentication object and returns LoginResponseDTO
     *
     * @param auth Authentication-object used for user information and token
     * @return {@link LoginResponseDTO}
     */
    private LoginResponseDTO createLoginResponse(Authentication auth) {
        MyUserDetails userDetails = ((MyUserDetails) auth.getPrincipal());

        return new LoginResponseDTO(
                tokenService.generateToken(auth),
                userDetails.getUserId(),
                userDetails.getRole(),
                userDetails.getUsername()
        );


    }

    /**
     * Private help method to create username and password authentication
     *
     * @param username Username of the user that needs authentication
     * @param password Password of the user that needs authentication
     * @return {@link Authentication}
     */
    private Authentication getAuth(String username, String password) {
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }


}

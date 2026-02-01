package se.jensen.johanna.socialapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.jensen.johanna.socialapp.dto.*;
import se.jensen.johanna.socialapp.service.AuthService;
import se.jensen.johanna.socialapp.service.RefreshTokenService;
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
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtils cookieUtils;
    private final AuthService authService;

    /**
     * Authenticates a user and returns a JWT token
     * Sets a refresh token as an HTTP-only cookie
     *
     * @param loginRequest {@link LoginRequest} contains username and password
     * @return Response with {@link LoginResponse}
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> getToken(
            @RequestBody LoginRequest loginRequest) {

        LoginResult result = authService.login(loginRequest);
        ResponseCookie responseCookie = cookieUtils.createRefreshCookie(result.refreshToken());

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(result.loginResponse());
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

        RefreshResult result = authService.refresh(oldTokenStr);
        ResponseCookie cookie = cookieUtils.createRefreshCookie(result.refreshToken());

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new RefreshTokenResponse(result.accessToken()));


    }

    /**
     * Creates a new user and authenticates them automatically for a smooth client experience.
     * Sets a refresh token as an HTTP-only cookie.
     *
     * @param registerUserRequest contains username, password and user details
     * @return Response with {@link LoginResponse} with JWT token and user information
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody
                                                  RegisterUserRequest registerUserRequest) {

        userService.registerUser(registerUserRequest);
        LoginResult result = authService.login(new LoginRequest(registerUserRequest.username(), registerUserRequest.password()));
        ResponseCookie responseCookie = cookieUtils.createRefreshCookie(result.refreshToken());


        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(result.loginResponse());

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


}

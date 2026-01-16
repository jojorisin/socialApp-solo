package se.jensen.johanna.socialapp.dto;

import lombok.Getter;
import lombok.Setter;

//needs to be class
// token is set in controller after register
//so user doesnt have to login after register

/**
 * Represents the response returned upon successfully registering a new user.
 * This class encapsulates user-specific details and a token for future authentication.
 * <p>
 * Fields:
 * - email: The email address of the newly registered user.
 * - username: The username of the newly registered user.
 * - userId: The unique identifier assigned to the user upon registration.
 * - token: An authentication token issued for the user.
 */
@Getter
@Setter
public class RegisterUserResponse {
    private String email;
    private String username;
    private Long userId;
    private String accessToken;
    private String refreshToken;


}

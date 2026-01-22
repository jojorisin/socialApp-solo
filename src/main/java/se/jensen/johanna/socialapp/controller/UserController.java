package se.jensen.johanna.socialapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.jensen.johanna.socialapp.dto.HomePageResponse;
import se.jensen.johanna.socialapp.dto.UserDTO;
import se.jensen.johanna.socialapp.dto.UserListDTO;
import se.jensen.johanna.socialapp.service.FriendshipService;
import se.jensen.johanna.socialapp.service.UserService;

import java.util.List;

/**
 * REST controller for managing user-related operations.
 * Provides endpoints for retrieving user lists, individual profiles, and user friendships.
 */

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final FriendshipService friendshipService;

    //OBS vilka är för admin vilka för user


    /**
     * Retrieves a list of all users with the MEMBER role.
     * The returned list contains simplified user information.
     *
     * @return a list of UserListDTO objects
     */
    @GetMapping
    public ResponseEntity<List<UserListDTO>> getAllUsers() {
        List<UserListDTO> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Retrieves detailed profile information for a specific user.
     *
     * @param userId the ID of the user to retrieve
     * @return the UserDTO containing user details
     */

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long userId) {
        UserDTO userDTO = userService.findUser(userId);
        return ResponseEntity.ok(userDTO);

    }

    /**
     * Retrieves the profile/homepage data for a specific user.
     * Access is restricted to authenticated users.
     *
     * @param userId the ID of the user whose profile data is being requested
     * @return the HomePageResponse containing profile and activity data
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{userId}/profile")
    public ResponseEntity<HomePageResponse> getProfile(@PathVariable Long userId) {

        HomePageResponse homePage = userService.getProfile(userId);

        return ResponseEntity.ok(homePage);
    }


    /**
     * Retrieves a list of accepted friendships to a specific user
     *
     * @param userId ID of user to fetch friends for
     * @return {@link UserListDTO}
     */
    @GetMapping("/{userId}/friends")
    public ResponseEntity<List<UserListDTO>> getMyFriends(@PathVariable Long userId) {

        List<UserListDTO> friends = friendshipService.getFriendsForUser(userId);


        return ResponseEntity.ok(friends);
    }


}

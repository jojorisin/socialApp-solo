package se.jensen.johanna.socialapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import se.jensen.johanna.socialapp.dto.MyFriendRequest;
import se.jensen.johanna.socialapp.dto.UpdateUserRequest;
import se.jensen.johanna.socialapp.dto.UpdateUserResponse;
import se.jensen.johanna.socialapp.dto.UserListDTO;
import se.jensen.johanna.socialapp.service.FriendshipService;
import se.jensen.johanna.socialapp.service.UserService;
import se.jensen.johanna.socialapp.util.JwtUtils;

import java.util.List;

/**
 * Controller for handling operations related to the currently authenticated user.
 * Provides endpoints for managing the user's own profile, friends, and friend requests.
 */

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/my")
@RequiredArgsConstructor
public class MyController {
    private final UserService userService;
    private final FriendshipService friendshipService;
    private final JwtUtils jwtUtils;

    /**
     * Returns a list of pending friendrequests for the authenticated user
     * Contains a boolean isIncoming, is true if the user is on the receiving end
     * is false if the user is the sender
     *
     * @param jwt AccessToken containing ID of authenticated user
     * @return {@link MyFriendRequest}
     */
    @GetMapping("/friend-request")
    public ResponseEntity<List<MyFriendRequest>> getFriendRequests(@AuthenticationPrincipal Jwt jwt) {
        Long userId = jwtUtils.extractUserId(jwt);
        List<MyFriendRequest> myFriendRequests = friendshipService.getFriendRequestsForUser(userId);


        return ResponseEntity.ok(myFriendRequests);
    }


    /**
     * Retrieves a list of all accepted friends for the authenticated user.
     *
     * @param jwt the {@link Jwt} access token containing the authenticated user's identity
     * @return a {@link ResponseEntity} containing a list of {@link UserListDTO} representing the user's friends
     */
    @GetMapping("/friends")
    public ResponseEntity<List<UserListDTO>> getMyFriends(@AuthenticationPrincipal Jwt jwt) {
        Long userId = jwtUtils.extractUserId(jwt);

        List<UserListDTO> friends = friendshipService.getFriendsForUser(userId);


        return ResponseEntity.ok(friends);
    }

    /**
     * Updates the profile information of the authenticated user.
     *
     * @param jwt         the {@link Jwt} access token containing the authenticated user's identity
     * @param userRequest the {@link UpdateUserRequest} containing the updated profile data
     * @return a {@link ResponseEntity} containing the {@link UpdateUserResponse} with updated user details
     */
    @PreAuthorize("isAuthenticated()")
    @PatchMapping
    public ResponseEntity<UpdateUserResponse> updateMe(@AuthenticationPrincipal Jwt jwt,
                                                       @RequestBody UpdateUserRequest userRequest) {
        Long userId = jwtUtils.extractUserId(jwt);

        UpdateUserResponse userResponse = userService.updateUser(userRequest, userId);
        return ResponseEntity.ok(userResponse);
    }

    /**
     * Deletes the account of the authenticated user.
     *
     * @param jwt the {@link Jwt} access token containing the authenticated user's identity
     * @return a {@link ResponseEntity} with status 204 (No Content) upon successful deletion
     */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping
    public ResponseEntity<Void> deleteMe(@AuthenticationPrincipal Jwt jwt) {
        Long userId = jwtUtils.extractUserId(jwt);

        userService.deleteUser(userId);

        return ResponseEntity.noContent().build();


    }

}

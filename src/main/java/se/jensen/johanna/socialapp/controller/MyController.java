package se.jensen.johanna.socialapp.controller;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import se.jensen.johanna.socialapp.dto.*;
import se.jensen.johanna.socialapp.service.FriendshipService;
import se.jensen.johanna.socialapp.service.PostService;
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
    private final PostService postService;


    @GetMapping
    public ResponseEntity<MyDTO> getMe(@AuthenticationPrincipal Jwt jwt) {
        Long userId = jwtUtils.extractUserId(jwt);
        MyDTO myDTO = userService.getAuthenticatedUser(userId);
        return ResponseEntity.ok(myDTO);
    }

    /**
     * Retrieves a paginated list of posts belonging to the currently authenticated user.
     *
     * @param pageable pagination and sorting information
     * @param jwt      the {@link Jwt} access token containing the authenticated user's identity
     * @return a ResponseEntity containing {@link UserPostDTO}
     */
    @GetMapping("/posts")
    public ResponseEntity<Page<UserPostDTO>> getMyPosts(
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwtUtils.extractUserId(jwt);
        Page<UserPostDTO> myPosts = postService.getPostsForUser(userId, pageable);
        return ResponseEntity.ok(myPosts);

    }

    /**
     * Returns a list of pending friendrequests for the authenticated user
     * Contains a boolean isIncoming, is true if the user is on the receiving end
     * is false if the user is the sender
     *
     * @param jwt AccessToken containing ID of the authenticated user
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
    @DeleteMapping
    public ResponseEntity<Void> deleteMe(@AuthenticationPrincipal Jwt jwt) {
        Long userId = jwtUtils.extractUserId(jwt);

        userService.deleteUser(userId);

        return ResponseEntity.noContent().build();


    }


}

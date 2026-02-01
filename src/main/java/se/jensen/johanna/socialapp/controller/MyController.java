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
import org.springframework.web.bind.annotation.*;
import se.jensen.johanna.socialapp.dto.*;
import se.jensen.johanna.socialapp.security.MyUserDetails;
import se.jensen.johanna.socialapp.service.FriendshipService;
import se.jensen.johanna.socialapp.service.PostService;
import se.jensen.johanna.socialapp.service.UserService;

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
    private final PostService postService;


    @GetMapping
    public ResponseEntity<MyDTO> getMe(@AuthenticationPrincipal MyUserDetails userDetails) {
        return ResponseEntity.ok(userService.getAuthenticatedUser(userDetails.getUserId()));
    }

    /**
     * Retrieves a paginated list of posts belonging to the currently authenticated user.
     *
     * @param pageable pagination and sorting information
     * @return a ResponseEntity containing {@link UserPostDTO}
     */
    @GetMapping("/posts")
    public ResponseEntity<Page<UserPostDTO>> getMyPosts(
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal MyUserDetails userDetails) {
        return ResponseEntity.ok(postService.getPostsForUser(userDetails.getUserId(), pageable));

    }

    /**
     * Returns a list of pending friendrequests for the authenticated user
     * Contains a boolean isIncoming, is true if the user is on the receiving end
     * is false if the user is the sender
     *
     * @return {@link MyFriendRequest}
     */
    @GetMapping("/friend-request")
    public ResponseEntity<List<MyFriendRequest>> getFriendRequests(@AuthenticationPrincipal MyUserDetails userDetails) {

        return ResponseEntity.ok(friendshipService.getFriendRequestsForUser(userDetails.getUserId()));
    }


    /**
     * Retrieves a list of all accepted friends for the authenticated user.
     *
     * @return a {@link ResponseEntity} containing a list of {@link UserListDTO} representing the user's friends
     */
    @GetMapping("/friends")
    public ResponseEntity<List<UserListDTO>> getMyFriends(@AuthenticationPrincipal MyUserDetails userDetails) {

        return ResponseEntity.ok(friendshipService.getFriendsForUser(userDetails.getUserId()));
    }

    /**
     * Updates the profile information of the authenticated user.
     *
     * @param userRequest the {@link UpdateUserRequest} containing the updated profile data
     * @return a {@link ResponseEntity} containing the {@link UpdateUserResponse} with updated user details
     */
    @PatchMapping
    public ResponseEntity<UpdateUserResponse> updateMe(@AuthenticationPrincipal MyUserDetails userDetails,
                                                       @RequestBody UpdateUserRequest userRequest) {

        return ResponseEntity.ok(userService.updateUser(userRequest, userDetails.getUserId()));
    }

    /**
     * Deletes the account of the authenticated user.
     *
     * @return a {@link ResponseEntity} with status 204 (No Content) upon successful deletion
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteMe(@AuthenticationPrincipal MyUserDetails userDetails) {


        userService.deleteUser(userDetails.getUserId());

        return ResponseEntity.noContent().build();


    }


}

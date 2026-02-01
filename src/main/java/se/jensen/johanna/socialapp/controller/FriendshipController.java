package se.jensen.johanna.socialapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import se.jensen.johanna.socialapp.dto.FriendResponseDTO;
import se.jensen.johanna.socialapp.security.MyUserDetails;
import se.jensen.johanna.socialapp.service.FriendshipService;

/**
 * REST controller for managing friendships and friend requests.
 * Provides endpoints for sending, accepting, rejecting, and deleting friendships
 * Only for authenticated users
 */

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/friendships")
@RequiredArgsConstructor
public class FriendshipController {
    private final FriendshipService friendshipService;


    /**
     * Sends a friend request to the user with the specified receiverId..
     *
     * @param receiverId the ID of the user who will receive the friend request
     * @return a ResponseEntity containing the created FriendResponseDTO
     */

    @PostMapping("/{receiverId}")
    public ResponseEntity<FriendResponseDTO> sendFriendRequest(
            @PathVariable Long receiverId,
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {

        return ResponseEntity.status(HttpStatus.CREATED).body(friendshipService.sendFriendRequest(userDetails.getUserId(), receiverId));

    }

    /**
     * Accepts a pending friend request.
     * Verifies that the logged-in user is the intended receiver of the request.
     *
     * @param friendshipId the ID of the friendship request to accept
     * @return a ResponseEntity containing the updated FriendResponseDTO
     */

    @PutMapping("/{friendshipId}/accept")
    public ResponseEntity<FriendResponseDTO> acceptFriendRequest(
            @PathVariable Long friendshipId,
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {

        return ResponseEntity.ok(friendshipService.acceptFriendRequest(friendshipId, userDetails.getUserId()));

    }

    /**
     * Rejects a pending friend request.
     * Verifies that the logged-in user is the intended receiver of the request.
     *
     * @param friendshipId the ID of the friendship request to reject
     * @return a ResponseEntity containing the updated FriendResponseDTO
     */

    @PutMapping("/{friendshipId}/reject")
    public ResponseEntity<Void> rejectFriendRequest(
            @PathVariable Long friendshipId,
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {
        friendshipService.rejectFriendRequest(friendshipId, userDetails.getUserId());


        return ResponseEntity.noContent().build();


    }

    /**
     * Deletes an existing friendship or cancels/removes a friend request.
     *
     * @param friendshipId the ID of the friendship to delete
     * @return a ResponseEntity with no content (204 No Content)
     */

    @DeleteMapping("{friendshipId}")
    public ResponseEntity<Void> deleteFriendship(
            @PathVariable Long friendshipId,
            @AuthenticationPrincipal MyUserDetails userDetails) {

        friendshipService.deleteFriendship(friendshipId, userDetails.getUserId());

        return ResponseEntity.noContent().build();

    }
/*
    @GetMapping("/status/{targetUserId}")
    public ResponseEntity<FriendshipStatusDTO> getFriendshipStatus(
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal MyUserDetails userDetails) {
        FriendshipStatusDTO statusDTO = friendshipService.getFriendshipStatus(currentUserId, targetUserId);

        if (statusDTO == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(statusDTO);
    }*/

}

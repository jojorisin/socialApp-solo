package se.jensen.johanna.socialapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import se.jensen.johanna.socialapp.dto.FriendResponseDTO;
import se.jensen.johanna.socialapp.service.FriendshipService;
import se.jensen.johanna.socialapp.util.JwtUtils;

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
    private final JwtUtils jwtUtils;


    /**
     * Sends a friend request to the user with the specified receiverId.
     * The sender is identified via the JWT token.
     *
     * @param receiverId the ID of the user who will receive the friend request
     * @param jwt        the authenticated user's JWT token
     * @return a ResponseEntity containing the created FriendResponseDTO
     */

    @PostMapping("/{receiverId}")
    public ResponseEntity<FriendResponseDTO> sendFriendRequest(@PathVariable
                                                               Long receiverId,
                                                               @AuthenticationPrincipal
                                                               Jwt jwt
    ) {
        Long senderId = jwtUtils.extractUserId(jwt);
        FriendResponseDTO friendResponseDTO =
                friendshipService.sendFriendRequest(senderId, receiverId);
        return ResponseEntity.status(HttpStatus.CREATED).body(friendResponseDTO);

    }

    /**
     * Accepts a pending friend request.
     * Verifies that the logged-in user is the intended receiver of the request.
     *
     * @param friendshipId the ID of the friendship request to accept
     * @param jwt          the authenticated user's JWT token
     * @return a ResponseEntity containing the updated FriendResponseDTO
     */

    @PutMapping("/{friendshipId}/accept")
    public ResponseEntity<FriendResponseDTO> acceptFriendRequest(
            @PathVariable Long friendshipId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long currentUserId = jwtUtils.extractUserId(jwt);
        FriendResponseDTO friendResponseDTO = friendshipService.
                acceptFriendRequest(friendshipId, currentUserId);


        return ResponseEntity.ok(friendResponseDTO);

    }

    /**
     * Rejects a pending friend request.
     * Verifies that the logged-in user is the intended receiver of the request.
     *
     * @param friendshipId the ID of the friendship request to reject
     * @param jwt          the authenticated user's JWT token
     * @return a ResponseEntity containing the updated FriendResponseDTO
     */

    @PutMapping("/{friendshipId}/reject")
    public ResponseEntity<Void> rejectFriendRequest(
            @PathVariable Long friendshipId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long currentUserId = jwtUtils.extractUserId(jwt);
        friendshipService.rejectFriendRequest(friendshipId, currentUserId);


        return ResponseEntity.noContent().build();


    }

    /**
     * Deletes an existing friendship or cancels/removes a friend request.
     *
     * @param friendshipId the ID of the friendship to delete
     * @param jwt          the authenticated user's JWT token
     * @return a ResponseEntity with no content (204 No Content)
     */

    @DeleteMapping("{friendshipId}")
    public ResponseEntity<Void> deleteFriendship(@PathVariable Long friendshipId, @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwtUtils.extractUserId(jwt);

        friendshipService.deleteFriendship(friendshipId, userId);

        return ResponseEntity.noContent().build();

    }

}

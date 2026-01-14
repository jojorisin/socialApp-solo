package se.jensen.johanna.socialapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import se.jensen.johanna.socialapp.dto.FriendRequestDTO;
import se.jensen.johanna.socialapp.dto.FriendResponseDTO;
import se.jensen.johanna.socialapp.model.Friendship;
import se.jensen.johanna.socialapp.service.FriendshipService;
import se.jensen.johanna.socialapp.util.JwtUtils;

@RestController
@RequestMapping("/friendships")
@RequiredArgsConstructor
public class FriendshipController {
    private final FriendshipService friendshipService;
    private final JwtUtils jwtUtils;

    /**
     * Sends a friend request to the user with the specified receiverId.
     * The sender is identified via the JWT token.
     */
    @PreAuthorize("isAuthenticated()")
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
     */
    @PreAuthorize("isAuthenticated()")
    @PutMapping ("accept/{friendshipId}")
    public ResponseEntity<FriendResponseDTO> acceptFriendRequest(
            @PathVariable Long friendshipId,
            @AuthenticationPrincipal Jwt jwt
    ){
        Long currentUserId = jwtUtils.extractUserId(jwt);
        FriendResponseDTO friendResponseDTO = friendshipService.
                acceptFriendRequest(friendshipId, currentUserId);


        return ResponseEntity.ok(friendResponseDTO);

    }

    @PreAuthorize("isAuthenticated")
    @PutMapping("reject/{friendshipId}")
    public ResponseEntity<FriendResponseDTO> rejectFriendRequest(
            @PathVariable Long friendshipId,
            @AuthenticationPrincipal Jwt jwt
    ){
        Long currentUserId = jwtUtils.extractUserId(jwt);
        FriendResponseDTO friendResponseDTO = friendshipService.
                rejectFriendRequest(friendshipId, currentUserId);


        return ResponseEntity.ok(friendResponseDTO);


    }

}

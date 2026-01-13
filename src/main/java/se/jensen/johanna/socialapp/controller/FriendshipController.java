package se.jensen.johanna.socialapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.jensen.johanna.socialapp.dto.FriendResponseDTO;
import se.jensen.johanna.socialapp.service.FriendshipService;
import se.jensen.johanna.socialapp.util.JwtUtils;

@RestController
@RequestMapping("/friendships")
@RequiredArgsConstructor
public class FriendshipController {
    private final FriendshipService friendshipService;
    private final JwtUtils jwtUtils;

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

}

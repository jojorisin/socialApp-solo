package se.jensen.johanna.socialapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import se.jensen.johanna.socialapp.dto.LikeResponse;
import se.jensen.johanna.socialapp.security.MyUserDetails;
import se.jensen.johanna.socialapp.service.LikeService;

@PreAuthorize("isAuthenticated()")
@RestController
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;


    @PostMapping("/posts/{postId}/likes")
    public ResponseEntity<LikeResponse> togglePostLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {

        return ResponseEntity.ok(likeService.togglePostLike(postId, userDetails.getUserId()));

    }

    @PostMapping("/comments/{commentId}/likes")
    public ResponseEntity<LikeResponse> toggleCommentLike(
            @PathVariable Long commentId,
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {
        return ResponseEntity.ok(likeService.toggleCommentLike(commentId, userDetails.getUserId()));
    }


}

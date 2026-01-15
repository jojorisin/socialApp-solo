package se.jensen.johanna.socialapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import se.jensen.johanna.socialapp.dto.*;
import se.jensen.johanna.socialapp.service.CommentService;
import se.jensen.johanna.socialapp.util.JwtUtils;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final JwtUtils jwtUtils;

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentDTO>> getAllCommentsForPost(@PathVariable
                                                                  Long postId) {
        List<CommentDTO> commentDTOS = commentService.findAllMainComments(postId);

        return ResponseEntity.ok(commentDTOS);


    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> postComment(@PathVariable
                                                       Long postId,
                                                       @AuthenticationPrincipal
                                                       Jwt jwt,
                                                       @RequestBody @Valid
                                                       CommentRequest commentRequest) {
        Long userId = jwtUtils.extractUserId(jwt);

        CommentResponse commentResponse = commentService.postComment(
                postId, userId, commentRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(commentResponse);

    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/comments/{commentId}/replies")
    public ResponseEntity<ReplyCommentResponse> replyComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal
            Jwt jwt,
            @RequestBody @Valid CommentRequest commentRequest
    ) {
        Long userId = jwtUtils.extractUserId(jwt);

        ReplyCommentResponse commentResponse =
                commentService.replyComment(
                        commentId,
                        userId,
                        commentRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(commentResponse);


    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<UpdateCommentResponse> updateComment(@AuthenticationPrincipal
                                                               Jwt jwt,
                                                               @PathVariable
                                                               Long commentId,
                                                               @RequestBody
                                                               CommentRequest commentRequest) {
        Long userId = jwtUtils.extractUserId(jwt);
        UpdateCommentResponse commentResponse = commentService.updateComment(
                commentId, userId, commentRequest);

        return ResponseEntity.ok(commentResponse);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@AuthenticationPrincipal
                                              Jwt jwt,
                                              @PathVariable
                                              Long commentId) {

        Long userId = jwtUtils.extractUserId(jwt);

        commentService.deleteComment(userId, commentId);

        return ResponseEntity.noContent().build();
    }
}

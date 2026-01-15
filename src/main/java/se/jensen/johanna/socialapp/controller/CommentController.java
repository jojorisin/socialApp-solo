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
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final JwtUtils jwtUtils;

    @GetMapping
    public ResponseEntity<List<CommentDTO>> getAllCommentsForPost(@PathVariable
                                                                  Long postId) {
        List<CommentDTO> commentDTOS = commentService.findAllMainComments(postId);

        return ResponseEntity.ok(commentDTOS);


    }

    @PostMapping
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

    @PostMapping("/{commentId}")
    public ResponseEntity<ReplyCommentResponse> replyComment(@PathVariable Long postId,
                                                             @PathVariable Long commentId,
                                                             @AuthenticationPrincipal
                                                             Jwt jwt,
                                                             @RequestBody @Valid CommentRequest commentRequest
    ) {
        Long userId = jwtUtils.extractUserId(jwt);

        ReplyCommentResponse commentResponse =
                commentService.replyComment(
                        postId,
                        commentId,
                        userId,
                        commentRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(commentResponse);


    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{commentId}")
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
}

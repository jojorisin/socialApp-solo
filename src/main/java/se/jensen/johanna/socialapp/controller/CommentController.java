package se.jensen.johanna.socialapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import se.jensen.johanna.socialapp.dto.*;
import se.jensen.johanna.socialapp.service.CommentService;
import se.jensen.johanna.socialapp.util.JwtUtils;


/**
 * Controller handling all operations related to comments
 * Providing endpoints to create, update, delete and get comments
 * as well as managing nested replies
 * Technical note: A reply is a comment that stores the ID of another comment
 * as its parentId, creating a nested relationship.
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final JwtUtils jwtUtils;

    /**
     * Retrieves all main comments for a post
     *
     * @param postId ID of the post to fetch comments to
     * @return {@link CommentDTO}
     */
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<Page<CommentDTO>> getAllCommentsForPost(@PathVariable
                                                                  Long postId,
                                                                  @ParameterObject
                                                                  @PageableDefault(size = 10,
                                                                  sort = "createdAt",
                                                                  direction = Sort.Direction.DESC)
                                                                  Pageable pageable) {

        Page<CommentDTO> commentDTOS = commentService.findAllMainComments(postId, pageable);

        return ResponseEntity.ok(commentDTOS);
    }

    /**
     * Creates a comment to a specific post as an authenticated user
     *
     * @param postId         ID of post to comment
     * @param jwt            AccessToken containing userId in sub
     * @param commentRequest {@link CommentRequest}
     * @return {@link CommentResponse}
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> postComment(@PathVariable
                                                       Long postId,
                                                       @AuthenticationPrincipal
                                                       Jwt jwt,
                                                       @RequestBody @Valid
                                                       CommentRequest commentRequest) {
        Long userId = jwtUtils.extractUserId(jwt);

        CommentResponse commentResponse = commentService.commentPost(
                postId, userId, commentRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(commentResponse);

    }

    /**
     * Creates a reply to a specific comment as an authenticated user
     * Technical note: A reply is a comment that stores the ID of another comment
     * as its parentId, creating a nested relationship.
     *
     * @param commentId      ID of comment to reply to
     * @param jwt            AccessToken containing userId in sub
     * @param commentRequest {@link CommentRequest}
     * @return {@link ReplyCommentResponse}
     */
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

    /**
     * Retrieves all replies to a specific comment
     * Technical note: A reply is a comment that stores the ID of another comment
     * as its parentId, creating a nested relationship.
     *
     * @param commentId ID of comment
     * @return List of commentDtos with replies
     */
    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<Page<CommentDTO>> getAllRepliesForComment(
            @PathVariable
            Long commentId,
            @ParameterObject
            @PageableDefault (size = 5,
            sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<CommentDTO> replies = commentService.findAllRepliesForComment(commentId, pageable);

        return ResponseEntity.ok(replies);

    }

    /**
     * Updates a specific comment as an authenticated user and verified owner of comment
     * Only the owner of the comment is authorized to perform this update
     *
     * @param jwt            AccessToken used to verify ownership
     * @param commentId      ID of comment to update
     * @param commentRequest {@link CommentRequest}
     * @return {@link UpdateCommentResponse}
     */
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

    /**
     * Deletes comment as an authenticated user and verified owner of comment
     *
     * @param jwt       AccessToken containing userId i sub
     * @param commentId ID of comment to delete
     * @return ResponseEntity with no content (204)
     */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@AuthenticationPrincipal
                                              Jwt jwt,
                                              @PathVariable
                                              Long commentId) {

        Long userId = jwtUtils.extractUserId(jwt);

        commentService.deleteComment(commentId, userId);

        return ResponseEntity.noContent().build();
    }
}

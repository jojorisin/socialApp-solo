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
import org.springframework.web.bind.annotation.*;
import se.jensen.johanna.socialapp.dto.*;
import se.jensen.johanna.socialapp.security.MyUserDetails;
import se.jensen.johanna.socialapp.service.CommentService;


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

    /**
     * Retrieves all main comments for a post
     *
     * @param postId ID of the post to fetch comments to
     * @return {@link CommentDTO}
     */
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<Page<CommentDTO>> getAllCommentsForPost(
            @PathVariable Long postId,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {


        return ResponseEntity.ok(commentService.findAllMainComments(postId, pageable));
    }

    /**
     * Creates a comment to a specific post as an authenticated user
     *
     * @param postId         ID of post to comment
     * @param commentRequest {@link CommentRequest}
     * @return {@link CommentResponse}
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> postComment(
            @PathVariable Long postId,
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestBody @Valid CommentRequest commentRequest) {


        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.commentPost(postId, userDetails.getUserId(), commentRequest));

    }

    /**
     * Creates a reply to a specific comment as an authenticated user
     * Technical note: A reply is a comment that stores the ID of another comment
     * as its parentId, creating a nested relationship.
     *
     * @param commentId      ID of comment to reply to
     * @param commentRequest {@link CommentRequest}
     * @return {@link ReplyCommentResponse}
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/comments/{commentId}/replies")
    public ResponseEntity<ReplyCommentResponse> replyComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestBody @Valid CommentRequest commentRequest
    ) {


        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.replyComment(commentId, userDetails.getUserId(), commentRequest));


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
            @PathVariable Long commentId,
            @ParameterObject @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(commentService.findAllRepliesForComment(commentId, pageable));

    }

    /**
     * Updates a specific comment as an authenticated user and verified owner of comment
     * Only the owner of the comment is authorized to perform this update
     *
     * @param commentId      ID of comment to update
     * @param commentRequest {@link CommentRequest}
     * @return {@link UpdateCommentResponse}
     */
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<UpdateCommentResponse> updateComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestBody @Valid CommentRequest commentRequest
    ) {

        return ResponseEntity.ok(commentService.updateComment(commentId, userDetails.getUserId(), commentRequest));
    }

    /**
     * Deletes comment as an authenticated user and verified owner of comment
     *
     * @param commentId ID of comment to delete
     * @return ResponseEntity with no content (204)
     */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @PathVariable Long commentId) {


        commentService.deleteComment(commentId, userDetails.getUserId());

        return ResponseEntity.noContent().build();
    }
}

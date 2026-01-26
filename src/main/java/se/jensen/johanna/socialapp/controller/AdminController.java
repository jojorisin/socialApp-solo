package se.jensen.johanna.socialapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.jensen.johanna.socialapp.dto.*;
import se.jensen.johanna.socialapp.dto.admin.RoleRequest;
import se.jensen.johanna.socialapp.dto.admin.RoleResponse;
import se.jensen.johanna.socialapp.service.CommentService;
import se.jensen.johanna.socialapp.service.PostService;
import se.jensen.johanna.socialapp.service.UserService;

/**
 * Controller handling all Admin-related operations
 * Provides endpoints to edit and delete users, posts and comments
 */
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;


    /**
     * Retrieves a paginated list of all users with MEMBER and ADMIN role
     *
     * @param pageable Paginates list
     * @return {@link AdminUserDTO} a detailed list of users
     */
    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserDTO>> getAllUsers(
            @ParameterObject @PageableDefault(size = 10, sort = "username", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<AdminUserDTO> adminUserDTOS = userService.getAllUsersAdmin(pageable);
        return ResponseEntity.ok(adminUserDTOS);

    }

    /**
     * Grants or updates roles for a user.
     *
     * @param roleRequest the request object containing role assignment details.
     * @return a {@link ResponseEntity} containing the {@link RoleResponse}.
     */

    @PutMapping("/roles")
    public ResponseEntity<RoleResponse> addRole(@Valid @RequestBody RoleRequest roleRequest) {
        RoleResponse roleResponse = userService.addRole(roleRequest);

        return ResponseEntity.ok(roleResponse);
    }

    /**
     * Deletes a user from the system.
     *
     * @param userId the ID of the user to delete.
     * @return a {@link ResponseEntity} with 204 No Content status on success.
     */

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);

        return ResponseEntity.noContent().build();
    }


    /**
     * Deletes a post from the system.
     *
     * @param postId the ID of the post to delete.
     * @return a {@link ResponseEntity} with no content (204) upon successful deletion.
     */
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePostAdmin(@PathVariable Long postId) {
        postService.deletePostAdmin(postId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Updates an existing post using administrative permissions.
     *
     * @param postRequest the request body containing update details.
     * @param postId      the ID of the post to update.
     * @return a {@link ResponseEntity} containing the {@link UpdatePostResponse}.
     */
    @PatchMapping("/posts/{postId}")
    public ResponseEntity<UpdatePostResponse> updatePostAdmin(
            @RequestBody @Valid PostRequest postRequest,
            @PathVariable Long postId) {
        UpdatePostResponse postResponse =
                postService.updatePostAdmin(postRequest, postId);

        return ResponseEntity.ok(postResponse);
    }

    /**
     * Updates user information from an administrative perspective.
     *
     * @param userId      the ID of the user to update.
     * @param userRequest the request object containing updated user information.
     * @return a {@link ResponseEntity} containing the {@link UpdateUserResponse}.
     */
    @PatchMapping("/users/{userId}")
    public ResponseEntity<UpdateUserResponse> updateUserAdmin(
            @PathVariable Long userId,
            @RequestBody UpdateUserRequest userRequest) {
        UpdateUserResponse userResponse = userService.updateUserAdmin(
                userRequest, userId);

        return ResponseEntity.ok(userResponse);
    }

    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<UpdateCommentResponse> editComment(
            @PathVariable Long commentId,
            @RequestBody @Valid CommentRequest commentRequest
    ) {
        UpdateCommentResponse commentResponse = commentService.editComment(commentId, commentRequest);

        return ResponseEntity.ok(commentResponse);

    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId
    ) {
        commentService.deleteComment(commentId);

        return ResponseEntity.noContent().build();

    }

}

package se.jensen.johanna.socialapp.controller;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.jensen.johanna.socialapp.dto.PostResponseDTO;
import se.jensen.johanna.socialapp.dto.admin.AdminUpdatePostRequest;
import se.jensen.johanna.socialapp.dto.admin.AdminUpdatePostResponse;
import se.jensen.johanna.socialapp.service.PostService;

/**
 * Controller for managing posts as an administrator.
 * Provides endpoints to retrieve, update, and delete posts,
 * as well as fetch all posts with pagination support.
 * All operations require the user to have an ADMIN role.
 */
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/admin/posts")
@RequiredArgsConstructor
public class AdminPostController {
    private final PostService postService;


    /**
     * Retrieves a specific post by its unique identifier.
     *
     * @param postId the ID of the post to retrieve.
     * @return a {@link ResponseEntity} containing the {@link PostResponseDTO} if found.
     */

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> getPost(@PathVariable
                                                   Long postId) {
        PostResponseDTO postResponseDTOS = postService.findPost(postId);

        return ResponseEntity.ok(postResponseDTOS);

    }

    /**
     * Retrieves a paginated list of all posts in the system.
     *
     * @param pageable pagination and sorting information (default: 10 items, sorted by createdAt descending).
     * @return a {@link ResponseEntity} containing a {@link Page} of {@link PostResponseDTO}.
     */
    @GetMapping
    public @NonNull ResponseEntity<Page<PostResponseDTO>> getAllPostsAdmin(
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<PostResponseDTO> postResponseDTOS = postService.findAllPosts(pageable);
        return ResponseEntity.ok(postResponseDTOS);
    }

    /**
     * Updates an existing post using administrative permissions.
     *
     * @param adminRequest the request body containing update details.
     * @param postId       the ID of the post to update.
     * @return a {@link ResponseEntity} containing the {@link AdminUpdatePostResponse}.
     */
    @PatchMapping("/{postId}")
    public ResponseEntity<AdminUpdatePostResponse> updatePostAdmin(@RequestBody
                                                                   AdminUpdatePostRequest
                                                                           adminRequest,
                                                                   @PathVariable Long postId) {
        AdminUpdatePostResponse adminUpdatePostResponse =
                postService.updatePostAdmin(adminRequest, postId);

        return ResponseEntity.ok(adminUpdatePostResponse);
    }

    /**
     * Deletes a post from the system.
     *
     * @param postId the ID of the post to delete.
     * @return a {@link ResponseEntity} with no content (204) upon successful deletion.
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePostAdmin(@PathVariable Long postId) {
        postService.deletePostAdmin(postId);

        return ResponseEntity.noContent().build();
    }
}

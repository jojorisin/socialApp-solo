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
import se.jensen.johanna.socialapp.dto.PostDTO;
import se.jensen.johanna.socialapp.dto.PostRequest;
import se.jensen.johanna.socialapp.dto.PostResponseDTO;
import se.jensen.johanna.socialapp.dto.UpdatePostResponse;
import se.jensen.johanna.socialapp.security.MyUserDetails;
import se.jensen.johanna.socialapp.service.PostService;

/**
 * Controller for handling operations related to posts in the system.
 * Provides endpoints for creating, retrieving, updating, and deleting posts.
 */
@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;


    /**
     * Retrieves a paginated list of all posts, sorted by creation date in descending order.
     *
     * @param pageable the pagination and sorting information provided for the request,
     *                 including page size, page number, and sort order.
     * @return a ResponseEntity containing a paginated list of PostDTO.
     */
    @GetMapping
    public ResponseEntity<Page<PostDTO>> getAllPosts(
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {

        return ResponseEntity.ok(postService.getAllPosts(pageable));

    }

    /**
     * Retrieves a specific post by its ID.
     *
     * @param postId The unique identifier of the post to fetch.
     * @return A ResponseEntity containing the {@link PostDTO}.
     */

    @GetMapping("/{postId}")
    public ResponseEntity<PostDTO> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPost(postId));
    }

    /**
     * Creates a new post for the currently authenticated user.
     *
     * @param postRequest The post-data to be created.
     * @return A ResponseEntity containing the created {@link PostResponseDTO} and HTTP 201 Status.
     */

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<PostResponseDTO> post(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestBody @Valid PostRequest postRequest) {

        return ResponseEntity.status(HttpStatus.CREATED).body(postService.addPost(postRequest, userDetails.getUserId()));

    }

    /**
     * Updates an existing post.
     * The service layer ensures that only the owner of the post can perform the update.
     *
     * @param postId      The ID of the post to update.
     * @param postRequest The updated post-data.
     * @return A {@link ResponseEntity} containing the {@link UpdatePostResponse}.
     */
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{postId}")
    public ResponseEntity<UpdatePostResponse> editPost(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @PathVariable Long postId,
            @RequestBody @Valid PostRequest postRequest) {


        return ResponseEntity.ok(postService.updatePost(postRequest, postId, userDetails.getUserId()));

    }

    /**
     * Deletes a specific post.
     * The service layer ensures that only the owner of the post can perform the deletion.
     *
     * @param postId The ID of the post to delete.
     * @return A {@link ResponseEntity} with HTTP 204 No Content status upon successful deletion.
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @PathVariable Long postId) {

        postService.deletePost(postId, userDetails.getUserId());

        return ResponseEntity.noContent().build();
    }
}

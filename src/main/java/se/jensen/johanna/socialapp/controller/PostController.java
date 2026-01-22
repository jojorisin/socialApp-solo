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
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import se.jensen.johanna.socialapp.dto.PostRequest;
import se.jensen.johanna.socialapp.dto.PostResponseDTO;
import se.jensen.johanna.socialapp.dto.UpdatePostResponseDTO;
import se.jensen.johanna.socialapp.service.PostService;
import se.jensen.johanna.socialapp.util.JwtUtils;

import java.util.Optional;

/**
 * Controller for handling operations related to posts in the system.
 * Provides endpoints for creating, retrieving, updating, and deleting posts.
 */

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    private final JwtUtils jwtUtils;


    /**
     * Retrieves a paginated list of all posts, sorted by creation date in descending order.
     * Optional variable of userId retrieves all posts from that user
     *
     * @param pageable the pagination and sorting information provided for the request,
     *                 including page size, page number, and sort order.
     * @return a ResponseEntity containing a paginated list of PostResponseDTO objects.
     */
    @GetMapping({"", "/user/{userId}"})
    public @NonNull ResponseEntity<Page<PostResponseDTO>> getAllPosts(
            @PathVariable(name = "userId", required = false) Optional<Long> userId,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {

        Page<PostResponseDTO> postResponseDTOS = userId
                .map(id -> postService.findAllPostsForUser(id, pageable))
                .orElseGet(() -> postService.findAllPosts(pageable));


        return ResponseEntity.ok(postResponseDTOS);

    }

    /**
     * Retrieves a specific post by its ID.
     *
     * @param postId The unique identifier of the post to fetch.
     * @return A ResponseEntity containing the {@link PostResponseDTO}.
     */

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> getPost(@PathVariable Long postId) {
        PostResponseDTO postResponseDto = postService.findPost(postId);
        return ResponseEntity.ok(postResponseDto);
    }

    /**
     * Creates a new post for the currently authenticated user.
     *
     * @param jwt  The JWT of the authenticated user.
     * @param post The post-data to be created.
     * @return A ResponseEntity containing the created {@link PostResponseDTO} and HTTP 201 Status.
     */

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<PostResponseDTO> post(@AuthenticationPrincipal
                                                Jwt jwt,
                                                @RequestBody @Valid PostRequest post) {
        Long userId = jwtUtils.extractUserId(jwt);

        PostResponseDTO postResponse = postService.addPost(post, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(postResponse);

    }

    /**
     * Updates an existing post.
     * The service layer ensures that only the owner of the post can perform the update.
     *
     * @param jwt         The JWT of the authenticated user.
     * @param postId      The ID of the post to update.
     * @param postRequest The updated post data.
     * @return A {@link ResponseEntity} containing the {@link UpdatePostResponseDTO}.
     */
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{postId}")
    public ResponseEntity<UpdatePostResponseDTO> editPost(@AuthenticationPrincipal
                                                          Jwt jwt,
                                                          @PathVariable Long postId,
                                                          @RequestBody @Valid PostRequest postRequest) {

        Long userId = jwtUtils.extractUserId(jwt);

        UpdatePostResponseDTO postResponse = postService.updatePost(
                postRequest,
                postId,
                userId);

        return ResponseEntity.ok(postResponse);

    }

    /**
     * Deletes a specific post.
     * The service layer ensures that only the owner of the post can perform the deletion.
     *
     * @param jwt    The JWT of the authenticated user.
     * @param postId The ID of the post to delete.
     * @return A {@link ResponseEntity} with HTTP 204 No Content status upon successful deletion.
     */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@AuthenticationPrincipal
                                           Jwt jwt,
                                           @PathVariable Long postId) {
        Long userId = jwtUtils.extractUserId(jwt);

        postService.deletePost(postId, userId);

        return ResponseEntity.noContent().build();
    }
}

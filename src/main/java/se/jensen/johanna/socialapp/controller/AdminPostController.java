package se.jensen.johanna.socialapp.controller;

import lombok.RequiredArgsConstructor;
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
@RestController
@RequestMapping("/admin/posts")
@RequiredArgsConstructor
public class AdminPostController {
    private final PostService postService;


    //ändra responsen kanske så den är anapssad till admin.
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> getPost(@PathVariable
                                                   Long postId) {
        PostResponseDTO postResponseDTOS = postService.findPost(postId);

        return ResponseEntity.ok(postResponseDTOS);

    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public @NonNull ResponseEntity<Page<PostResponseDTO>> getAllPostsAdmin(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<PostResponseDTO> postResponseDTOS = postService.findAllPosts(pageable);
        return ResponseEntity.ok(postResponseDTOS);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{postId}")
    public ResponseEntity<AdminUpdatePostResponse> updatePostAdmin(@RequestBody
                                                                   AdminUpdatePostRequest
                                                                           adminRequest,
                                                                   @PathVariable Long postId) {
        AdminUpdatePostResponse adminUpdatePostResponse =
                postService.updatePostAdmin(adminRequest, postId);

        return ResponseEntity.ok(adminUpdatePostResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePostAdmin(@PathVariable Long postId) {
        postService.deletePostAdmin(postId);

        return ResponseEntity.noContent().build();
    }
}

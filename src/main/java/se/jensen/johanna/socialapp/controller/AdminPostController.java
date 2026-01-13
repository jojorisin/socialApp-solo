package se.jensen.johanna.socialapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.jensen.johanna.socialapp.dto.PostResponseDTO;
import se.jensen.johanna.socialapp.dto.admin.AdminUpdatePostRequest;
import se.jensen.johanna.socialapp.dto.admin.AdminUpdatePostResponse;
import se.jensen.johanna.socialapp.service.PostService;

import java.util.List;

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
    public ResponseEntity<List<PostResponseDTO>> getAllPostsAdmin() {
        List<PostResponseDTO> postResponseDTOS = postService.findAllPosts();
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

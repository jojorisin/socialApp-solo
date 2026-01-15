package se.jensen.johanna.socialapp.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import se.jensen.johanna.socialapp.dto.PostRequest;
import se.jensen.johanna.socialapp.dto.PostResponseDTO;
import se.jensen.johanna.socialapp.dto.UpdatePostResponseDTO;
import se.jensen.johanna.socialapp.service.PostService;
import se.jensen.johanna.socialapp.util.JwtUtils;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    private final JwtUtils jwtUtils;


    @GetMapping
    public ResponseEntity<List<PostResponseDTO>> getAllPosts() {
        List<PostResponseDTO> postResponseDTOS = postService.findAllPosts();

        return ResponseEntity.ok(postResponseDTOS);

    }

    //här ska va comment
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> getPost(@PathVariable Long postId) {
        PostResponseDTO postResponseDto = postService.findPost(postId);
        return ResponseEntity.ok(postResponseDto);
    }


    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<PostResponseDTO> post(@AuthenticationPrincipal
                                                Jwt jwt,
                                                @RequestBody @Valid PostRequest post) {
        Long userId = jwtUtils.extractUserId(jwt);

        PostResponseDTO postResponse = postService.addPost(post, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(postResponse);

    }

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

package se.jensen.johanna.socialapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.jensen.johanna.socialapp.dto.PostRequest;
import se.jensen.johanna.socialapp.dto.UpdatePostResponse;
import se.jensen.johanna.socialapp.exception.ForbiddenException;
import se.jensen.johanna.socialapp.exception.NotFoundException;
import se.jensen.johanna.socialapp.mapper.PostMapper;
import se.jensen.johanna.socialapp.model.Post;
import se.jensen.johanna.socialapp.model.User;
import se.jensen.johanna.socialapp.repository.PostRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private PostService postService;

    private Long postId;
    private Long userId;
    private PostRequest postRequest;
    private Post existingPost;


    @BeforeEach
    void setUp() {
        postId = 1L;
        userId = 1L;
        postRequest = new PostRequest("Updated content");

        User owner = new User();
        owner.setUserId(userId);

        existingPost = new Post();
        existingPost.setCreatedAt(LocalDateTime.now());
        existingPost.setPostId(postId);
        existingPost.setUser(owner);
        existingPost.setText("Old content");
    }

    @Test
    void updatePost_Success() {
        // Arrange
        UpdatePostResponse expectedResponse =
                new UpdatePostResponse(postId, userId, "Updated content",
                        existingPost.getCreatedAt(), LocalDateTime.now());

        when(postRepository.findById(postId)).thenReturn(Optional.of(existingPost));
        when(postMapper.toUpdatePostResponseDTO(existingPost)).thenReturn(expectedResponse);

        // Act
        UpdatePostResponse actualResponse = postService.updatePost(postRequest, postId, userId);

        // Assert
        assertNotNull(actualResponse);
        verify(postRepository).findById(postId);
        verify(postMapper).updatePost(postRequest, existingPost);
        verify(postRepository).save(existingPost);
        verify(postMapper).toUpdatePostResponseDTO(existingPost);
    }

    @Test
    void updatePost_ThrowsNotFoundException_WhenPostDoesNotExist() {
        // Arrange
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
                postService.updatePost(postRequest, postId, userId)
        );
        verify(postRepository, never()).save(any());
    }

    @Test
    void updatePost_ThrowsForbiddenException_WhenUserIsNotAuthor() {
        // Arrange
        Long wrongUserId = 99L;
        when(postRepository.findById(postId)).thenReturn(Optional.of(existingPost));

        // Act & Assert
        assertThrows(ForbiddenException.class, () ->
                postService.updatePost(postRequest, postId, wrongUserId)
        );
        verify(postRepository, never()).save(any());
    }
}
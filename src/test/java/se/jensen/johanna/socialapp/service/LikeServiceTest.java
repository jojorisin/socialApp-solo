package se.jensen.johanna.socialapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.jensen.johanna.socialapp.dto.LikeResponse;
import se.jensen.johanna.socialapp.model.Post;
import se.jensen.johanna.socialapp.model.User;
import se.jensen.johanna.socialapp.repository.PostLikeRepository;
import se.jensen.johanna.socialapp.service.helper.EntityProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @InjectMocks
    private LikeService likeService;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private EntityProvider entityProvider;

    private Long postId;
    private Long userId;
    private Post post;
    private User user;

    @BeforeEach
    void setUp() {
        this.postId = 1L;
        this.userId = 2L;
        this.post = new Post();
        post.setPostId(postId);
        this.user = new User();
        user.setUserId(2L);

    }

    @Test
    void deletePostLike_whenIsCurrentlyLiked() {

        when(postLikeRepository.existsBetween(postId, userId)).thenReturn(true);
        when(postLikeRepository.countByPost_PostId(postId)).thenReturn(0);

        LikeResponse likeResponse = likeService.togglePostLike(postId, userId);

        verify(postLikeRepository).deleteByPost_PostIdAndUser_UserId(postId, userId);
        verify(postLikeRepository, never()).save(any());

        assertEquals(0, likeResponse.likeCount());
        assertEquals(false, likeResponse.likedByMe());


    }

    @Test
    void createPostLike_whenIsNotCurrentlyLiked() {
        when(postLikeRepository.existsBetween(postId, userId)).thenReturn(false);
        when(entityProvider.getPostOrThrow(postId)).thenReturn(post);
        when(entityProvider.getUserOrThrow(userId)).thenReturn(user);
        when(postLikeRepository.countByPost_PostId(postId)).thenReturn(1);

        LikeResponse likeResponse = likeService.togglePostLike(postId, userId);

        verify(postLikeRepository).save(any());
        assertEquals(1, likeResponse.likeCount());
        assertEquals(true, likeResponse.likedByMe());

    }
}
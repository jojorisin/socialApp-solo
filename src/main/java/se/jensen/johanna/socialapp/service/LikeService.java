package se.jensen.johanna.socialapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.jensen.johanna.socialapp.dto.LikeResponse;
import se.jensen.johanna.socialapp.model.*;
import se.jensen.johanna.socialapp.repository.CommentLikeRepository;
import se.jensen.johanna.socialapp.repository.PostLikeRepository;
import se.jensen.johanna.socialapp.service.helper.EntityProvider;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {
    private final PostLikeRepository postLikeRepository;
    private final EntityProvider entityProvider;
    private final CommentLikeRepository commentLikeRepository;


    public LikeResponse togglePostLike(Long postId, Long userId) {
        boolean currentlyLiked = postLikeRepository.existsBetween(postId, userId);
        if (!currentlyLiked) {
            Post post = entityProvider.getPostOrThrow(postId);
            User user = entityProvider.getUserOrThrow(userId);
            postLikeRepository.save(new PostLike(post, user));
        } else {
            postLikeRepository.deleteByPost_PostIdAndUser_UserId(postId, userId);

        }

        return new LikeResponse(postLikeRepository.countByPost_PostId(postId), !currentlyLiked);

    }

    public LikeResponse toggleCommentLike(Long commentId, Long userId) {
        boolean currentlyLiked = commentLikeRepository.existsBetween(commentId, userId);
        if (!currentlyLiked) {
            Comment comment = entityProvider.getCommentOrThrow(commentId);
            User user = entityProvider.getUserOrThrow(userId);
            commentLikeRepository.save(new CommentLike(comment, user));

        } else {
            commentLikeRepository.deleteCommentLikeByComment_CommentIdAndUser_UserId(commentId, userId);
        }

        return new LikeResponse(commentLikeRepository.countByComment_CommentId(commentId), !currentlyLiked);
    }


}

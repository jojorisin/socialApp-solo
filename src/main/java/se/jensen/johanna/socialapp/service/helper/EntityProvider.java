package se.jensen.johanna.socialapp.service.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import se.jensen.johanna.socialapp.exception.NotFoundException;
import se.jensen.johanna.socialapp.model.Comment;
import se.jensen.johanna.socialapp.model.Friendship;
import se.jensen.johanna.socialapp.model.Post;
import se.jensen.johanna.socialapp.model.User;
import se.jensen.johanna.socialapp.repository.CommentRepository;
import se.jensen.johanna.socialapp.repository.FriendshipRepository;
import se.jensen.johanna.socialapp.repository.PostRepository;
import se.jensen.johanna.socialapp.repository.UserRepository;

/**
 * Component class used for retrieving entities and throws exception if not found
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EntityProvider {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final FriendshipRepository friendshipRepository;


    public Post getPostOrThrow(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("Post with id={} not found", postId);
                    return new NotFoundException(String.format("Post with id %d not found.", postId));

                });
    }


    public User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with id={} not found", userId);
                    return new NotFoundException(String.format("User with id %d not found.", userId));
                });
    }


    public Comment getCommentOrThrow(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> {
                            log.warn("Comment with id={} was not found", commentId);
                            return new NotFoundException(String.format("Comment with id %d not found.", commentId));
                        }
                );

    }

    public Friendship getFriendshipOrThrow(Long friendshipId) {
        return friendshipRepository.findById(friendshipId).orElseThrow(() -> {
            log.warn("Friendship with id={} not found", friendshipId);
            return new NotFoundException(String.format("Friendship with id %d not found.", friendshipId));
        });
    }


}

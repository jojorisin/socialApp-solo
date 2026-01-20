package se.jensen.johanna.socialapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.jensen.johanna.socialapp.dto.*;
import se.jensen.johanna.socialapp.exception.NotFoundException;
import se.jensen.johanna.socialapp.exception.UnauthorizedAccessException;
import se.jensen.johanna.socialapp.mapper.CommentMapper;
import se.jensen.johanna.socialapp.model.Comment;
import se.jensen.johanna.socialapp.model.Post;
import se.jensen.johanna.socialapp.model.User;
import se.jensen.johanna.socialapp.repository.CommentRepository;
import se.jensen.johanna.socialapp.repository.PostRepository;
import se.jensen.johanna.socialapp.repository.UserRepository;

import java.util.List;

/**
 * Service class responsible for the business logic of comment management.
 * * Provides functionality for creating, updating, deleting, and retrieving comments,
 * including nested replies. This class also enforces authorization rules and
 * ensures proper exception handling for non-existent or unauthorized access.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentMapper commentMapper;


    /**
     * Creates and saves a comment related to a specific post
     *
     * @param postId         ID of the post to comment on
     * @param userId         ID of the user that's creating the comment
     * @param commentRequest Content of the comment
     * @return Returns {@link CommentResponse}
     */
    public CommentResponse commentPost(Long postId,
                                       Long userId,
                                       CommentRequest commentRequest) {
        log.info("User with id={} is trying to comment on post with id={}", userId, postId);

        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("User with id={} was not found when trying to comment on post with id={}", userId, postId);
            return new NotFoundException("User with id " + userId + " not found.");
        });
        Post post = postRepository.findById(postId).orElseThrow(() -> {
            log.warn("Post with id={} was not found when user with id={} was trying to comment", postId, userId);
            return new NotFoundException("Post with id " + postId + " not found.");
        });

        Comment comment = commentMapper.toComment(commentRequest);
        comment.setUser(user);
        comment.setPost(post);
        commentRepository.save(comment);

        log.info("User with id={} successfully commented on post with id={}", userId, postId);

        return commentMapper.toResponse(comment);

    }

    /**
     * Creates a reply to a specific comment
     *
     * @param parentId       ID of the comment that's receiving a reply-comment
     * @param userId         ID of the user that is creating the reply-comment
     * @param commentRequest Content of the comment
     * @return Returns {@link ReplyCommentResponse}
     * @throws NotFoundException If given userId or parentId does not exist
     */
    public ReplyCommentResponse replyComment(
            Long parentId,
            Long userId,
            CommentRequest commentRequest) {
        log.info("User with id={} is trying to comment on comment with id={}", userId, parentId);

        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("User with id={} was not found when trying to comment on a comment", userId);
            return new NotFoundException("User with id " + userId + " not found.");
        });
        Comment parent = commentRepository.findById(parentId).orElseThrow(() -> {
            log.warn("Comment with id={} was not found when user with id={} was trying to comment", parentId, userId);
            return new NotFoundException("Comment with id " + parentId + " not found.");
        });
        Comment reply = commentMapper.toComment(commentRequest);
        reply.setUser(user);
        parent.addReply(reply);
        commentRepository.save(reply);

        log.info("User with id={} successfully commented on comment with id={}", userId, parentId);

        return commentMapper.toReplyCommentResponse(reply);

    }

    /**
     * Retrieves a list of all Main comments without parentId related to a post.
     *
     * @param postId ID of the post to fetch comments to
     * @return Returns {@link CommentDTO}
     */
    public List<CommentDTO> findAllMainComments(Long postId) {
        return commentRepository.findAllMainComments(postId)
                .stream().map(commentMapper::toCommentDTO).toList();


    }

    /**
     * Retrieves a list of all replies to a specific comment
     *
     * @param commentId ID of comment to fetch replies to
     * @return Returns {@link CommentDTO}
     */
    public List<CommentDTO> findAllRepliesForComment(Long commentId) {
        return commentRepository.findByParent_CommentIdOrderByCreatedAtAsc(commentId)
                .stream().map(commentMapper::toCommentDTO).toList();
    }

    /**
     * Updates the content of an existing comment after verifying ownership
     *
     * @param commentId      ID of comment to update
     * @param userId         ID of the owner to comment
     * @param commentRequest Content of the update
     * @return Returns {@link UpdateCommentResponse}
     * @throws NotFoundException           If no comment exists with the given id
     * @throws UnauthorizedAccessException If the userId does not match comments owner
     */
    public UpdateCommentResponse updateComment(Long commentId, Long userId, CommentRequest commentRequest) {
        log.info("User with id={} is trying to update comment with id={}", userId, commentId);

        Comment commentToUpdate = commentRepository.findById(commentId).orElseThrow(() -> {
            log.warn("Comment with id={} was not found when user with id={} attempted to update it", commentId, userId);
            return new NotFoundException("Comment with id " + commentId + " not found.");
        });
        if (!commentToUpdate.getUser().getUserId().equals(userId)) {
            log.warn("User with id={} is not authorized to update comment with id={}", userId, commentId);
            throw new UnauthorizedAccessException("You are not authorized to update comment");
        }
        commentMapper.updateComment(commentRequest, commentToUpdate);
        commentRepository.save(commentToUpdate);

        log.info("User with id={} successfully updated comment with id={}", userId, commentId);
        return commentMapper.toUpdateCommentResponse(commentToUpdate);
    }

    /**
     * Deleted comment after verifying ownership
     *
     * @param commentId ID of comment to delete
     * @param userId    ID of the owner to comment
     * @throws NotFoundException           If no comment exists with the given id
     * @throws UnauthorizedAccessException If the userId does not match comments owner
     */
    public void deleteComment(Long commentId, Long userId) {
        log.info("User with id={} is trying to delete comment with id={}", userId, commentId);

        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> {
            log.warn("Comment with id={} was not found when user with id={} attempted to delete it", commentId, userId);
            return new NotFoundException("Comment with id " + commentId + " not found.");
        });
        if (!comment.getUser().getUserId().equals(userId)) {
            log.warn("User with id={} is not authorized to delete comment with id={}", userId, commentId);
            throw new UnauthorizedAccessException("You are not authorized to delete comment");
        }
        commentRepository.delete(comment);
        log.info("User with id={} successfully deleted comment with id={}", userId, commentId);
    }
}

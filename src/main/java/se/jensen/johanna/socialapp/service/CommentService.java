package se.jensen.johanna.socialapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
        User user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
        Post post = postRepository.findById(postId).orElseThrow(NotFoundException::new);
        Comment comment = commentMapper.toComment(commentRequest);
        comment.setUser(user);
        comment.setPost(post);
        commentRepository.save(comment);

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
        User user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
        Comment parent = commentRepository.findById(parentId).orElseThrow(NotFoundException::new);
        Comment reply = commentMapper.toComment(commentRequest);
        reply.setUser(user);
        parent.addReply(reply);
        commentRepository.save(reply);

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
        Comment commentToUpdate = commentRepository.findById(commentId).orElseThrow(NotFoundException::new);
        if (!commentToUpdate.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to update comment");
        }
        commentMapper.updateComment(commentRequest, commentToUpdate);
        commentRepository.save(commentToUpdate);
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
        Comment comment = commentRepository.findById(commentId).orElseThrow(NotFoundException::new);
        if (!comment.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to delete comment");
        }
        commentRepository.delete(comment);
    }
}

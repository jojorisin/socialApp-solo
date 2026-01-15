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

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentMapper commentMapper;


    public CommentResponse postComment(Long postId,
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

    //Returnerar inte post om inte de finns kommentar
    public List<CommentDTO> findAllMainComments(Long postId) {
        List<CommentDTO> commentDTOS =
                commentRepository.findAllMainCommentsWithReplies(postId)
                        .stream().map(commentMapper::toCommentDTO).toList();

        return commentDTOS;

    }

    public UpdateCommentResponse updateComment(Long commentId, Long userId, CommentRequest commentRequest) {
        Comment commentToUpdate = commentRepository.findById(commentId).orElseThrow(NotFoundException::new);
        if (!commentToUpdate.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to update comment");
        }
        commentMapper.updateComment(commentRequest, commentToUpdate);
        commentRepository.save(commentToUpdate);
        return commentMapper.toUpdateCommentResponse(commentToUpdate);
    }

    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(NotFoundException::new);
        if (!comment.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to delete comment");
        }
        commentRepository.delete(comment);
    }
}

package se.jensen.johanna.socialapp.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.jensen.johanna.socialapp.dto.CommentRequest;
import se.jensen.johanna.socialapp.dto.UpdateCommentResponse;
import se.jensen.johanna.socialapp.exception.ForbiddenException;
import se.jensen.johanna.socialapp.exception.NotFoundException;
import se.jensen.johanna.socialapp.mapper.CommentMapper;
import se.jensen.johanna.socialapp.model.Comment;
import se.jensen.johanna.socialapp.model.User;
import se.jensen.johanna.socialapp.repository.CommentRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommentMapper commentMapper;
    @InjectMocks
    private CommentService commentService;

    @Test
    @DisplayName("Should throw ForbiddenException when user is not owner")
    void updateComment_ShouldThrowForbidden_WhenUserIsNotOwner() {
        //Arrange
        Long commentId = 1L;
        Long wrongUserId = 2L;
        User owner = new User();
        owner.setUserId(3L);
        Comment comment = new Comment();
        comment.setUser(owner);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        //Act & Assert
        assertThrows(ForbiddenException.class, () ->
                commentService.updateComment(commentId, wrongUserId, new CommentRequest("test"))
        );

        //Verify comment is never saved
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("Should update comment when user is owner")
    void updateComment_ShouldUpdateComment_WhenUserIsOwner() {
        //Arrange
        Long commentId = 1L;
        Long ownerId = 2L;
        User owner = new User();
        owner.setUserId(ownerId);
        Comment commentToUpdate = new Comment();
        commentToUpdate.setUser(owner);
        CommentRequest commentRequest = new CommentRequest("test");


        when(commentRepository.findById(commentId)).thenReturn(Optional.of(commentToUpdate));
        UpdateCommentResponse mockResponse = new UpdateCommentResponse("test", LocalDateTime.now());
        when(commentMapper.toUpdateCommentResponse(commentToUpdate)).thenReturn(mockResponse);

        //Act
        UpdateCommentResponse result = commentService.updateComment(commentId, ownerId, commentRequest);

        //Assert
        assertNotNull(result);
        assertEquals("test", result.text());

        //Verify comment is saved and updated
        verify(commentRepository).save(commentToUpdate);
        verify(commentMapper).updateComment(commentRequest, commentToUpdate);

    }

    @Test
    @DisplayName("Should throw NotFoundException when comment is not found")
    void updateComment_ShouldThrowNotFound_WhenCommentIsNotFound() {
        //Arrange
        Long nonExistingCommentId = 99L;
        when(commentRepository.findById(nonExistingCommentId)).thenReturn(Optional.empty());

        //Act & Assert
        assertThrows(NotFoundException.class, () -> commentService.updateComment(
                nonExistingCommentId, 1L, new CommentRequest("test")));

        //Verify comment is never saved
        verify(commentRepository, never()).save(any(Comment.class));
        verify(commentMapper, never()).updateComment(any(CommentRequest.class), any(Comment.class));


    }
}
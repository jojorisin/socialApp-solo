package se.jensen.johanna.socialapp.service;

import org.junit.jupiter.api.BeforeEach;
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

    private Comment existingComment;
    private User owner;
    private CommentRequest commentRequest;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setUserId(1L);
        existingComment = new Comment();
        existingComment.setUser(owner);
        existingComment.setCommentId(2L);
        commentRequest = new CommentRequest("Updated Content");
    }

    @Test
    @DisplayName("Should throw ForbiddenException when user is not owner")
    void updateComment_ShouldThrowForbidden_WhenUserIsNotOwner() {
        //Arrange
        Long wrongUserId = 2L;


        when(commentRepository.findById(existingComment.getCommentId()))
                .thenReturn(Optional.of(existingComment));

        //Act & Assert
        assertThrows(ForbiddenException.class, () ->
                commentService.updateComment(
                        existingComment.getCommentId(),
                        wrongUserId,
                        commentRequest)
        );

        //Verify comment is never saved
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("Should update comment when user is owner")
    void updateComment_ShouldUpdateComment_WhenUserIsOwner() {


        when(commentRepository.findById(existingComment.getCommentId())).thenReturn(Optional.of(existingComment));
        UpdateCommentResponse mockResponse = new UpdateCommentResponse(
                commentRequest.text(), LocalDateTime.now());
        when(commentMapper.toUpdateCommentResponse(existingComment)).thenReturn(mockResponse);

        //Act
        UpdateCommentResponse result = commentService.updateComment(
                existingComment.getCommentId(), owner.getUserId(), commentRequest);

        //Assert
        assertNotNull(result);
        assertEquals(commentRequest.text(), result.text());

        //Verify comment is saved and updated
        verify(commentRepository).save(existingComment);
        verify(commentMapper).updateComment(commentRequest, existingComment);

    }

    @Test
    @DisplayName("Should throw NotFoundException when comment is not found")
    void updateComment_ShouldThrowNotFound_WhenCommentIsNotFound() {
        //Arrange
        Long nonExistingCommentId = 99L;
        when(commentRepository.findById(nonExistingCommentId)).thenReturn(Optional.empty());

        //Act & Assert
        assertThrows(NotFoundException.class, () -> commentService.updateComment(
                nonExistingCommentId, owner.getUserId(), commentRequest));

        //Verify comment is never saved and mapper is never used
        verifyNoInteractions(commentMapper);
        verify(commentRepository, never()).save(any(Comment.class));


    }
}
package se.jensen.johanna.socialapp.mapper;

import org.mapstruct.*;
import se.jensen.johanna.socialapp.dto.*;
import se.jensen.johanna.socialapp.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    Comment toComment(CommentRequest commentRequest);

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "postId", source = "post.postId")
    CommentResponse toResponse(Comment comment);

    @Mapping(target = "parentId", source = "parent.commentId")
    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "username", source = "user.username")
    ReplyCommentResponse toReplyCommentResponse(Comment comment);

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "username", source = "user.username")
    CommentDTO toCommentDTO(Comment comment);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateComment(CommentRequest commentRequest, @MappingTarget Comment comment);

    UpdateCommentResponse toUpdateCommentResponse(Comment comment);
}

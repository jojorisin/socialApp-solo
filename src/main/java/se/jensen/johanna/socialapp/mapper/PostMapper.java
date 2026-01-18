package se.jensen.johanna.socialapp.mapper;

import org.mapstruct.*;
import se.jensen.johanna.socialapp.dto.*;
import se.jensen.johanna.socialapp.dto.admin.AdminUpdatePostRequest;
import se.jensen.johanna.socialapp.dto.admin.AdminUpdatePostResponse;
import se.jensen.johanna.socialapp.model.Post;

import java.util.List;

@Mapper(componentModel = "spring", uses = CommentMapper.class, imports = {java.time.LocalDateTime.class})
public interface PostMapper {

    @Mapping(target = "userId", source = "post.user.userId")
    @Mapping(target = "username", source = "post.user.username")
    @Mapping(target = "comments", expression = "java(getMainComments(post, commentMapper))")
    PostWithCommentsDTO toDTO(Post post, @Context CommentMapper commentMapper);

    default List<CommentDTO> getMainComments(Post post, CommentMapper commentMapper) {
        if (post.getComments() == null) return List.of();

        return post.getComments().stream()
                .filter(comment -> comment.getParent() == null)
                .map(commentMapper::toCommentDTO)  //
                .toList();
    }

    PostResponse toPostResponse(Post post);

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "username", source = "user.username")
    PostResponseDTO toPostResponseDTO(Post post);

    /**
     * @param postRequest maps post from postrequest
     * @return post
     */
    @Mapping(target = "updatedAt", ignore = true)
    Post toPost(PostRequest postRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    void updatePost(PostRequest postRequest, @MappingTarget Post post);

    @Mapping(target = "userId", source = "post.user.userId")
    UpdatePostResponseDTO toUpdatePostResponseDTO(Post post);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePostAdmin(AdminUpdatePostRequest adminRequest, @MappingTarget Post post);

    @Mapping(target = "userId", source = "post.user.userId")
    AdminUpdatePostResponse toAdminUpdateResponse(Post post);
}


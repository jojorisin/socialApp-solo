package se.jensen.johanna.socialapp.mapper;

import org.mapstruct.*;
import se.jensen.johanna.socialapp.dto.*;
import se.jensen.johanna.socialapp.model.Post;

@Mapper(componentModel = "spring", uses = CommentMapper.class, imports = {java.time.LocalDateTime.class})
public interface PostMapper {


    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "username", source = "user.username")
    PostResponseDTO toPostResponseDTO(Post post);

    UserPostDTO toUserPostDTO(Post post);

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "profileImagePath", source = "user.profileImagePath")
    PostDTO toPostDTO(Post post);

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
    UpdatePostResponse toUpdatePostResponseDTO(Post post);


}


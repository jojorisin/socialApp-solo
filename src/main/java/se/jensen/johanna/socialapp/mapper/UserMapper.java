package se.jensen.johanna.socialapp.mapper;

import org.mapstruct.*;
import se.jensen.johanna.socialapp.dto.*;
import se.jensen.johanna.socialapp.dto.admin.AdminUpdateUserRequest;
import se.jensen.johanna.socialapp.dto.admin.AdminUpdateUserResponse;
import se.jensen.johanna.socialapp.dto.admin.AdminUserDTO;
import se.jensen.johanna.socialapp.model.Role;
import se.jensen.johanna.socialapp.model.User;

@Mapper(componentModel = "spring", uses = {PostMapper.class})
public interface UserMapper {

    UserDTO toUserDTO(User user);

    UserListDTO toUserListDTO(User user);


    UserResponse toUserResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "username", ignore = true)
    void updateUser(UpdateUserRequest userRequest, @MappingTarget User user);

    UserWithPostsDTO toUserWithPosts(User user);

    UpdateUserResponse toUpdateUserResponse(User user);

    @Mapping(target = "role", source = "role")
    @Mapping(target = "password", source = "hashedPw")
    User toUser(UserRequest userRequest, String hashedPw, Role role);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserAdmin(AdminUpdateUserRequest userRequest, @MappingTarget User user);

    AdminUpdateUserResponse toAdminResponse(User user);

    AdminUserDTO toAdminUserDTO(User user);
}

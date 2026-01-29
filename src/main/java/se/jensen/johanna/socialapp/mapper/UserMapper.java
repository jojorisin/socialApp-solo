package se.jensen.johanna.socialapp.mapper;

import org.mapstruct.*;
import se.jensen.johanna.socialapp.dto.*;
import se.jensen.johanna.socialapp.model.Role;
import se.jensen.johanna.socialapp.model.User;

/**
 * Mapper-class for user.
 */
@Mapper(componentModel = "spring", uses = {PostMapper.class})
public interface UserMapper {

    UserDTO toUserDTO(User user);

    UserListDTO toUserListDTO(User user);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "username", ignore = true)
    void updateUser(UpdateUserRequest userRequest, @MappingTarget User user);

    MyDTO toMyDTO(User user);

    UpdateUserResponse toUpdateUserResponse(User user);

    @Mapping(target = "role", source = "role")
    @Mapping(target = "password", source = "hashedPw")
    User toUser(RegisterUserRequest registerUserRequest, String hashedPw, Role role);

    AdminUserDTO toAdminUserDTO(User user);

}

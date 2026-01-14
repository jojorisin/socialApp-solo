package se.jensen.johanna.socialapp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import se.jensen.johanna.socialapp.dto.FriendResponseDTO;
import se.jensen.johanna.socialapp.model.Friendship;

@Mapper(componentModel = "spring")
public interface FriendshipMapper {

    @Mapping(target = "senderId", source = "sender.userId")
    @Mapping(target = "receiverId", source = "receiver.userId")
    FriendResponseDTO toFriendResponseDTO(Friendship friendship);
}

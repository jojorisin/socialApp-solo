package se.jensen.johanna.socialapp.dto;

import se.jensen.johanna.socialapp.model.FriendshipStatus;

public record FriendResponseDTO(
        Long friendshipId,
        FriendshipStatus status,
        Long senderId,
        Long receiverId
) {
}

package se.jensen.johanna.socialapp.dto;

public record MyFriendRequest(
        Long friendshipId,
        Long friendId,
        String friendUsername,
        String profileImagePath,
        boolean isIncoming
) {
}

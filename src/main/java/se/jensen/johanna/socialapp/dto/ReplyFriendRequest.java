package se.jensen.johanna.socialapp.dto;

import se.jensen.johanna.socialapp.model.FriendshipStatus;

public record ReplyFriendRequest(
        FriendshipStatus status
) {
}

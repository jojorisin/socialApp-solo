package se.jensen.johanna.socialapp.dto;

import se.jensen.johanna.socialapp.model.FriendshipStatus;

public record FriendshipStatusDTO(
        Long friendshipId,
        FriendshipStatus status,
        boolean isIncomingRequest // True if the logged-in user is the receiver (needs to Accept/Reject)
) {
}
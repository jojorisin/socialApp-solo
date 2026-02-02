package se.jensen.johanna.socialapp.dto;

public record LikeResponse(
        Integer likeCount,
        Boolean likedByMe
) {
}

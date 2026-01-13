package se.jensen.johanna.socialapp.dto;

import java.util.List;

public record HomePageResponse(
        String message,
        List<PostWithCommentsDTO> posts
) {
}

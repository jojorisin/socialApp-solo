package se.jensen.johanna.socialapp.dto;

import java.util.List;

public record HomePageResponse(
        String message,
        String username,
        String profileImagePath,
        List<PostResponse> posts,
        List<UserListDTO> friends
) {
}

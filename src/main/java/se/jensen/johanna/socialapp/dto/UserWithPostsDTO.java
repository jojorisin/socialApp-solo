package se.jensen.johanna.socialapp.dto;

import java.util.List;

public record UserWithPostsDTO(
        String username,
        Long userId,
        List<PostResponseDTO> posts
) {
}

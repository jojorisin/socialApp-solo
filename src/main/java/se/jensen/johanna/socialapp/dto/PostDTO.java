package se.jensen.johanna.socialapp.dto;

import java.time.LocalDateTime;

public record PostDTO(
        Long postId,
        Long userId,
        String username,
        String profileImagePath,
        String text,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

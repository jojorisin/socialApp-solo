package se.jensen.johanna.socialapp.dto;

import java.time.LocalDateTime;

public record PostResponseDTO(
        Long postId,
        Long userId,
        String username,
        String text,
        LocalDateTime createdAt
) {
}

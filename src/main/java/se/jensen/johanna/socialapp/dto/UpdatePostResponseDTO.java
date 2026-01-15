package se.jensen.johanna.socialapp.dto;

import java.time.LocalDateTime;

public record UpdatePostResponseDTO(
        Long postId,
        Long userId,
        String text,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

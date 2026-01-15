package se.jensen.johanna.socialapp.dto;

import java.time.LocalDateTime;

public record UpdateCommentResponse(
        String text,
        LocalDateTime updatedAt
) {
}

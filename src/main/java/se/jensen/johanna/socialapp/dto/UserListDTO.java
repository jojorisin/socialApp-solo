package se.jensen.johanna.socialapp.dto;

import java.util.List;

public record UserListDTO(
        Long userId,
        String username,
        String profileImagePath
) {
}

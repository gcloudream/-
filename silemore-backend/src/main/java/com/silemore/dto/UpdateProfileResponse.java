package com.silemore.dto;

import java.time.LocalDateTime;

public record UpdateProfileResponse(
        Long id,
        String email,
        String nickname,
        LocalDateTime updatedAt
) {
}

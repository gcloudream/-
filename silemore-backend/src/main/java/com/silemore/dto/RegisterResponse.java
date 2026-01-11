package com.silemore.dto;

import java.time.LocalDateTime;

public record RegisterResponse(
        Long id,
        String email,
        String nickname,
        LocalDateTime createdAt
) {
}

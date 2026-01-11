package com.silemore.dto;

import java.time.LocalDateTime;

public record ContactCreateResponse(
        Long id,
        String name,
        String email,
        String relationship,
        boolean isVerified,
        LocalDateTime verifyEmailSentAt,
        LocalDateTime createdAt
) {
}

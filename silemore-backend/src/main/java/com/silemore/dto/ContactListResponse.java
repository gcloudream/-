package com.silemore.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ContactListResponse(
        List<ContactItem> contacts,
        int total,
        int limit,
        int remaining
) {
    public record ContactItem(
            Long id,
            String name,
            String email,
            boolean isVerified,
            LocalDateTime createdAt
    ) {
    }
}

package com.silemore.dto;

import java.time.LocalDateTime;

public record UserProfileResponse(
        Long id,
        String email,
        String nickname,
        int alertDays,
        String reminderTime,
        boolean reminderEnabled,
        boolean isPaused,
        LocalDateTime pauseUntil,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Stats stats,
        Contacts contacts
) {
    public record Stats(
            long totalCheckIns,
            int currentStreak,
            int longestStreak,
            LocalDateTime lastCheckInAt
    ) {
    }

    public record Contacts(int total, int verified) {
    }
}

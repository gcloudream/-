package com.silemore.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        LoginUser user
) {
    public record LoginUser(
            Long id,
            String email,
            String nickname,
            int alertDays,
            String reminderTime,
            boolean isPaused,
            boolean hasCheckedInToday,
            int streakDays
    ) {
    }
}

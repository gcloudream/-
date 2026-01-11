package com.silemore.dto;

import java.time.LocalDateTime;

public record SettingsResponse(
        int alertDays,
        String reminderTime,
        boolean reminderEnabled,
        boolean isPaused,
        LocalDateTime pauseUntil
) {
}

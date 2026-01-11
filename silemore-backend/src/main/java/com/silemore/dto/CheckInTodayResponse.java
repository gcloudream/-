package com.silemore.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CheckInTodayResponse(
        boolean hasCheckedIn,
        CheckInItem checkIn,
        Stats stats
) {
    public record CheckInItem(
            Long id,
            LocalDate checkInDate,
            LocalDateTime checkInTime
    ) {
    }

    public record Stats(
            int currentStreak,
            long missedDays,
            int alertThreshold,
            LocalDateTime lastCheckInAt
    ) {
    }
}

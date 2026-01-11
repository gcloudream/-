package com.silemore.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CheckInResponse(
        Long id,
        LocalDate checkInDate,
        LocalDateTime checkInTime,
        int streakDays,
        boolean isNewRecord,
        String encouragement
) {
}

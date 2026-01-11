package com.silemore.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record CheckInHistoryResponse(
        List<CheckInHistoryItem> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public record CheckInHistoryItem(
            Long id,
            LocalDate checkInDate,
            LocalDateTime checkInTime
    ) {
    }
}

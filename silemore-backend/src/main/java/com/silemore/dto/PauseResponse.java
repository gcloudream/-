package com.silemore.dto;

import java.time.LocalDateTime;

public record PauseResponse(
        boolean isPaused,
        LocalDateTime pauseUntil,
        String reason
) {
}

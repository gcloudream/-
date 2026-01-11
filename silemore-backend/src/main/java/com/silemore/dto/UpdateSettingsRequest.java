package com.silemore.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public record UpdateSettingsRequest(
        @Min(1) @Max(7) Integer alertDays,
        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Invalid time format") String reminderTime,
        Boolean reminderEnabled
) {
}

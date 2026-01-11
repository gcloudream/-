package com.silemore.dto;

import jakarta.validation.constraints.Size;

public record PauseRequest(
        String action,
        Integer duration,
        @Size(max = 200) String reason
) {
}

package com.silemore.dto;

import java.util.Map;

public record HealthResponse(
        String status,
        Map<String, Map<String, String>> components
) {
}

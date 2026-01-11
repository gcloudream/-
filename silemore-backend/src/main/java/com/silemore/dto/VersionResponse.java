package com.silemore.dto;

public record VersionResponse(
        String version,
        String buildTime,
        String gitCommit
) {
}

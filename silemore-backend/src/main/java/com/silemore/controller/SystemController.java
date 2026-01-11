package com.silemore.controller;

import com.silemore.dto.HealthResponse;
import com.silemore.dto.VersionResponse;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemController {
    private final String version;
    private final String buildTime;
    private final String gitCommit;

    public SystemController(@Value("${app.version:1.0.0}") String version,
                            @Value("${app.build-time:unknown}") String buildTime,
                            @Value("${app.git-commit:unknown}") String gitCommit) {
        this.version = version;
        this.buildTime = buildTime;
        this.gitCommit = gitCommit;
    }

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("UP", Map.of(
                "db", Map.of("status", "UP"),
                "mail", Map.of("status", "UP")
        ));
    }

    @GetMapping("/version")
    public com.silemore.dto.ApiResponse<VersionResponse> version() {
        VersionResponse data = new VersionResponse(version, buildTime, gitCommit);
        return com.silemore.dto.ApiResponse.success(data);
    }
}

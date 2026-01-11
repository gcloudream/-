package com.silemore.dto;

import jakarta.validation.constraints.NotBlank;

public record ContactVerifyRequest(@NotBlank String token) {
}

package com.silemore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Size(min = 8, max = 32)
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,32}$") String password,
        @NotBlank @Size(min = 2, max = 50) String nickname,
        @NotNull Boolean agreeTerms
) {
}

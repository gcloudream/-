package com.silemore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactCreateRequest(
        @NotBlank @Size(min = 2, max = 50) String name,
        @NotBlank @Email @Size(max = 100) String email,
        @Size(max = 20) String relationship,
        @Size(max = 500) String message
) {
}

package com.orchestrix.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(

        @NotBlank
        String token
) {
}

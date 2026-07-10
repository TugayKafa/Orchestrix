package com.orchestrix.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(

        @NotBlank
        String token
) {
}

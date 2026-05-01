package com.orchestrix.security;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(

        @NotBlank
        String token
) {
}

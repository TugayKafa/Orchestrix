package com.orchestrix.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @Email
        @NotBlank
        String email,

        @NotBlank
        @Size(min = 8, max = 32,
                message = "Password must be between 8-32 characters long")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=]).+$",
                message = "Password must contain uppercase, lowercase, digit and special character")
        String password,

        @NotBlank
        @Size(min = 4,
                message = "Name must be at least 4 letters long")
        @Pattern(regexp = "^[a-zA-Z]+$", message = "Name must contain only letters")
        String name
) {
}

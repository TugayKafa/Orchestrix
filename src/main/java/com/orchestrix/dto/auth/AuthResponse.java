package com.orchestrix.dto.auth;

public record AuthResponse(String email, String refreshToken, String accessToken) {
}

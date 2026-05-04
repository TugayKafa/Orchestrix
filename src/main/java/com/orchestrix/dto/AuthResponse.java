package com.orchestrix.dto;

public record AuthResponse(String email, String refreshToken, String accessToken) {
}

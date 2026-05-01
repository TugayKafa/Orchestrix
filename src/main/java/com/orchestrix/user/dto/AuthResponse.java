package com.orchestrix.user.dto;

public record AuthResponse(String email, String refreshToken, String accessToken) {
}
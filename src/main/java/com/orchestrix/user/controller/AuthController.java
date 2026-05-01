package com.orchestrix.user.controller;

import com.orchestrix.security.AccessTokenResponse;
import com.orchestrix.security.JwtService;
import com.orchestrix.security.RefreshRequest;
import com.orchestrix.security.RefreshTokenService;
import com.orchestrix.user.dto.AuthResponse;
import com.orchestrix.user.dto.LoginRequest;
import com.orchestrix.user.dto.RegisterRequest;
import com.orchestrix.user.entity.User;
import com.orchestrix.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(UserService userService,
                          JwtService jwtService,
                          RefreshTokenService refreshTokenService
    ) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        User user = userService.register(request.email(), request.password(), request.name());
        String refreshToken = refreshTokenService.generateRefreshToken(user).getToken();
        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(
                        request.email(),
                        refreshToken,
                        accessToken
                        )
                );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        User user = userService.login(request.email(), request.password());
        String refreshToken = refreshTokenService.generateRefreshToken(user).getToken();
        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole());
        return ResponseEntity.ok()
                .body(new AuthResponse(
                        request.email(),
                        refreshToken,
                        accessToken
                        )
                );
    }
}

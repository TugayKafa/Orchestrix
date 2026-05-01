package com.orchestrix.controller;

import com.orchestrix.dto.AccessTokenResponse;
import com.orchestrix.dto.AuthResponse;
import com.orchestrix.dto.LoginRequest;
import com.orchestrix.dto.RefreshRequest;
import com.orchestrix.dto.RegisterRequest;
import com.orchestrix.entity.User;
import com.orchestrix.security.JwtService;
import com.orchestrix.service.RefreshTokenService;
import com.orchestrix.service.UserService;
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

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refreshToken(@RequestBody @Valid RefreshRequest request) {
        String accessToken = refreshTokenService.generateAccessToken(request.token());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AccessTokenResponse(accessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid RefreshRequest request) {
        refreshTokenService.revokeToken(request.token());
        return ResponseEntity.noContent().build();
    }
}

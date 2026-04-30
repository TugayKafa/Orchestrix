package com.orchestrix.user.controller;

import com.orchestrix.security.JwtService;
import com.orchestrix.user.dto.AuthResponse;
import com.orchestrix.user.dto.LoginRequest;
import com.orchestrix.user.dto.RegisterRequest;
import com.orchestrix.user.entity.Role;
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

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        userService.register(request.email(), request.password(), request.name());
        String token = jwtService.generateToken(request.email(), Role.USER);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, request.email()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        User user = userService.login(request.email(), request.password());
        String token = jwtService.generateToken(user.getEmail(), user.getRole());
        return ResponseEntity.ok()
                .body(new AuthResponse(token, request.email()));
    }
}

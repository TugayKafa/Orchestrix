package com.orchestrix.controller;

import com.orchestrix.dto.AccessTokenResponse;
import com.orchestrix.dto.AuthResponse;
import com.orchestrix.dto.LoginRequest;
import com.orchestrix.dto.RefreshRequest;
import com.orchestrix.dto.RegisterRequest;
import com.orchestrix.entity.User;
import com.orchestrix.security.JwtService;
import com.orchestrix.security.SecurityConstants;
import com.orchestrix.security.TokenBlacklistService;
import com.orchestrix.service.RefreshTokenService;
import com.orchestrix.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthController(UserService userService,
                          JwtService jwtService,
                          RefreshTokenService refreshTokenService,
                          TokenBlacklistService tokenBlacklistService
    ) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        User user = userService.register(request.email(), request.password(), request.firstName(), request.lastName());
        logger.info("User registered: {}", user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(buildAuthResponse(user));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        User user = userService.login(request.email(), request.password());
        logger.info("User logged in: {}", user.getEmail());
        return ResponseEntity.ok().body(buildAuthResponse(user));
    }

    private AuthResponse buildAuthResponse(User user) {
        String refreshToken = refreshTokenService.generateRefreshToken(user).getToken();
        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole());
        return new AuthResponse(user.getEmail(), refreshToken, accessToken);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refreshToken(@RequestBody @Valid RefreshRequest request) {
        String accessToken = refreshTokenService.generateAccessToken(request.token());
        logger.info("Access token refreshed");
        return ResponseEntity.ok(new AccessTokenResponse(accessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid RefreshRequest request,
                                       HttpServletRequest httpRequest) {
        refreshTokenService.revokeToken(request.token());
        String authHeader = httpRequest.getHeader(SecurityConstants.AUTHORIZATION_HEADER);

        if (authHeader != null && authHeader.startsWith(SecurityConstants.TOKEN_TYPE)) {
            String accessToken = authHeader.substring(SecurityConstants.TOKEN_TYPE.length());
            tokenBlacklistService.blacklist(accessToken);
        }

        logger.info("User logged out");
        return ResponseEntity.noContent().build();
    }
}

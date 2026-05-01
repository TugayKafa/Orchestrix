package com.orchestrix.service;

import com.orchestrix.entity.RefreshToken;
import com.orchestrix.entity.User;
import com.orchestrix.exception.RefreshTokenExpiredException;
import com.orchestrix.exception.RefreshTokenNotFoundException;
import com.orchestrix.exception.RefreshTokenRevokedException;
import com.orchestrix.repository.RefreshTokenRepository;
import com.orchestrix.security.JwtService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    public RefreshToken generateRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken(user, UUID.randomUUID().toString());
        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public String generateAccessToken(String token) {
        RefreshToken refreshToken = findRefreshToken(token);

        return jwtService.generateToken(
                refreshToken.getUser().getEmail(),
                refreshToken.getUser().getRole()
        );
    }

    public void revokeToken(String token) {
        RefreshToken refreshToken = findRefreshToken(token);
        refreshToken.revokeToken();
        refreshTokenRepository.save(refreshToken);
    }

    private RefreshToken findRefreshToken(String token) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(token);

        if (refreshToken.isEmpty()) {
            throw new RefreshTokenNotFoundException("Invalid refresh token.");
        }
        if (refreshToken.get().getRevokedAt() != null) {
            throw new RefreshTokenRevokedException("Revoked refresh token.");
        }
        if (refreshToken.get().getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RefreshTokenExpiredException("Expired refresh token.");
        }

        return refreshToken.get();
    }
}

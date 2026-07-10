package com.orchestrix.service;

import com.orchestrix.entity.auth.AuthProvider;
import com.orchestrix.entity.auth.RefreshToken;
import com.orchestrix.entity.auth.Role;
import com.orchestrix.entity.auth.User;
import com.orchestrix.exception.RefreshTokenExpiredException;
import com.orchestrix.exception.RefreshTokenNotFoundException;
import com.orchestrix.exception.RefreshTokenRevokedException;
import com.orchestrix.repository.RefreshTokenRepository;
import com.orchestrix.security.JwtService;
import com.orchestrix.service.auth.RefreshTokenServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() throws Exception {
        Field expirationField = RefreshTokenServiceImpl.class.getDeclaredField("expirationWeeks");
        expirationField.setAccessible(true);
        expirationField.set(refreshTokenService, 1);
    }

    @Test
    void testGenerateRefreshTokenReturnsValidRefreshToken() {
        User user = new User(
                "ivan@gmail.com", "hash", "Ivan", "Ivanov", Role.USER, AuthProvider.LOCAL);
        RefreshToken refreshToken = refreshTokenService.generateRefreshToken(user);

        assertEquals(
                user,
                refreshToken.getUser(),
                "Expected refresh token to be assigned to 'ivan@gmail.com'"
        );
        assertNotNull(
                refreshToken,
                "Expected a valid (non-null) token to be generated."
        );
        assertFalse(
                refreshToken.getToken().isBlank(),
                "Expected a valid (non-blank) token to be generated."
        );
        assertFalse(
                refreshToken.getExpiresAt().isBefore(LocalDateTime.now()),
                "Expected a valid (non-expired) token to be generated."
        );
        assertNull(
                refreshToken.getRevokedAt(),
                "Expected a valid (non-revoked) token to be generated."
        );
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void testGenerateAccessTokenReturnsValidAccessToken() {
        User user = new User(
                "ivan@gmail.com", "hash", "Ivan", "Ivanov", Role.USER, AuthProvider.LOCAL);
        when(refreshTokenRepository.findByToken("token"))
                .thenReturn(Optional.of(new RefreshToken(user, "token", 1)));
        when(jwtService.generateToken("ivan@gmail.com", Role.USER))
                .thenReturn("mocked-access-token");

        String accessToken = refreshTokenService.generateAccessToken("token");

        assertEquals(
                "mocked-access-token",
                accessToken,
                "Expected 'mocked-access-token' to be generated."
        );
        verify(jwtService).generateToken("ivan@gmail.com", Role.USER);
    }

    @Test
    void testGenerateAccessTokenWithNonexistingRefreshTokenThrowsException() {
        when(refreshTokenRepository.findByToken("token"))
                .thenReturn(Optional.empty());

        assertThrows(RefreshTokenNotFoundException.class,
                () -> refreshTokenService.generateAccessToken("token"),
                "Expected an exception to thrown when no refresh token was found."
        );

        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    void testGenerateAccessTokenWithRevokedRefreshTokenThrowsException() {
        User user = new User(
                "ivan@gmail.com", "hash", "Ivan", "Ivanov", Role.USER, AuthProvider.LOCAL);
        RefreshToken refreshToken = new RefreshToken(user, "token", 1);
        refreshToken.revokeToken();

        when(refreshTokenRepository.findByToken("token"))
                .thenReturn(Optional.of(refreshToken));

        assertThrows(RefreshTokenRevokedException.class,
                () -> refreshTokenService.generateAccessToken("token"),
                "Expected an exception to be thrown when used refresh token was revoked."
        );

        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    void testGenerateAccessTokenWithExpiredRefreshTokenThrowsException() throws Exception {
        User user = new User(
                "ivan@gmail.com", "hash", "Ivan", "Ivanov", Role.USER, AuthProvider.LOCAL);
        RefreshToken refreshToken = new RefreshToken(user, "token", 1);

        Field expiresAtField = RefreshToken.class.getDeclaredField("expiresAt");
        expiresAtField.setAccessible(true);
        expiresAtField.set(refreshToken, LocalDateTime.now().minusDays(1));

        when(refreshTokenRepository.findByToken("token"))
                .thenReturn(Optional.of(refreshToken));

        assertThrows(RefreshTokenExpiredException.class,
                () -> refreshTokenService.generateAccessToken("token"),
                "Expected an exception to be thrown when used refresh token was expired."
        );

        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    void testRevokeTokenSuccessfully() {
        User user = new User(
                "ivan@gmail.com", "hash", "Ivan", "Ivanov", Role.USER, AuthProvider.LOCAL);
        RefreshToken refreshToken = new RefreshToken(user, "token", 1);

        when(refreshTokenRepository.findByToken("token"))
                .thenReturn(Optional.of(refreshToken));

        refreshTokenService.revokeToken("token");

        assertNotNull(refreshToken.getRevokedAt());
        verify(refreshTokenRepository).save(refreshToken);
    }
}

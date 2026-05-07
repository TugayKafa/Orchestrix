package com.orchestrix.service;

import com.orchestrix.repository.RefreshTokenRepository;
import com.orchestrix.service.auth.RefreshTokenCleanupScheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RefreshTokenCleanupSchedulerTest {

    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    RefreshTokenCleanupScheduler scheduler;

    @Test
    void testCleanUpDeletesExpiredOrRevokedTokens() {
        scheduler.cleanupExpiredTokens();
        verify(refreshTokenRepository).deleteExpiredOrRevoked(any(LocalDateTime.class));
    }
}
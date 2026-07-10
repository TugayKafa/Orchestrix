package com.orchestrix.service.auth;

import com.orchestrix.entity.auth.RefreshToken;
import com.orchestrix.entity.auth.User;

public interface RefreshTokenService {

    RefreshToken generateRefreshToken(User user);

    String generateAccessToken(String token);

    void revokeToken(String token);
}

package com.cosmic.user_service.service;

import com.cosmic.user_service.entity.RefreshToken;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(String email);

    RefreshToken verifyExpiration(RefreshToken token);

    void revokeToken(String token);
}

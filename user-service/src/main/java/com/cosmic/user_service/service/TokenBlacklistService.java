package com.cosmic.user_service.service;

public interface TokenBlacklistService {
    void blacklistToken(String token);

    boolean isBlacklisted(String token);
}

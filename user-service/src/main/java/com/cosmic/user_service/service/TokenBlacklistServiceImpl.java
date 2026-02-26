package com.cosmic.user_service.service;

import com.cosmic.user_service.entity.BlacklistedToken;
import com.cosmic.user_service.repository.BlacklistedTokenRepository;
import com.cosmic.user_service.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService{

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final JwtService jwtService;


    @Override
    public void blacklistToken(String token) {


        Date expiry = jwtService.extractExpiration(token);


        BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                .token(token)
                .expiryDate(expiry.toInstant())
                .build();


        blacklistedTokenRepository.save(blacklistedToken);
    }


    @Override
    public boolean isBlacklisted(String token) {
        return blacklistedTokenRepository.existsByToken(token);
    }
}

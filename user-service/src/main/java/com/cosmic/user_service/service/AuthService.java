package com.cosmic.user_service.service;

import com.cosmic.user_service.dto.AuthResponseDto;
import com.cosmic.user_service.dto.LoginRequestDto;
import com.cosmic.user_service.dto.RegisterRequestDto;

public interface AuthService {
    void register(RegisterRequestDto registerRequestDto);

    AuthResponseDto login(LoginRequestDto loginRequestDto);

    AuthResponseDto refreshAccessToken(String refreshToken);

    void logout(String refreshToken, String accessToken);
}

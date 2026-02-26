package com.cosmic.user_service.service;

import com.cosmic.user_service.dto.RegisterRequestDto;
import com.cosmic.user_service.dto.UserResponseDto;

public interface UserService {
    UserResponseDto registerUser(RegisterRequestDto registerRequestDto);
}

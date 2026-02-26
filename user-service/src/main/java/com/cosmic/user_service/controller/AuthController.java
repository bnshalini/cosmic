package com.cosmic.user_service.controller;

import com.cosmic.user_service.dto.*;
import com.cosmic.user_service.payload.ApiResponse;
import com.cosmic.user_service.security.jwt.JwtService;
import com.cosmic.user_service.service.AuthService;
import com.cosmic.user_service.service.TokenBlacklistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtService jwtService;


    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(
            @Valid @RequestBody RegisterRequestDto registerRequestDto) {


        authService.register(registerRequestDto);


        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("User registered successfully")
                .data("Registration successful")
                .build();


        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(
            @Valid @RequestBody LoginRequestDto loginRequestDto) {


        AuthResponseDto authResponse = authService.login(loginRequestDto);


        ApiResponse<AuthResponseDto> response = ApiResponse.<AuthResponseDto>builder()
                .success(true)
                .message("Login successful")
                .data(authResponse)
                .build();


        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponseDto>> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDto requestDto) {

        AuthResponseDto responseDto = authService.refreshAccessToken(requestDto.getRefreshToken());

        ApiResponse<AuthResponseDto> response = ApiResponse.<AuthResponseDto>builder()
                .success(true)
                .message("Token refreshed successfully")
                .data(responseDto)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody RefreshTokenRequestDto requestDto) {

        String accessToken = authHeader.substring(7);

        authService.logout(requestDto.getRefreshToken(), accessToken);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("Logout successful")
                .data("Refresh token revoked + Access token blacklisted")
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate-token")
    public ResponseEntity<ApiResponse<TokenValidationResponseDto>> validateToken(
            @RequestHeader("Authorization") String authHeader
    ) {

        String token = authHeader.substring(7);

        // check blacklisted
        boolean isBlacklisted = tokenBlacklistService.isBlacklisted(token);

        if (isBlacklisted) {
            ApiResponse<TokenValidationResponseDto> response = ApiResponse.<TokenValidationResponseDto>builder()
                    .success(false)
                    .message("Token is blacklisted")
                    .data(null)
                    .build();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String email;
        String role;

        try {
            email = jwtService.extractUsername(token);
            role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        } catch (Exception e) {

            ApiResponse<TokenValidationResponseDto> response = ApiResponse.<TokenValidationResponseDto>builder()
                    .success(false)
                    .message("Invalid token")
                    .data(null)
                    .build();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        TokenValidationResponseDto dto = TokenValidationResponseDto.builder()
                .valid(true)
                .email(email)
                .role(role)
                .build();

        ApiResponse<TokenValidationResponseDto> response = ApiResponse.<TokenValidationResponseDto>builder()
                .success(true)
                .message("Token is valid")
                .data(dto)
                .build();

        return ResponseEntity.ok(response);
    }

}

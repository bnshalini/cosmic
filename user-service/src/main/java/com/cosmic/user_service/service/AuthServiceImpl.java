package com.cosmic.user_service.service;

import com.cosmic.user_service.dto.AuthResponseDto;
import com.cosmic.user_service.dto.LoginRequestDto;
import com.cosmic.user_service.dto.RegisterRequestDto;
import com.cosmic.user_service.entity.RefreshToken;
import com.cosmic.user_service.entity.Role;
import com.cosmic.user_service.entity.User;
import com.cosmic.user_service.exceptions.EmailAlreadyExistsException;
import com.cosmic.user_service.repository.RefreshTokenRepository;
import com.cosmic.user_service.repository.UserRepository;
import com.cosmic.user_service.security.jwt.CustomUserDetailsService;
import com.cosmic.user_service.security.jwt.JwtService;
import com.cosmic.user_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public void register(RegisterRequestDto registerRequestDto) {

        if (userRepository.existsByEmail(registerRequestDto.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered: " + registerRequestDto.getEmail());
        }

        User user = User.builder()
                .name(registerRequestDto.getName())
                .email(registerRequestDto.getEmail())
                .password(passwordEncoder.encode(registerRequestDto.getPassword()))
                .role(Role.CUSTOMER)
                .build();

        userRepository.save(user);
    }

    @Override
    public AuthResponseDto login(LoginRequestDto loginRequestDto) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()
                )
        );

        // Load UserDetails (to get role)
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(loginRequestDto.getEmail());

        // Access Token will contain role now
        String accessToken = jwtService.generateAccessToken(userDetails);

        // Refresh token remains same
        String refreshToken = refreshTokenService.createRefreshToken(loginRequestDto.getEmail()).getToken();

        return AuthResponseDto.builder()
                .email(loginRequestDto.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthResponseDto refreshAccessToken(String refreshToken) {

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        refreshTokenService.verifyExpiration(token);

        String email = jwtService.extractUsername(refreshToken);

        // Load UserDetails again (to include role in new token)
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        String newAccessToken = jwtService.generateAccessToken(userDetails);

        return AuthResponseDto.builder()
                .email(email)
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public void logout(String refreshToken, String accessToken) {


        refreshTokenService.revokeToken(refreshToken);


        tokenBlacklistService.blacklistToken(accessToken);
    }


}

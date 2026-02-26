package com.cosmic.user_service.service;

import com.cosmic.user_service.dto.RegisterRequestDto;
import com.cosmic.user_service.dto.UserResponseDto;
import com.cosmic.user_service.entity.Role;
import com.cosmic.user_service.entity.User;
import com.cosmic.user_service.exceptions.EmailAlreadyExistsException;
import com.cosmic.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public UserResponseDto registerUser(RegisterRequestDto registerRequestDto) {
        if (userRepository.existsByEmail(registerRequestDto.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists: " + registerRequestDto.getEmail());
        }


        User user = User.builder()
                .name(registerRequestDto.getName())
                .email(registerRequestDto.getEmail())
                .password(passwordEncoder.encode(registerRequestDto.getPassword()))
                .role(Role.CUSTOMER)
                .build();


        User savedUser = userRepository.save(user);


        return UserResponseDto.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }
}

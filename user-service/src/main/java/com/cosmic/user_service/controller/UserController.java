package com.cosmic.user_service.controller;

import com.cosmic.user_service.dto.RegisterRequestDto;
import com.cosmic.user_service.dto.UserResponseDto;
import com.cosmic.user_service.payload.ApiResponse;
import com.cosmic.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDto>> registerUser(@Valid @RequestBody RegisterRequestDto registerRequestDto) {


        UserResponseDto savedUser = userService.registerUser(registerRequestDto);


        ApiResponse<UserResponseDto> response = ApiResponse.<UserResponseDto>builder()
                .success(true)
                .message("User registered successfully")
                .data(savedUser)
                .build();


        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<String>> getProfile() {

        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("Profile fetched successfully")
                .data("This is protected user profile API")
                .build();

        return ResponseEntity.ok(response);
    }
}

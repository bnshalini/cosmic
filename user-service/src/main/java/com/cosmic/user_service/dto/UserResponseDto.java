package com.cosmic.user_service.dto;

import com.cosmic.user_service.entity.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDto {
    private Long id;
    private String name;
    private String email;
    private Role role;
}

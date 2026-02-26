package com.cosmic.user_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenValidationResponseDto {

    private boolean valid;
    private String email;
    private String role;
}

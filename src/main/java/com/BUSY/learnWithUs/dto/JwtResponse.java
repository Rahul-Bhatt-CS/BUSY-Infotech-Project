package com.BUSY.learnWithUs.dto;

import com.BUSY.learnWithUs.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private UserResponse user;
}

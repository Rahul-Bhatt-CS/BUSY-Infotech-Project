package com.BUSY.learnWithUs.Dto.Auth;

import com.BUSY.learnWithUs.Entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private UserRole role;
}

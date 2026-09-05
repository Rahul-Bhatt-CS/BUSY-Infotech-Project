package com.BUSY.learnWithUs.Dto.Auth;

import com.BUSY.learnWithUs.Entity.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private UserRole role;
}

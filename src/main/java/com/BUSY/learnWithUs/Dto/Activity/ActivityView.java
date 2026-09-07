package com.BUSY.learnWithUs.Dto.Activity;

import com.BUSY.learnWithUs.Dto.Auth.UserResponse;
import com.BUSY.learnWithUs.Entity.ActivityType;

import java.time.Instant;
import java.time.LocalDateTime;

public record ActivityView(
        Long id,
        UserResponse actor,
        ActivityType eventType,
        String description,
        LocalDateTime createdAt
) {
}
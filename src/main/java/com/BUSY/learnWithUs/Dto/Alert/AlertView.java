package com.BUSY.learnWithUs.Dto.Alert;


import com.BUSY.learnWithUs.Dto.Auth.UserResponse;
import com.BUSY.learnWithUs.Dto.Course.CourseView;
import com.BUSY.learnWithUs.Entity.ProgressStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public record AlertView(
        Long id,
        Long enrollmentId,
        UserResponse learner,
        CourseView course,
        ProgressStatus progressStatus,
        LocalDateTime progressUpdatedAt,
        LocalDateTime triggeredAt
) {
}
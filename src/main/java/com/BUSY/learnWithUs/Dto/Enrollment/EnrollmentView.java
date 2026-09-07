package com.BUSY.learnWithUs.Dto.Enrollment;


import com.BUSY.learnWithUs.Dto.Auth.UserResponse;
import com.BUSY.learnWithUs.Dto.Course.CourseView;
import com.BUSY.learnWithUs.Entity.ProgressStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public record EnrollmentView(
        Long id,
        UserResponse learner,
        CourseView course,
        ProgressStatus progressStatus,
        LocalDateTime progressUpdatedAt,
        LocalDateTime enrolledAt,
        LocalDateTime completedAt
) {
}
package com.BUSY.learnWithUs.Dto.Progress;

import com.BUSY.learnWithUs.Entity.ProgressStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public record ProgressView(
        Long enrollmentId,
        Long courseId,
        Long learnerId,
        ProgressStatus progressStatus,
        LocalDateTime progressUpdatedAt,
        LocalDateTime enrolledAt,
        LocalDateTime completedAt
) {
}
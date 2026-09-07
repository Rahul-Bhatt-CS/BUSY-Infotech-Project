package com.BUSY.learnWithUs.Service;

import com.BUSY.learnWithUs.Dto.Progress.ProgressView;
import com.BUSY.learnWithUs.Entity.Enrollment;
import com.BUSY.learnWithUs.Entity.ProgressStatus;
import com.BUSY.learnWithUs.Entity.User;
import com.BUSY.learnWithUs.Repository.*;
import com.BUSY.learnWithUs.Security.JwtUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProgressService {
    private final EnrollmentRepository enrollments;

    @Transactional
    public ProgressView progress(
            User learner,
            Long eid,
            ProgressStatus target
    ) {
        Enrollment e = enrollments.findById(eid)
                .orElseThrow(
                        () -> new EntityNotFoundException("Enrollment not found")
                );

        if (!e.getLearner().getId().equals(learner.getId())) {
            throw new AccessDeniedException(
                    "You can only change your own progress"
            );
        }

        if (
                target == ProgressStatus.IN_PROGRESS
                        && e.getProgressStatus() != ProgressStatus.NOT_STARTED
        ) {
            throw new IllegalArgumentException(
                    "Invalid progress transition"
            );
        }

        if (
                target == ProgressStatus.COMPLETED
                        && e.getProgressStatus() != ProgressStatus.IN_PROGRESS
        ) {
            throw new IllegalArgumentException(
                    "Invalid progress transition"
            );
        }

        e.setProgressStatus(target);
        e.setProgressUpdatedAt(LocalDateTime.now());

        if (target == ProgressStatus.COMPLETED) {
            e.setCompletedAt(LocalDateTime.now());
        }

        enrollments.save(e);

        return new ProgressView(
                e.getId(),
                e.getCourse().getId(),
                e.getLearner().getId(),
                e.getProgressStatus(),
                e.getProgressUpdatedAt(),
                e.getEnrolledAt(),
                e.getCompletedAt()
        );
    }

    public ProgressView ownProgress(
            User learner,
            Long eid
    ) {
        Enrollment e = enrollments.findById(eid)
                .orElseThrow(
                        () -> new EntityNotFoundException("Enrollment not found")
                );

        if (!e.getLearner().getId().equals(learner.getId())) {
            throw new AccessDeniedException(
                    "You can only view your own progress"
            );
        }

        return new ProgressView(
                e.getId(),
                e.getCourse().getId(),
                e.getLearner().getId(),
                e.getProgressStatus(),
                e.getProgressUpdatedAt(),
                e.getEnrolledAt(),
                e.getCompletedAt()
        );
    }

}

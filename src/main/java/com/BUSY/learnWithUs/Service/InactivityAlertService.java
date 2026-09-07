package com.BUSY.learnWithUs.Service;

import com.BUSY.learnWithUs.Dto.Alert.AlertView;
import com.BUSY.learnWithUs.Dto.Auth.UserResponse;
import com.BUSY.learnWithUs.Dto.Course.CourseView;
import com.BUSY.learnWithUs.Entity.Course;
import com.BUSY.learnWithUs.Entity.InactivityAlert;
import com.BUSY.learnWithUs.Entity.User;
import com.BUSY.learnWithUs.Entity.UserRole;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class InactivityAlertService {

    private final InactivityAlertRepository alerts;
    private final EnrollmentRepository enrollments;

    private void instructor(User u) {
        if (u.getRole() != UserRole.INSTRUCTOR) {
            throw new AccessDeniedException("Instructor access required");
        }
    }

    public UserResponse user(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getRole());
    }

    public CourseView course(Course c) {
        return new CourseView(
                c.getId(),
                c.getTitle(),
                c.getDescription(),
                c.getCategory(),
                c.getStatus(),
                user(c.getInstructor()),
                enrollments.countByCourseId(c.getId()),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }

    public List<AlertView> alerts(User u) {
        instructor(u);

        return alerts
                .findByDismissedAtIsNullOrderByTriggeredAtAsc()
                .stream()
                .map(
                        a -> new AlertView(
                                a.getId(),
                                a.getEnrollment().getId(),
                                user(a.getEnrollment().getLearner()),
                                course(a.getEnrollment().getCourse()),
                                a.getEnrollment().getProgressStatus(),
                                a.getEnrollment().getProgressUpdatedAt(),
                                a.getTriggeredAt()
                        )
                )
                .toList();
    }

    public long alertCount(User u) {
        instructor(u);

        return alerts.countByDismissedAtIsNull();
    }

    @Transactional
    public void dismissAlert(
            User u,
            Long id
    ) {
        instructor(u);

        InactivityAlert a = alerts.findById(id)
                .orElseThrow(
                        () -> new EntityNotFoundException("Alert not found")
                );

        a.setDismissedAt(LocalDateTime.now());

        alerts.save(a);
    }


}

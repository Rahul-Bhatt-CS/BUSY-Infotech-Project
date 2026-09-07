package com.BUSY.learnWithUs.Service;

import com.BUSY.learnWithUs.Dto.Activity.ActivityView;
import com.BUSY.learnWithUs.Dto.Activity.CommentRequest;
import com.BUSY.learnWithUs.Dto.Auth.UserResponse;
import com.BUSY.learnWithUs.Entity.*;
import com.BUSY.learnWithUs.Repository.*;
import com.BUSY.learnWithUs.Security.JwtUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {
    private final UserRepository users;
    private final CourseRepository courses;
    private final LessonRepository lessons;
    private final EnrollmentRepository enrollments;
    private final ActivityLogRepository logs;
    private final InactivityAlertRepository alerts;
    private final PasswordEncoder encoder;
    private final JwtUtils jwt;

    public UserResponse user(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getRole());
    }

    private Course requireCourse(Long id) {
        return courses.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
    }


    private void canAccess(User u, Course c) {
        if (u.getRole() == UserRole.INSTRUCTOR) {
            return;
        }

        if (c.getStatus() != CourseStatus.PUBLISHED) {
            throw new AccessDeniedException("Course is not accessible");
        }
    }


    public List<ActivityView> activities(
            User u,
            Long cid
    ) {
        Course c = requireCourse(cid);

        canAccess(u, c);

        return logs
                .findByCourseIdOrderByCreatedAtDesc(cid)
                .stream()
                .map(
                        a -> new ActivityView(
                                a.getId(),
                                user(a.getActor()),
                                a.getEventType(),
                                a.getDescription(),
                                a.getCreatedAt()
                        )
                )
                .toList();
    }

    @Transactional
    public ActivityView comment(
            User u,
            Long cid,
            CommentRequest r
    ) {
        Course c = requireCourse(cid);

        canAccess(u, c);

        if (
                r == null
                        || r.comment() == null
                        || r.comment().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Comment is required"
            );
        }

        ActivityLog a = new ActivityLog();

        a.setCourse(c);
        a.setActor(u);
        a.setEventType(ActivityType.COMMENT);
        a.setDescription(r.comment().trim());

        logs.save(a);

        return new ActivityView(
                a.getId(),
                user(u),
                a.getEventType(),
                a.getDescription(),
                a.getCreatedAt()
        );
    }



}

package com.BUSY.learnWithUs.Service;

import com.BUSY.learnWithUs.Entity.Course;
import com.BUSY.learnWithUs.Entity.Enrollment;
import com.BUSY.learnWithUs.Entity.User;
import com.BUSY.learnWithUs.Entity.UserRole;
import com.BUSY.learnWithUs.Repository.*;
import com.BUSY.learnWithUs.Security.JwtUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseExportService {
    private final UserRepository users;
    private final CourseRepository courses;
    private final LessonRepository lessons;
    private final EnrollmentRepository enrollments;
    private final ActivityLogRepository logs;
    private final InactivityAlertRepository alerts;
    private final PasswordEncoder encoder;
    private final JwtUtils jwt;

    private Course requireCourse(Long id) {
        return courses.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
    }

    private void instructor(User u) {
        if (u.getRole() != UserRole.INSTRUCTOR) {
            throw new AccessDeniedException("Instructor access required");
        }
    }

    private String csvFilter(String s) {
        return "\"" +
                s.replace("\"", "\"\"") +
                "\"";
    }

    public String csv(
            User u,
            Long cid
    ) {
        Course c = requireCourse(cid);

        instructor(u);

        if (!c.getInstructor().getId().equals(u.getId())) {
            throw new AccessDeniedException(
                    "Only the course instructor can export progress"
            );
        }

        StringBuilder b =
                new StringBuilder(
                        "learner_email,progress_status,enrolled_at,progress_updated_at,completed_at\n"
                );

        for (
                Enrollment e :
                enrollments.findByCourseIdOrderByEnrolledAtDesc(cid)
        ) {
            b.append(
                            csvFilter(e.getLearner().getEmail())
                    )
                    .append(',')
                    .append(e.getProgressStatus())
                    .append(',')
                    .append(e.getEnrolledAt())
                    .append(',')
                    .append(e.getProgressUpdatedAt())
                    .append(',')
                    .append(
                            e.getCompletedAt() == null
                                    ? ""
                                    : e.getCompletedAt()
                    )
                    .append('\n');
        }

        return b.toString();
    }
}

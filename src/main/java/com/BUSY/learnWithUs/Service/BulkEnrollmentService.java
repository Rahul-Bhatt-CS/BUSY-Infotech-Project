package com.BUSY.learnWithUs.Service;

import com.BUSY.learnWithUs.Dto.Bulk.BulkEnrollmentRequest;
import com.BUSY.learnWithUs.Dto.Bulk.BulkResult;
import com.BUSY.learnWithUs.Entity.*;
import com.BUSY.learnWithUs.Repository.*;
import com.BUSY.learnWithUs.Security.JwtUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BulkEnrollmentService {
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


    @Transactional
    public List<BulkResult> bulk(
            User u,
            Long cid,
            BulkEnrollmentRequest r
    ) {
        instructor(u);

        Course c = requireCourse(cid);

        if (c.getStatus() != CourseStatus.PUBLISHED) {
            throw new IllegalArgumentException(
                    "Bulk enrollment is only available for published courses"
            );
        }

        if (
                r == null
                        || r.emails() == null
                        || r.emails().isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "At least one email is required"
            );
        }

        List<BulkResult> out = new ArrayList<>();

        for (String raw : r.emails()) {

            String email =
                    raw == null
                            ? ""
                            : raw.trim().toLowerCase();

            if (email.isBlank()) {
                continue;
            }

            Optional<User> learner =
                    users.findByEmailIgnoreCase(email);

            if (
                    learner.isEmpty()
                            || learner.get().getRole() != UserRole.LEARNER
            ) {
                out.add(
                        new BulkResult(
                                email,
                                "Unknown address"
                        )
                );

            } else if (
                    enrollments
                            .findByLearnerIdAndCourseId(
                                    learner.get().getId(),
                                    cid
                            )
                            .isPresent()
            ) {
                out.add(
                        new BulkResult(
                                email,
                                "Already-enrolled learner"
                        )
                );

            } else {
                Enrollment e = new Enrollment();

                e.setCourse(c);
                e.setLearner(learner.get());

                enrollments.save(e);

                out.add(
                        new BulkResult(
                                email,
                                "Newly enrolled"
                        )
                );
            }
        }

        return out;
    }

}

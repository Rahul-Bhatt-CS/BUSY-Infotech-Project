package com.BUSY.learnWithUs.Service;

import com.BUSY.learnWithUs.Dto.Bulk.BulkEnrollmentRequest;
import com.BUSY.learnWithUs.Dto.Bulk.BulkResult;
import com.BUSY.learnWithUs.Entity.*;
import com.BUSY.learnWithUs.Repository.CourseRepository;
import com.BUSY.learnWithUs.Repository.EnrollmentRepository;
import com.BUSY.learnWithUs.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BulkEnrollmentService {

    private final UserRepository users;
    private final CourseRepository courses;
    private final EnrollmentRepository enrollments;

    private Course requireCourse(Long id) {
        return courses.findById(id)
                .orElseThrow(
                        () -> new EntityNotFoundException("Course not found")
                );
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

        // Only the instructor who owns the course can bulk-enroll learners.
        if (!c.getInstructor().getId().equals(u.getId())) {
            throw new AccessDeniedException(
                    "Only the course instructor can bulk enroll learners"
            );
        }

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
        Set<String> processedEmails = new HashSet<>();

        for (String raw : r.emails()) {

            String email = raw == null
                    ? ""
                    : raw.trim().toLowerCase(Locale.ROOT);

            if (email.isBlank()) {
                continue;
            }

            // Prevent duplicate emails in the same request,
            // including duplicates that differ only by case.
            if (!processedEmails.add(email)) {
                out.add(
                        new BulkResult(
                                email,
                                "Duplicate address"
                        )
                );
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
                continue;
            }

            if (
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
                continue;
            }

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

        return out;
    }
}
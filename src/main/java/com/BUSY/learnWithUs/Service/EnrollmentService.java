package com.BUSY.learnWithUs.Service;

import com.BUSY.learnWithUs.Dto.Auth.UserResponse;
import com.BUSY.learnWithUs.Dto.Course.CourseView;
import com.BUSY.learnWithUs.Dto.Enrollment.EnrollmentView;
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
public class EnrollmentService {
    private final UserRepository users;
    private final CourseRepository courses;
    private final EnrollmentRepository enrollments;

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

    @Transactional
    public EnrollmentView enroll(
            User actor,
            Long cid,
            Long learnerId
    ) {
        instructor(actor);

        Course c = requireCourse(cid);

        if (c.getStatus() != CourseStatus.PUBLISHED) {
            throw new IllegalArgumentException(
                    "Learners can only be enrolled in published courses"
            );
        }

        User learner = users.findById(learnerId)
                .orElseThrow(
                        () -> new EntityNotFoundException("Learner not found")
                );

        if (learner.getRole() != UserRole.LEARNER) {
            throw new IllegalArgumentException(
                    "Selected user is not a learner"
            );
        }

        if (
                enrollments
                        .findByLearnerIdAndCourseId(
                                learnerId,
                                cid
                        )
                        .isPresent()
        ) {
            throw new IllegalArgumentException(
                    "Learner is already enrolled"
            );
        }

        Enrollment e = new Enrollment();

        e.setCourse(c);
        e.setLearner(learner);

        enrollments.save(e);

        return enrollment(e);
    }

    @Transactional
    public EnrollmentView selfEnroll(
            User learner,
            Long cid
    ) {
        Course c = requireCourse(cid);

        if (learner.getRole() != UserRole.LEARNER) {
            throw new AccessDeniedException(
                    "Learner access required"
            );
        }

        if (c.getStatus() != CourseStatus.PUBLISHED) {
            throw new IllegalArgumentException(
                    "Self-enrollment is only available for published courses"
            );
        }

        if (
                enrollments
                        .findByLearnerIdAndCourseId(
                                learner.getId(),
                                cid
                        )
                        .isPresent()
        ) {
            throw new IllegalArgumentException(
                    "Already enrolled"
            );
        }

        Enrollment e = new Enrollment();

        e.setCourse(c);
        e.setLearner(learner);

        enrollments.save(e);

        return enrollment(e);
    }

    public EnrollmentView enrollment(Enrollment e) {
        return new EnrollmentView(
                e.getId(),
                user(e.getLearner()),
                course(e.getCourse()),
                e.getProgressStatus(),
                e.getProgressUpdatedAt(),
                e.getEnrolledAt(),
                e.getCompletedAt()
        );
    }

    public List<EnrollmentView> myEnrollments(User learner) {
        return enrollments
                .findByLearnerIdOrderByEnrolledAtDesc(learner.getId())
                .stream()
                .map(this::enrollment)
                .toList();
    }

    public List<EnrollmentView> courseEnrollments(
            User u,
            Long cid
    ) {
        Course c = requireCourse(cid);

        instructor(u);

        if (!c.getInstructor().getId().equals(u.getId())) {
            throw new AccessDeniedException(
                    "Only the course instructor can view learner progress"
            );
        }

        return enrollments
                .findByCourseIdOrderByEnrolledAtDesc(cid)
                .stream()
                .map(this::enrollment)
                .toList();
    }

    private Course requireCourse(Long id) {
        return courses.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
    }

    private void instructor(User u) {
        if (u.getRole() != UserRole.INSTRUCTOR) {
            throw new AccessDeniedException("Instructor access required");
        }
    }
}

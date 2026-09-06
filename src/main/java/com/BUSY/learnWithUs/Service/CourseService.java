package com.BUSY.learnWithUs.Service;

import com.BUSY.learnWithUs.Dto.Auth.UserResponse;
import com.BUSY.learnWithUs.Dto.Course.CourseRequest;
import com.BUSY.learnWithUs.Dto.Course.CourseView;
import com.BUSY.learnWithUs.Dto.Course.PageResponse;
import com.BUSY.learnWithUs.Entity.*;
import com.BUSY.learnWithUs.Repository.*;
import com.BUSY.learnWithUs.Security.JwtUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CourseService {
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

    public PageResponse<CourseView> listCourses(
            User u,
            String search,
            String category,
            CourseStatus status,
            Long instructorId,
            String sortBy,
            String dir,
            int page,
            int size
    ) {
        Sort sort = Sort.by(
                ("desc".equalsIgnoreCase(dir)
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC),
                switch (sortBy == null ? "createdAt" : sortBy) {
                    case "title" -> "title";
                    default -> "createdAt";
                }
        );

        Pageable pp = PageRequest.of(
                Math.max(0, page),
                Math.min(100, Math.max(1, size))
        );

        String sb = sortBy == null ? "createdAt" : sortBy;
        boolean desc = "desc".equalsIgnoreCase(dir);

        Page<Course> p = switch (sb) {
            case "title" ->
                    desc
                            ? courses.titleDesc(
                            u.getRole().name(),
                            blank(search),
                            blank(category),
                            status,
                            instructorId,
                            pp
                    )
                            : courses.titleAsc(
                            u.getRole().name(),
                            blank(search),
                            blank(category),
                            status,
                            instructorId,
                            pp
                    );

            case "enrollmentCount" ->
                    desc
                            ? courses.enrollmentDesc(
                            u.getRole().name(),
                            blank(search),
                            blank(category),
                            status,
                            instructorId,
                            pp
                    )
                            : courses.enrollmentAsc(
                            u.getRole().name(),
                            blank(search),
                            blank(category),
                            status,
                            instructorId,
                            pp
                    );

            default ->
                    desc
                            ? courses.createdDesc(
                            u.getRole().name(),
                            blank(search),
                            blank(category),
                            status,
                            instructorId,
                            pp
                    )
                            : courses.createdAsc(
                            u.getRole().name(),
                            blank(search),
                            blank(category),
                            status,
                            instructorId,
                            pp
                    );
        };

        return new PageResponse<>(
                p.getContent()
                        .stream()
                        .map(this::course)
                        .toList(),
                p.getNumber(),
                p.getSize(),
                p.getTotalElements()
        );
    }

    private String blank(String s) {
        return s == null || s.isBlank() ? null : s.trim();
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

    private void canAccess(User u, Course c) {
        if (u.getRole() == UserRole.INSTRUCTOR) {
            return;
        }

        if (c.getStatus() != CourseStatus.PUBLISHED) {
            throw new AccessDeniedException("Course is not accessible");
        }
    }

    private void log(
            Course c,
            User actor,
            ActivityType type,
            String description
    ) {
        ActivityLog a = new ActivityLog();

        a.setCourse(c);
        a.setActor(actor);
        a.setEventType(type);
        a.setDescription(description);

        logs.save(a);
    }

    @Transactional
    public CourseView create(User u, CourseRequest r) {
        instructor(u);
        validateCourse(r);

        Course c = new Course();

        c.setTitle(r.getTitle().trim());
        c.setDescription(r.getDescription());
        c.setCategory(r.getCategory().trim());
        c.setInstructor(u);
        c.setStatus(CourseStatus.DRAFT);
        c.setUpdatedAt(LocalDateTime.now());

        courses.save(c);

        log(
                c,
                u,
                ActivityType.COURSE_CREATED,
                "Course created"
        );

        return course(c);
    }

    @Transactional
    public CourseView update(
            User u,
            Long id,
            CourseRequest r
    ) {
        instructor(u);

        Course c = requireCourse(id);

        if (!c.getInstructor().getId().equals(u.getId())) {
            throw new AccessDeniedException(
                    "Only the course instructor can edit it"
            );
        }

        validateCourse(r);

        c.setTitle(r.getTitle().trim());
        c.setDescription(r.getDescription());
        c.setCategory(r.getCategory().trim());
        c.setUpdatedAt(LocalDateTime.now());

        courses.save(c);

        log(
                c,
                u,
                ActivityType.COURSE_EDITED,
                "Course details edited"
        );

        return course(c);
    }

    private void validateCourse(CourseRequest r) {
        if (r == null || r.getTitle() == null || r.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }

        if (r.getDescription() == null || r.getDescription().isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }

        if (r.getCategory() == null || r.getCategory().isBlank()) {
            throw new IllegalArgumentException("Category is required");
        }
    }

    @Transactional
    public CourseView transition(
            User u,
            Long id,
            CourseStatus target
    ) {
        instructor(u);

        Course c = requireCourse(id);

        if (!c.getInstructor().getId().equals(u.getId())) {
            throw new AccessDeniedException(
                    "Only the course instructor can change its lifecycle"
            );
        }

        if (target == CourseStatus.PUBLISHED) {

            if (c.getStatus() != CourseStatus.DRAFT) {
                throw new IllegalArgumentException(
                        "Only Draft courses can be published"
                );
            }

            if (lessons.countByCourseId(id) == 0) {
                throw new IllegalArgumentException(
                        "Cannot publish this course. At least one lesson is required."
                );
            }

            c.setStatus(target);

            log(
                    c,
                    u,
                    ActivityType.COURSE_PUBLISHED,
                    "Course published"
            );

        } else if (target == CourseStatus.ARCHIVED) {

            if (c.getStatus() != CourseStatus.PUBLISHED) {
                throw new IllegalArgumentException(
                        "Only Published courses can be archived"
                );
            }

            c.setStatus(target);

            log(
                    c,
                    u,
                    ActivityType.COURSE_ARCHIVED,
                    "Course archived"
            );

        } else if (
                target == CourseStatus.DRAFT
                        && c.getStatus() == CourseStatus.ARCHIVED
        ) {

            c.setStatus(CourseStatus.DRAFT);


        } else {
            throw new IllegalArgumentException(
                    "Invalid course lifecycle transition"
            );
        }

        c.setUpdatedAt(LocalDateTime.now());

        courses.save(c);

        return course(c);
    }

    public CourseView getCourse(
            User u,
            Long id
    ) {
        Course c = requireCourse(id);

        canAccess(u, c);

        return course(c);
    }
}

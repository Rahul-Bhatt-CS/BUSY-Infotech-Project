package com.BUSY.learnWithUs.Service;

import com.BUSY.learnWithUs.Dto.Lesson.LessonRequest;
import com.BUSY.learnWithUs.Dto.Lesson.LessonView;
import com.BUSY.learnWithUs.Entity.Course;
import com.BUSY.learnWithUs.Entity.Lesson;
import com.BUSY.learnWithUs.Entity.User;
import com.BUSY.learnWithUs.Repository.*;
import com.BUSY.learnWithUs.Security.JwtUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonService {
    private final UserRepository users;
    private final CourseRepository courses;
    private final LessonRepository lessons;
    private final EnrollmentRepository enrollments;
    private final ActivityLogRepository logs;
    private final InactivityAlertRepository alerts;
    private final PasswordEncoder encoder;
    private final JwtUtils jwt;

    public LessonView lesson(Lesson l) {
        return new LessonView(
                l.getId(),
                l.getTitle(),
                l.getContent(),
                l.getPosition()
        );
    }

    @Transactional
    public LessonView addLesson(
            User u,
            Long courseId,
            LessonRequest r
    ) {
        Course c = requireCourse(courseId);

        assertOwner(u, c);
        validateLesson(r);

        Lesson l = new Lesson();

        l.setCourse(c);
        l.setTitle(r.title().trim());
        l.content = r.content();
        l.position = r.position() == null
                ? (int) lessons.countByCourseId(courseId) + 1
                : r.position();

        if (l.position < 1) {
            throw new IllegalArgumentException(
                    "Position must be positive"
            );
        }

        shiftForInsert(
                courseId,
                l.position,
                null
        );

        lessons.save(l);

        return lesson(l);
    }

    @Transactional
    public LessonView updateLesson(
            User u,
            Long id,
            LessonRequest r
    ) {
        Lesson l = lessons.findById(id)
                .orElseThrow(
                        () -> new EntityNotFoundException("Lesson not found")
                );

        assertOwner(u, l.course);
        validateLesson(r);

        int old = l.position;
        int np = r.position() == null
                ? old
                : r.position();

        if (np < 1) {
            throw new IllegalArgumentException(
                    "Position must be positive"
            );
        }

        if (np != old) {
            List<Lesson> ls =
                    lessons.findByCourseIdOrderByPositionAsc(
                            l.course.id
                    );

            if (np > ls.size()) {
                np = ls.size();
            }

            for (Lesson x : ls) {
                if (
                        !x.id.equals(id)
                                && x.position >= Math.min(old, np)
                                && x.position <= Math.max(old, np)
                ) {
                    x.position += old > np ? 1 : -1;
                }
            }
        }

        l.title = r.title().trim();
        l.content = r.content();
        l.position = np;
        l.updatedAt = Instant.now();

        lessons.save(l);

        return lesson(l);
    }

    private void validateLesson(LessonRequest r) {
        if (
                r == null
                        || r.title() == null
                        || r.title().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Lesson title is required"
            );
        }

        if (
                r.content() == null
                        || r.content().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Lesson content is required"
            );
        }
    }

    private void assertOwner(User u, Course c) {
        instructor(u);

        if (!c.instructor.id.equals(u.id)) {
            throw new AccessDeniedException(
                    "Only the course instructor can manage lessons"
            );
        }
    }

    private void shiftForInsert(
            Long cid,
            int pos,
            Long ignore
    ) {
        List<Lesson> ls =
                lessons.findByCourseIdOrderByPositionAsc(cid);

        for (Lesson x : ls) {
            if (
                    !Objects.equals(x.id, ignore)
                            && x.position >= pos
            ) {
                x.position++;
            }
        }
    }

    @Transactional
    public void deleteLesson(
            User u,
            Long id
    ) {
        Lesson l = lessons.findById(id)
                .orElseThrow(
                        () -> new EntityNotFoundException("Lesson not found")
                );

        assertOwner(u, l.course);

        int old = l.position;

        lessons.delete(l);

        for (
                Lesson x :
                lessons.findByCourseIdOrderByPositionAsc(
                        l.course.id
                )
        ) {
            if (x.position > old) {
                x.position--;
            }
        }
    }

    @Transactional
    public void reorder(
            User u,
            Long cid,
            List<Long> ids
    ) {
        Course c = requireCourse(cid);

        assertOwner(u, c);

        List<Lesson> ls =
                lessons.findByCourseIdOrderByPositionAsc(cid);

        if (
                ls.size() != ids.size()
                        || new HashSet<>(ids).size() != ls.size()
                        || !ls.stream()
                        .map(x -> x.id)
                        .collect(Collectors.toSet())
                        .equals(new HashSet<>(ids))
        ) {
            throw new IllegalArgumentException(
                    "Reorder list must contain every lesson exactly once"
            );
        }

        Map<Long, Lesson> m =
                ls.stream()
                        .collect(
                                Collectors.toMap(
                                        x -> x.id,
                                        x -> x
                                )
                        );

        for (int i = 0; i < ids.size(); i++) {
            m.get(ids.get(i)).position = i + 1;
        }

        lessons.saveAll(ls);
    }

    public List<LessonView> getLessons(
            User u,
            Long cid
    ) {
        Course c = requireCourse(cid);

        canAccess(u, c);

        return lessons
                .findByCourseIdOrderByPositionAsc(cid)
                .stream()
                .map(this::lesson)
                .toList();
    }

    private Course requireCourse(Long id) {
        return courses.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
    }

    private void instructor(User u) {
        if (u.role != Role.INSTRUCTOR) {
            throw new AccessDeniedException("Instructor access required");
        }
    }

    private void canAccess(User u, Course c) {
        if (u.role == Role.INSTRUCTOR) {
            return;
        }

        if (c.status != CourseStatus.PUBLISHED) {
            throw new AccessDeniedException("Course is not accessible");
        }
    }
}

package com.BUSY.learnWithUs.Service;

import com.BUSY.learnWithUs.Dto.Lesson.LessonRequest;
import com.BUSY.learnWithUs.Dto.Lesson.LessonView;
import com.BUSY.learnWithUs.Entity.*;
import com.BUSY.learnWithUs.Repository.*;
import com.BUSY.learnWithUs.Security.JwtUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
        l.setContent(r.content());
        if(r.position() == null){
            l.setPosition((int) lessons.countByCourseId(courseId) + 1);
        }else{
            l.setPosition(r.position());
        }

//        l.position = r.position() == null
//                ? (int) lessons.countByCourseId(courseId) + 1
//                : r.position();

        if (l.getPosition() < 1) {
            throw new IllegalArgumentException(
                    "Position must be positive"
            );
        }

        shiftForInsert(
                courseId,
                l.getPosition(),
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

        assertOwner(u, l.getCourse());
        validateLesson(r);

        int old = l.getPosition();
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
                            l.getCourse().getId()
                    );

            if (np > ls.size()) {
                np = ls.size();
            }

            for (Lesson x : ls) {
                if (
                        !x.getId().equals(id)
                                && x.getPosition() >= Math.min(old, np)
                                && x.getPosition() <= Math.max(old, np)
                ) {
                    x.setPosition(x.getPosition() + old > np ? 1 : -1);
                }
            }
        }

        l.setTitle(r.title().trim());
        l.setContent(r.content());
        l.setPosition(np);
        l.setUpdatedAt(LocalDateTime.now());

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

        if (!c.getInstructor().getId().equals(u.getId())) {
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
                    !Objects.equals(x.getId(), ignore)
                            && x.getPosition() >= pos
            ) {
                x.setPosition(x.getPosition() + 1);
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

        assertOwner(u, l.getCourse());

        int old = l.getPosition();

        lessons.delete(l);

        for (
                Lesson x :
                lessons.findByCourseIdOrderByPositionAsc(
                        l.getCourse().getId()
                )
        ) {
            if (x.getPosition() > old) {
                x.setPosition(x.getPosition() - 1);
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
                        .map(x -> x.getId())
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
                                        x -> x.getId(),
                                        x -> x
                                )
                        );

        for (int i = 0; i < ids.size(); i++) {
            m.get(ids.get(i)).setPosition(i+1);
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
}

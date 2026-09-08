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
    private final CourseRepository courses;
    private final LessonRepository lessons;

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

        List<Lesson> lessonList =
                lessons.findByCourseIdOrderByPositionAsc(courseId);

        int position = r.position() == null
                ? lessonList.size() + 1
                : r.position();

        if (position < 1) {
            throw new IllegalArgumentException(
                    "Position must be positive"
            );
        }

        if (position > lessonList.size() + 1) {
            position = lessonList.size() + 1;
        }

        /*
         * Temporarily move existing lessons.
         */
        for (int i = 0; i < lessonList.size(); i++) {
            lessonList.get(i).setPosition(-(i + 1));
        }

        lessons.saveAll(lessonList);

        /*
         * Make sure temporary values reach MySQL.
         */
        lessons.flush();

        /*
         * Rebuild the positions including the new lesson.
         */
        Lesson newLesson = new Lesson();

        newLesson.setCourse(c);
        newLesson.setTitle(r.title().trim());
        newLesson.setContent(r.content());
        newLesson.setPosition(position);

        lessonList.add(position - 1, newLesson);

        for (int i = 0; i < lessonList.size(); i++) {
            lessonList.get(i).setPosition(i + 1);
        }

        lessons.saveAll(lessonList);

        lessons.flush();

        return lesson(newLesson);
    }

    @Transactional
    public LessonView updateLesson(
            User u,
            Long id,
            LessonRequest r
    ) {
        Lesson lesson = lessons.findById(id)
                .orElseThrow(
                        () -> new EntityNotFoundException("Lesson not found")
                );

        assertOwner(u, lesson.getCourse());
        validateLesson(r);

        Long courseId = lesson.getCourse().getId();

        int oldPosition = lesson.getPosition();

        int newPosition = r.position() == null
                ? oldPosition
                : r.position();

        if (newPosition < 1) {
            throw new IllegalArgumentException(
                    "Position must be positive"
            );
        }

        List<Lesson> lessonList =
                lessons.findByCourseIdOrderByPositionAsc(courseId);

        if (newPosition > lessonList.size()) {
            newPosition = lessonList.size();
        }

        /*
         * No position change.
         * Only update the lesson's content.
         */
        if (newPosition == oldPosition) {
            lesson.setTitle(r.title().trim());
            lesson.setContent(r.content());
            lesson.setUpdatedAt(LocalDateTime.now());

            return lesson(lessons.save(lesson));
        }

        /*
         * Remove the lesson from the current ordering.
         */
        lessonList.removeIf(
                x -> x.getId().equals(id)
        );

        /*
         * Insert it into the requested position.
         *
         * List index is zero-based,
         * lesson position is one-based.
         */
        lessonList.add(newPosition - 1, lesson);

        /*
         * STEP 1:
         * Give every lesson a temporary negative position.
         *
         * This makes every position unique.
         */
        for (int i = 0; i < lessonList.size(); i++) {
            lessonList.get(i).setPosition(-(i + 1));
        }

        lessons.saveAll(lessonList);

        /*
         * VERY IMPORTANT:
         * Force Hibernate to execute the temporary UPDATEs now.
         */
        lessons.flush();

        /*
         * STEP 2:
         * Assign the final positions 1..N.
         */
        for (int i = 0; i < lessonList.size(); i++) {
            Lesson current = lessonList.get(i);

            current.setPosition(i + 1);
        }

        /*
         * Update the lesson itself.
         */
        lesson.setTitle(r.title().trim());
        lesson.setContent(r.content());
        lesson.setUpdatedAt(LocalDateTime.now());

        lessons.saveAll(lessonList);

        /*
         * Force the final UPDATEs.
         */
        lessons.flush();

        return lesson(lesson);
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


    @Transactional
    public void deleteLesson(
            User u,
            Long id
    ) {
        Lesson lesson = lessons.findById(id)
                .orElseThrow(
                        () -> new EntityNotFoundException("Lesson not found")
                );

        assertOwner(u, lesson.getCourse());

        Long courseId = lesson.getCourse().getId();

        List<Lesson> lessonList =
                lessons.findByCourseIdOrderByPositionAsc(courseId);

        lessonList.removeIf(
                x -> x.getId().equals(id)
        );

        /*
         * Temporarily clear all positions.
         */
        for (int i = 0; i < lessonList.size(); i++) {
            lessonList.get(i).setPosition(-(i + 1));
        }

        lessons.saveAll(lessonList);

        lessons.flush();

        /*
         * Rebuild positions.
         */
        for (int i = 0; i < lessonList.size(); i++) {
            lessonList.get(i).setPosition(i + 1);
        }

        lessons.saveAll(lessonList);

        /*
         * Delete after the remaining lessons have safe positions.
         */
        lessons.delete(lesson);

        lessons.flush();
    }

    @Transactional
    public void reorder(
            User u,
            Long cid,
            List<Long> ids
    ) {
        Course c = requireCourse(cid);

        assertOwner(u, c);

        List<Lesson> lessonList =
                lessons.findByCourseIdOrderByPositionAsc(cid);

        if (
                ids == null
                        || lessonList.size() != ids.size()
                        || new HashSet<>(ids).size() != ids.size()
                        || !lessonList.stream()
                        .map(Lesson::getId)
                        .collect(Collectors.toSet())
                        .equals(new HashSet<>(ids))
        ) {
            throw new IllegalArgumentException(
                    "Reorder list must contain every lesson exactly once"
            );
        }

        Map<Long, Lesson> lessonMap =
                lessonList.stream()
                        .collect(
                                Collectors.toMap(
                                        Lesson::getId,
                                        lesson -> lesson
                                )
                        );

        /*
         * STEP 1:
         * Temporarily move every lesson away from
         * the real positions.
         */
        for (int i = 0; i < lessonList.size(); i++) {
            lessonList.get(i).setPosition(-(i + 1));
        }

        lessons.saveAll(lessonList);

        /*
         * Force temporary positions into the database.
         */
        lessons.flush();

        /*
         * STEP 2:
         * Apply requested order.
         */
        for (int i = 0; i < ids.size(); i++) {
            lessonMap
                    .get(ids.get(i))
                    .setPosition(i + 1);
        }

        lessons.saveAll(lessonList);

        /*
         * Force final positions into the database.
         */
        lessons.flush();
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

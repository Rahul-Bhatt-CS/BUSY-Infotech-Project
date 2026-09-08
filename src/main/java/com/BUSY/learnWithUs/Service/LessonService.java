package com.BUSY.learnWithUs.Service;

import com.BUSY.learnWithUs.Dto.Lesson.LessonView;
import com.BUSY.learnWithUs.Entity.*;
import com.BUSY.learnWithUs.Repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final CourseRepository courses;
    private final LessonRepository lessons;
    private final FileStorageService fileStorageService;

    public LessonView lesson(Lesson l) {
        return new LessonView(
                l.getId(),
                l.getTitle(),
                l.getContent(), // Relative file location.
                l.getPosition()
        );
    }

    @Transactional
    public LessonView addLesson(
            User u,
            Long courseId,
            String title,
            Integer requestedPosition,
            MultipartFile file
    ) {
        Course c = requireCourse(courseId);

        assertOwner(u, c);
        validateLesson(title, file, true);

        String storedPath = fileStorageService.store(file);

        try {
            List<Lesson> lessonList =
                    lessons.findByCourseIdOrderByPositionAsc(courseId);

            int position = requestedPosition == null
                    ? lessonList.size() + 1
                    : requestedPosition;

            if (position < 1) {
                throw new IllegalArgumentException(
                        "Position must be positive"
                );
            }

            if (position > lessonList.size() + 1) {
                position = lessonList.size() + 1;
            }

            /*
             * Temporarily move existing lessons so the unique
             * (course_id, position) constraint cannot collide.
             */
            for (int i = 0; i < lessonList.size(); i++) {
                lessonList.get(i).setPosition(-(i + 1));
            }

            lessons.saveAll(lessonList);
            lessons.flush();

            Lesson newLesson = new Lesson();

            newLesson.setCourse(c);
            newLesson.setTitle(title.trim());
            newLesson.setContent(storedPath);
            newLesson.setPosition(position);

            lessonList.add(position - 1, newLesson);

            for (int i = 0; i < lessonList.size(); i++) {
                lessonList.get(i).setPosition(i + 1);
            }

            lessons.saveAll(lessonList);
            lessons.flush();

            return lesson(newLesson);

        } catch (RuntimeException e) {
            fileStorageService.delete(storedPath);
            throw e;
        }
    }

    @Transactional
    public LessonView updateLesson(
            User u,
            Long id,
            String title,
            Integer requestedPosition,
            MultipartFile file
    ) {
        Lesson lesson = lessons.findById(id)
                .orElseThrow(
                        () -> new EntityNotFoundException("Lesson not found")
                );

        assertOwner(u, lesson.getCourse());

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Lesson title is required"
            );
        }

        if (file != null && !file.isEmpty()) {
            validateLesson(title, file, false);
        }

        String oldPath = lesson.getContent();
        String newPath = oldPath;

        if (file != null && !file.isEmpty()) {
            newPath = fileStorageService.store(file);
        }

        try {
            Long courseId = lesson.getCourse().getId();

            int oldPosition = lesson.getPosition();

            int newPosition = requestedPosition == null
                    ? oldPosition
                    : requestedPosition;

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
             */
            if (newPosition == oldPosition) {
                lesson.setTitle(title.trim());
                lesson.setContent(newPath);
                lesson.setUpdatedAt(LocalDateTime.now());

                Lesson saved = lessons.save(lesson);

                if (!newPath.equals(oldPath)) {
                    fileStorageService.delete(oldPath);
                }

                return lesson(saved);
            }

            lessonList.removeIf(
                    x -> x.getId().equals(id)
            );

            lessonList.add(newPosition - 1, lesson);

            /*
             * Temporarily move every lesson to a unique negative
             * position before assigning the final ordering.
             */
            for (int i = 0; i < lessonList.size(); i++) {
                lessonList.get(i).setPosition(-(i + 1));
            }

            lessons.saveAll(lessonList);
            lessons.flush();

            for (int i = 0; i < lessonList.size(); i++) {
                lessonList.get(i).setPosition(i + 1);
            }

            lesson.setTitle(title.trim());
            lesson.setContent(newPath);
            lesson.setUpdatedAt(LocalDateTime.now());

            lessons.saveAll(lessonList);
            lessons.flush();

            if (!newPath.equals(oldPath)) {
                fileStorageService.delete(oldPath);
            }

            return lesson(lesson);

        } catch (RuntimeException e) {
            if (!newPath.equals(oldPath)) {
                fileStorageService.delete(newPath);
            }
            throw e;
        }
    }

    private void validateLesson(
            String title,
            MultipartFile file,
            boolean requireFile
    ) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Lesson title is required"
            );
        }

        if (requireFile && (file == null || file.isEmpty())) {
            throw new IllegalArgumentException(
                    "A PDF, PPT, or PPTX lesson file is required"
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
        String filePath = lesson.getContent();

        List<Lesson> lessonList =
                lessons.findByCourseIdOrderByPositionAsc(courseId);

        lessonList.removeIf(
                x -> x.getId().equals(id)
        );

        for (int i = 0; i < lessonList.size(); i++) {
            lessonList.get(i).setPosition(-(i + 1));
        }

        lessons.saveAll(lessonList);
        lessons.flush();

        for (int i = 0; i < lessonList.size(); i++) {
            lessonList.get(i).setPosition(i + 1);
        }

        lessons.saveAll(lessonList);

        lessons.delete(lesson);
        lessons.flush();

        fileStorageService.delete(filePath);
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

        for (int i = 0; i < lessonList.size(); i++) {
            lessonList.get(i).setPosition(-(i + 1));
        }

        lessons.saveAll(lessonList);
        lessons.flush();

        for (int i = 0; i < ids.size(); i++) {
            lessonMap.get(ids.get(i)).setPosition(i + 1);
        }

        lessons.saveAll(lessonList);
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

    public Path getLessonFilePath(
            User u,
            Long lessonId
    ) {
        Lesson lesson = lessons.findById(lessonId)
                .orElseThrow(
                        () -> new EntityNotFoundException("Lesson not found")
                );

        canAccess(u, lesson.getCourse());

        return fileStorageService.load(lesson.getContent());
    }

    private Course requireCourse(Long id) {
        return courses.findById(id)
                .orElseThrow(
                        () -> new EntityNotFoundException("Course not found")
                );
    }

    private void instructor(User u) {
        if (u.getRole() != UserRole.INSTRUCTOR) {
            throw new AccessDeniedException(
                    "Instructor access required"
            );
        }
    }

    private void canAccess(User u, Course c) {
        if (u.getRole() == UserRole.INSTRUCTOR) {
            return;
        }

        if (c.getStatus() != CourseStatus.PUBLISHED) {
            throw new AccessDeniedException(
                    "Course is not accessible"
            );
        }
    }
}

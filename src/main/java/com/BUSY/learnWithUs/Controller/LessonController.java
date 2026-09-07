package com.BUSY.learnWithUs.Controller;

import com.BUSY.learnWithUs.Dto.Lesson.LessonRequest;
import com.BUSY.learnWithUs.Dto.Lesson.LessonView;
import com.BUSY.learnWithUs.Dto.Lesson.ReorderLessonsRequest;
import com.BUSY.learnWithUs.Entity.User;
import com.BUSY.learnWithUs.Service.AuthService;
import com.BUSY.learnWithUs.Service.LessonService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class LessonController {

    private final AuthService authService;
    private final LessonService lessonService;

    private User me() {
        return authService.current(
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName()
        );
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'LEARNER')")
    @GetMapping("/courses/{id}/lessons")
    public List<LessonView> lessons(@PathVariable Long id) {
        return lessonService.getLessons(me(), id);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/courses/{id}/lessons")
    public LessonView addLesson(
            @PathVariable Long id,
            @RequestBody LessonRequest r
    ) {
        return lessonService.addLesson(me(), id, r);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PutMapping("/lessons/{id}")
    public LessonView updateLesson(
            @PathVariable Long id,
            @RequestBody LessonRequest r
    ) {
        return lessonService.updateLesson(me(), id, r);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @DeleteMapping("/lessons/{id}")
    public ResponseEntity<Void> deleteLesson(
            @PathVariable Long id
    ) {
        lessonService.deleteLesson(me(), id);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PutMapping("/courses/{id}/lessons/reorder")
    public ResponseEntity<Void> reorder(
            @PathVariable Long id,
            @RequestBody ReorderLessonsRequest request
    ) {
        lessonService.reorder(me(), id, request.lessonIds());

        return ResponseEntity.noContent().build();
    }

}

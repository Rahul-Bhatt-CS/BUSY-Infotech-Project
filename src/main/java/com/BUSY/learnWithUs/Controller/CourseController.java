package com.BUSY.learnWithUs.Controller;

import com.BUSY.learnWithUs.Dto.Course.CourseRequest;
import com.BUSY.learnWithUs.Dto.Course.CourseView;
import com.BUSY.learnWithUs.Dto.Course.PageResponse;
import com.BUSY.learnWithUs.Entity.CourseStatus;
import com.BUSY.learnWithUs.Entity.User;
import com.BUSY.learnWithUs.Service.AuthService;
import com.BUSY.learnWithUs.Service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CourseController {
    private final CourseService courseService;
    private final AuthService authService;

    private User me() {
        return authService.current(
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName()
        );
    }

    @GetMapping("/courses")
    public PageResponse<CourseView> courses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(required = false) Long instructorId,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return courseService.listCourses(
                me(),
                search,
                category,
                status,
                instructorId,
                sortBy,
                sortDirection,
                page,
                size
        );
    }

    @GetMapping("/courses/{id}")
    public CourseView course(@PathVariable Long id) {
        return courseService.getCourse(me(), id);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/courses")
    public CourseView create(@RequestBody CourseRequest r) {
        return courseService.create(me(), r);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PutMapping("/courses/{id}")
    public CourseView update(
            @PathVariable Long id,
            @RequestBody CourseRequest r
    ) {
        return courseService.update(me(), id, r);
    }

    // ==================== Course Lifecycle ====================

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/courses/{id}/publish")
    public CourseView publish(@PathVariable Long id) {
        return courseService.transition(me(), id, CourseStatus.PUBLISHED);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/courses/{id}/archive")
    public CourseView archive(@PathVariable Long id) {
        return courseService.transition(me(), id, CourseStatus.ARCHIVED);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/courses/{id}/restore")
    public CourseView restore(@PathVariable Long id) {
        return courseService.transition(me(), id, CourseStatus.DRAFT);
    }


}

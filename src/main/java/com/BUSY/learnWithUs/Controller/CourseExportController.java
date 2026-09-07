package com.BUSY.learnWithUs.Controller;

import com.BUSY.learnWithUs.Entity.User;
import com.BUSY.learnWithUs.Service.AuthService;
import com.BUSY.learnWithUs.Service.CourseExportService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class CourseExportController {

    private final CourseExportService courseExportService;
    private final AuthService authService;


    private User me() {
        return authService.current(
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName()
        );
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping(
            value = "/courses/{id}/progress/export",
            produces = "text/csv"
    )
    public ResponseEntity<String> export(@PathVariable Long id) {
        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=course-" + id + "-progress.csv"
                )
                .body(courseExportService.csv(me(), id));
    }
}

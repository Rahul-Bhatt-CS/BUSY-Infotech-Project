package com.BUSY.learnWithUs.Controller;

import com.BUSY.learnWithUs.Dto.Auth.UserResponse;
import com.BUSY.learnWithUs.Dto.Enrollment.EnrollmentRequest;
import com.BUSY.learnWithUs.Dto.Enrollment.EnrollmentView;
import com.BUSY.learnWithUs.Entity.User;
import com.BUSY.learnWithUs.Entity.UserRole;
import com.BUSY.learnWithUs.Service.AuthService;
import com.BUSY.learnWithUs.Service.EnrollmentService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final AuthService authService;

    private User me() {
        return authService.current(
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName()
        );
    }

    @PostMapping("/courses/{id}/enrollments")
    public EnrollmentView enroll(
            @PathVariable Long id,
            @RequestBody EnrollmentRequest request
    ) {
        return enrollmentService.enroll(me(), id, request.learnerId());
    }

    @PostMapping("/courses/{id}/enroll")
    public EnrollmentView selfEnroll(@PathVariable Long id) {
        return enrollmentService.selfEnroll(me(), id);
    }

    @GetMapping("/enrollments/my")
    public List<EnrollmentView> myEnrollments() {
        return enrollmentService.myEnrollments(me());
    }

    @GetMapping("/users/learners")
    public List<UserResponse> learners() {
        if (me().getRole() != UserRole.INSTRUCTOR) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Instructor access required"
            );
        }

        return enrollmentService.learners()
                .stream()
                .map(enrollmentService::user)
                .toList();
    }

    @GetMapping("/courses/{id}/enrollments")
    public List<EnrollmentView> courseEnrollments(
            @PathVariable Long id
    ) {
        return enrollmentService.courseEnrollments(me(), id);
    }

}

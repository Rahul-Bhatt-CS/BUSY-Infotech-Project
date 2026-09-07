package com.BUSY.learnWithUs.Controller;

import com.BUSY.learnWithUs.Dto.Bulk.BulkEnrollmentRequest;
import com.BUSY.learnWithUs.Dto.Bulk.BulkResult;
import com.BUSY.learnWithUs.Entity.User;
import com.BUSY.learnWithUs.Service.AuthService;
import com.BUSY.learnWithUs.Service.BulkEnrollmentService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class BulkEnrollmentController {

    private final BulkEnrollmentService bulkEnrollmentService;
    private final AuthService authService;

    private User me() {
        return authService.current(
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName()
        );
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/courses/{id}/enrollments/bulk")
    public List<BulkResult> bulk(
            @PathVariable Long id,
            @RequestBody BulkEnrollmentRequest r
    ) {
        return bulkEnrollmentService.bulk(me(), id, r);
    }

}

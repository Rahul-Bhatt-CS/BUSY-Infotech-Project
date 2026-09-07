package com.BUSY.learnWithUs.Controller;

import com.BUSY.learnWithUs.Dto.Progress.ProgressRequest;
import com.BUSY.learnWithUs.Dto.Progress.ProgressView;
import com.BUSY.learnWithUs.Entity.ProgressStatus;
import com.BUSY.learnWithUs.Entity.User;
import com.BUSY.learnWithUs.Service.AuthService;
import com.BUSY.learnWithUs.Service.ProgressService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ProgressController{

    private final ProgressService progressService;
    private final AuthService authService;

    private User me() {
        return authService.current(
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName()
        );
    }

    @GetMapping("/enrollments/{id}/progress")
    public ProgressView progress(@PathVariable Long id) {
        return progressService.ownProgress(me(), id);
    }

    @PostMapping("/enrollments/{id}/progress")
    public ProgressView progress(
            @PathVariable Long id,
            @RequestBody ProgressRequest request
    ) {
        return progressService.progress(me(), id, request.status());
    }

}

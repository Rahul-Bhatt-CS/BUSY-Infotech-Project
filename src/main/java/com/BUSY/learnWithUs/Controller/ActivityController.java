package com.BUSY.learnWithUs.Controller;

import com.BUSY.learnWithUs.Dto.Activity.ActivityView;
import com.BUSY.learnWithUs.Dto.Activity.CommentRequest;
import com.BUSY.learnWithUs.Entity.User;
import com.BUSY.learnWithUs.Service.ActivityService;
import com.BUSY.learnWithUs.Service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ActivityController {

    private final ActivityService activityService;
    private final AuthService authService;

    private User me() {
        return authService.current(
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName()
        );
    }

    @GetMapping("/courses/{id}/activity")
    public List<ActivityView> activity(@PathVariable Long id) {
        return activityService.activities(me(), id);
    }

    @PostMapping("/courses/{id}/activity/comments")
    public ActivityView comment(
            @PathVariable Long id,
            @RequestBody CommentRequest r
    ) {
        return activityService.comment(me(), id, r);
    }

}

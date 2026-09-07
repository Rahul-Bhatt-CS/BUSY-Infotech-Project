package com.BUSY.learnWithUs.Controller;

import com.BUSY.learnWithUs.Dto.Dashboard.Dashboard;
import com.BUSY.learnWithUs.Entity.User;
import com.BUSY.learnWithUs.Service.AuthService;
import com.BUSY.learnWithUs.Service.DashboardService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final AuthService authService;


    private User me() {
        return authService.current(
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName()
        );
    }


    @GetMapping("/dashboard")
    public Dashboard dashboard() {
        return dashboardService.dashboard(me());
    }
}

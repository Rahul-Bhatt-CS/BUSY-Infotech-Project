package com.BUSY.learnWithUs.Controller;

import com.BUSY.learnWithUs.Dto.Alert.AlertView;
import com.BUSY.learnWithUs.Entity.User;
import com.BUSY.learnWithUs.Service.AuthService;
import com.BUSY.learnWithUs.Service.InactivityAlertService;
import jakarta.annotation.security.DenyAll;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class InactivityAlertController {

    private final InactivityAlertService inactivityAlertService;
    private final AuthService authService;

    private User me() {
        return authService.current(
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName()
        );
    }

    @GetMapping("/alerts")
    public List<AlertView> alerts() {
        return inactivityAlertService.alerts(me());
    }

    @GetMapping("/alerts/count")
    public Map<String, Long> alertCount() {
        return Map.of(
                "count",
                inactivityAlertService.alertCount(me())
        );
    }

    @PostMapping("/alerts/{id}/dismiss")
    public ResponseEntity<Void> dismiss(
            @PathVariable Long id
    ) {
        inactivityAlertService.dismissAlert(me(), id);

        return ResponseEntity.noContent().build();
    }

}

package com.BUSY.learnWithUs.Service;

import com.BUSY.learnWithUs.Dto.Dashboard.Dashboard;
import com.BUSY.learnWithUs.Dto.Dashboard.EnrollmentByCourse;
import com.BUSY.learnWithUs.Dto.Dashboard.WeeklyCompletion;
import com.BUSY.learnWithUs.Entity.CourseStatus;
import com.BUSY.learnWithUs.Entity.ProgressStatus;
import com.BUSY.learnWithUs.Entity.User;
import com.BUSY.learnWithUs.Entity.UserRole;
import com.BUSY.learnWithUs.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository users;
    private final CourseRepository courses;
    private final EnrollmentRepository enrollments;

    private void instructor(User u) {
        if (u.getRole() != UserRole.INSTRUCTOR) {
            throw new AccessDeniedException(
                    "Instructor access required"
            );
        }
    }

    public Dashboard dashboard(User u) {

        instructor(u);

        LocalDateTime now =
                LocalDateTime.now();

        YearMonth ym =
                YearMonth.now();

        LocalDateTime start =
                ym.atDay(1)
                        .atStartOfDay();

        long totalLearners =
                users.countByRole(
                        UserRole.LEARNER
                );

        long published =
                courses.countByStatus(
                        CourseStatus.PUBLISHED
                );

        long completed =
                enrollments
                        .countByProgressStatusAndCompletedAtBetween(
                                ProgressStatus.COMPLETED,
                                start,
                                now
                        );

        long inProgress =
                enrollments.countByProgressStatus(
                        ProgressStatus.IN_PROGRESS
                );

        Map<String, Long> by =
                new LinkedHashMap<>();

        for (ProgressStatus status : ProgressStatus.values()) {
            by.put(
                    status.name(),
                    enrollments.countByProgressStatus(status)
            );
        }

        List<EnrollmentByCourse> byCourse =
                courses.findAll()
                        .stream()
                        .map(
                                c ->
                                        new EnrollmentByCourse(
                                                c.getId(),
                                                c.getTitle(),
                                                enrollments.countByCourseId(
                                                        c.getId()
                                                )
                                        )
                        )
                        .sorted(
                                Comparator.comparingLong(
                                        EnrollmentByCourse::enrollmentCount
                                ).reversed()
                        )
                        .toList();

        List<WeeklyCompletion> weeks =
                new ArrayList<>();

        LocalDate today =
                LocalDate.now();

        for (int i = 7; i >= 0; i--) {

            LocalDate monday =
                    today.minusWeeks(i)
                            .with(DayOfWeek.MONDAY);

            LocalDateTime from =
                    monday.atStartOfDay();

            LocalDateTime to =
                    monday.plusWeeks(1)
                            .atStartOfDay();

            weeks.add(
                    new WeeklyCompletion(
                            monday.toString(),
                            enrollments
                                    .countByProgressStatusAndCompletedAtBetween(
                                            ProgressStatus.COMPLETED,
                                            from,
                                            to
                                    )
                    )
            );
        }

        return new Dashboard(
                totalLearners,
                published,
                completed,
                inProgress,
                by,
                byCourse,
                weeks
        );
    }
}
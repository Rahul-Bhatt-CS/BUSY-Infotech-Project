package com.BUSY.learnWithUs.Service;

import com.BUSY.learnWithUs.Dto.Dashboard.Dashboard;
import com.BUSY.learnWithUs.Dto.Dashboard.EnrollmentByCourse;
import com.BUSY.learnWithUs.Dto.Dashboard.WeeklyCompletion;
import com.BUSY.learnWithUs.Entity.CourseStatus;
import com.BUSY.learnWithUs.Entity.ProgressStatus;
import com.BUSY.learnWithUs.Entity.User;
import com.BUSY.learnWithUs.Entity.UserRole;
import com.BUSY.learnWithUs.Repository.*;
import com.BUSY.learnWithUs.Security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final UserRepository users;
    private final CourseRepository courses;
    private final LessonRepository lessons;
    private final EnrollmentRepository enrollments;
    private final ActivityLogRepository logs;
    private final InactivityAlertRepository alerts;
    private final PasswordEncoder encoder;
    private final JwtUtils jwt;


    private void instructor(User u) {
        if (u.getRole() != UserRole.INSTRUCTOR) {
            throw new AccessDeniedException("Instructor access required");
        }
    }

    public Dashboard dashboard(User u) {
        instructor(u);

        Instant now = Instant.now();

        YearMonth ym =
                YearMonth.now(
                        ZoneOffset.UTC
                );

        Instant start =
                ym.atDay(1)
                        .atStartOfDay(
                                ZoneOffset.UTC
                        )
                        .toInstant();

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

        Map<String, Long> by = new LinkedHashMap<>();

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
                                                enrollments.countByCourseId(c.getId())
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
                LocalDate.now(
                        ZoneOffset.UTC
                );

        for (int i = 7; i >= 0; i--) {

            LocalDate monday =
                    today.minusWeeks(i)
                            .with(
                                    java.time.DayOfWeek.MONDAY
                            );

            Instant from =
                    monday.atStartOfDay(
                            ZoneOffset.UTC
                    ).toInstant();

            Instant to =
                    monday.plusWeeks(1)
                            .atStartOfDay(
                                    ZoneOffset.UTC
                            ).toInstant();

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

package com.BUSY.learnWithUs.Dto.Dashboard;

import java.util.List;
import java.util.Map;

public record Dashboard(
        long totalLearners,
        long publishedCourses,
        long completionsThisMonth,
        long learnersInProgress,
        Map<String, Long> enrollmentsByProgress,
        List<EnrollmentByCourse> enrollmentsByCourse,
        List<WeeklyCompletion> completionsLastEightWeeks
) {
}
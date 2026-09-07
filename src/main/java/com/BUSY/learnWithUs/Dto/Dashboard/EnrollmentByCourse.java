package com.BUSY.learnWithUs.Dto.Dashboard;


public record EnrollmentByCourse(
        Long courseId,
        String courseTitle,
        long enrollmentCount
) {
}
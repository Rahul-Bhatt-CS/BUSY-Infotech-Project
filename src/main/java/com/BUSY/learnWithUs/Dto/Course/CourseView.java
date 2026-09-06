package com.BUSY.learnWithUs.Dto.Course;

import com.BUSY.learnWithUs.Dto.Auth.UserResponse;
import com.BUSY.learnWithUs.Entity.CourseStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class CourseView {

    private Long id;
    private String title;
    private String description;
    private String category;
    private CourseStatus status;
    private UserResponse instructor;
    private long enrollmentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
package com.BUSY.learnWithUs.Dto.Course;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class CourseRequest {
    private String title;
    private String description;
    private String category;
}

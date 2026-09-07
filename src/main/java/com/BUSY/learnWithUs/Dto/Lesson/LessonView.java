package com.BUSY.learnWithUs.Dto.Lesson;


public record LessonView(
        Long id,
        String title,
        String content,
        Integer position
) {
}
package com.BUSY.learnWithUs.Dto.Lesson;


public record LessonRequest(
        String title,
        String content,
        Integer position
) {
}
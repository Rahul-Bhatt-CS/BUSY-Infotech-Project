package com.BUSY.learnWithUs.Dto.Lesson;

import java.util.List;

public record ReorderLessonsRequest (
    List<Long> lessonIds
){
}

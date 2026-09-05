package com.BUSY.learnWithUs.repository;

import com.BUSY.learnWithUs.entity.Lesson;
import com.BUSY.learnWithUs.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByCourseOrderByPositionAsc(Course course);
}

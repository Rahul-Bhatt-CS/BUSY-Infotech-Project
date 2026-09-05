package com.BUSY.learnWithUs.Repository;

import com.BUSY.learnWithUs.Entity.Lesson;
import com.BUSY.learnWithUs.Entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByCourseOrderByPositionAsc(Course course);
}

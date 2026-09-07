package com.BUSY.learnWithUs.Repository;

import com.BUSY.learnWithUs.Entity.Lesson;
import com.BUSY.learnWithUs.Entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByCourseIdOrderByPositionAsc(Long courseId);
    long countByCourseId(Long courseId);
}

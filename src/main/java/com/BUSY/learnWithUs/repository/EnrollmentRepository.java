package com.BUSY.learnWithUs.repository;

import com.BUSY.learnWithUs.entity.Enrollment;
import com.BUSY.learnWithUs.entity.User;
import com.BUSY.learnWithUs.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByLearner(User learner);
    List<Enrollment> findByCourse(Course course);
    Optional<Enrollment> findByLearnerAndCourse(User learner, Course course);
    boolean existsByLearnerAndCourse(User learner, Course course);
}

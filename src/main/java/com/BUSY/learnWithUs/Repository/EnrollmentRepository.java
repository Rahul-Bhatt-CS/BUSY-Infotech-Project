package com.BUSY.learnWithUs.Repository;

import com.BUSY.learnWithUs.Entity.Enrollment;
import com.BUSY.learnWithUs.Entity.User;
import com.BUSY.learnWithUs.Entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByLearner(User learner);
    List<Enrollment> findByCourse(Course course);
    Optional<Enrollment> findByLearnerAndCourse(User learner, Course course);
    boolean existsByLearnerAndCourse(User learner, Course course);
}

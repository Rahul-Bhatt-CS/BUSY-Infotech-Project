package com.BUSY.learnWithUs.Repository;

import com.BUSY.learnWithUs.Entity.Enrollment;
import com.BUSY.learnWithUs.Entity.ProgressStatus;
import com.BUSY.learnWithUs.Entity.User;
import com.BUSY.learnWithUs.Entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByLearner(User learner);
    List<Enrollment> findByCourse(Course course);
    Optional<Enrollment> findByLearnerAndCourse(User learner, Course course);
    boolean existsByLearnerAndCourse(User learner, Course course);
    Optional<Enrollment> findByLearnerIdAndCourseId(Long learnerId,Long courseId);
    List<Enrollment> findByLearnerIdOrderByEnrolledAtDesc(Long learnerId);
    List<Enrollment> findByCourseIdOrderByEnrolledAtDesc(Long courseId);
    long countByProgressStatus(ProgressStatus status);
    long countByCourseId(Long courseId);
    long countByProgressStatusAndCompletedAtBetween(ProgressStatus status, Instant from, Instant to);
    @Query("select e from Enrollment e where e.progressStatus=:status and e.progressUpdatedAt < :cutoff")
    List<Enrollment> findInactive(@Param("status") ProgressStatus status,
                                  @Param("cutoff") Instant cutoff);
}

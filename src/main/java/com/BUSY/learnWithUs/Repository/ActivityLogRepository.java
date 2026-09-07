package com.BUSY.learnWithUs.Repository;

import com.BUSY.learnWithUs.Entity.ActivityLog;
import com.BUSY.learnWithUs.Entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByCourseIdOrderByCreatedAtDesc(Long courseId);
}

package com.BUSY.learnWithUs.repository;

import com.BUSY.learnWithUs.entity.ActivityLog;
import com.BUSY.learnWithUs.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByCourseOrderByCreatedAtDesc(Course course);
}

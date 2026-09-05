package com.BUSY.learnWithUs.Repository;

import com.BUSY.learnWithUs.Entity.ActivityLog;
import com.BUSY.learnWithUs.Entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByCourseOrderByCreatedAtDesc(Course course);
}

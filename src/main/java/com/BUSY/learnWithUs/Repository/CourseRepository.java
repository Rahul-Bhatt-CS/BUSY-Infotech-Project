package com.BUSY.learnWithUs.Repository;

import com.BUSY.learnWithUs.Entity.Course;
import com.BUSY.learnWithUs.Entity.CourseStatus;
import com.BUSY.learnWithUs.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {
    Page<Course> findByStatus(CourseStatus status, Pageable pageable);
    Page<Course> findByInstructor(User instructor, Pageable pageable);
}

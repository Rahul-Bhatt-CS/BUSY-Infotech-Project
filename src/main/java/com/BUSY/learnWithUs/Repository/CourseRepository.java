package com.BUSY.learnWithUs.Repository;

import com.BUSY.learnWithUs.Entity.Course;
import com.BUSY.learnWithUs.Entity.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {

    String FILTER =
            "where (:role = 'INSTRUCTOR' " +
                    "or c.status = com.BUSY.learnWithUs.Entity.CourseStatus.PUBLISHED) " +
                    "and (:search is null " +
                    "or lower(c.title) like lower(concat('%', :search, '%')) " +
                    "or lower(c.description) like lower(concat('%', :search, '%'))) " +
                    "and (:category is null or c.category = :category) " +
                    "and (:status is null or c.status = :status) " +
                    "and (:instructorId is null or c.instructor.id = :instructorId)";

    String COUNT =
            "select count(c) from Course c " +
                    FILTER;

    @Query(
            "select c from Course c " +
                    FILTER +
                    " order by c.title asc"
    )
    Page<Course> titleAsc(
            @Param("role") String role,
            @Param("search") String search,
            @Param("category") String category,
            @Param("status") CourseStatus status,
            @Param("instructorId") Long instructorId,
            Pageable p
    );

    @Query(
            "select c from Course c " +
                    FILTER +
                    " order by c.title desc"
    )
    Page<Course> titleDesc(
            @Param("role") String role,
            @Param("search") String search,
            @Param("category") String category,
            @Param("status") CourseStatus status,
            @Param("instructorId") Long instructorId,
            Pageable p
    );

    @Query(
            "select c from Course c " +
                    FILTER +
                    " order by c.createdAt asc"
    )
    Page<Course> createdAsc(
            @Param("role") String role,
            @Param("search") String search,
            @Param("category") String category,
            @Param("status") CourseStatus status,
            @Param("instructorId") Long instructorId,
            Pageable p
    );

    @Query(
            "select c from Course c " +
                    FILTER +
                    " order by c.createdAt desc"
    )
    Page<Course> createdDesc(
            @Param("role") String role,
            @Param("search") String search,
            @Param("category") String category,
            @Param("status") CourseStatus status,
            @Param("instructorId") Long instructorId,
            Pageable p
    );

    @Query(
            "select c from Course c " +
                    FILTER +
                    " order by size(c.enrollments) asc"
    )
    Page<Course> enrollmentAsc(
            @Param("role") String role,
            @Param("search") String search,
            @Param("category") String category,
            @Param("status") CourseStatus status,
            @Param("instructorId") Long instructorId,
            Pageable p
    );

    @Query(
            "select c from Course c " +
                    FILTER +
                    " order by size(c.enrollments) desc"
    )
    Page<Course> enrollmentDesc(
            @Param("role") String role,
            @Param("search") String search,
            @Param("category") String category,
            @Param("status") CourseStatus status,
            @Param("instructorId") Long instructorId,
            Pageable p
    );

    @Query(COUNT)
    long countMatches(
            @Param("role") String role,
            @Param("search") String search,
            @Param("category") String category,
            @Param("status") CourseStatus status,
            @Param("instructorId") Long instructorId
    );

    @Query("select count(c) from Course c where c.status = :status")
    long countByStatus(@Param("status") CourseStatus status);
}
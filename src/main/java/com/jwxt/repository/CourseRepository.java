package com.jwxt.repository;

import com.jwxt.entity.Course;
import com.jwxt.entity.CourseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTeacherId(Long teacherId);
    List<Course> findBySemester(String semester);
    List<Course> findByCategory(CourseCategory category);
}

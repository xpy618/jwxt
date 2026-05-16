package com.jwxt.repository;

import com.jwxt.entity.CourseSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseSlotRepository extends JpaRepository<CourseSlot, Long> {
    List<CourseSlot> findByCourseId(Long courseId);
    List<CourseSlot> findByCourseIdIn(List<Long> courseIds);
    void deleteByCourseId(Long courseId);
}

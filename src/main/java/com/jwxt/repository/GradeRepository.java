package com.jwxt.repository;

import com.jwxt.entity.Grade;
import com.jwxt.entity.GradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByStudentId(Long studentId);
    List<Grade> findByCourseId(Long courseId);
    Optional<Grade> findByStudentIdAndCourseId(Long studentId, Long courseId);
    List<Grade> findByCourseIdAndStatus(Long courseId, GradeStatus status);

    @Query("SELECT g FROM Grade g JOIN Course c ON g.courseId = c.id WHERE g.studentId = ?1 AND c.semester = ?2")
    List<Grade> findByStudentIdAndSemester(Long studentId, String semester);
}

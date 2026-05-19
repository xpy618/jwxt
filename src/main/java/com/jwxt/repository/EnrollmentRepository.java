package com.jwxt.repository;

import com.jwxt.entity.Enrollment;
import com.jwxt.entity.GradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudentId(Long studentId);
    List<Enrollment> findByCourseId(Long courseId);
    long countByCourseId(Long courseId);
    boolean existsByCourseId(Long courseId);
    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    @Query("SELECT e.courseId, COUNT(e) FROM Enrollment e WHERE e.courseId IN :courseIds GROUP BY e.courseId")
    List<Object[]> countGroupByCourseId(@Param("courseIds") List<Long> courseIds);
    void deleteByStudentIdAndCourseId(Long studentId, Long courseId);

    List<Enrollment> findByCourseIdAndStatus(Long courseId, GradeStatus status);

    @Query("SELECT e FROM Enrollment e JOIN Course c ON e.courseId = c.id WHERE e.studentId = ?1 AND c.semester = ?2")
    List<Enrollment> findByStudentIdAndSemester(Long studentId, String semester);
}

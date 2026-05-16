package com.jwxt.service;

import com.jwxt.common.BusinessException;
import com.jwxt.dto.GradeRequest;
import com.jwxt.dto.GradeVO;
import com.jwxt.entity.*;
import com.jwxt.repository.CourseRepository;
import com.jwxt.repository.GradeRepository;
import com.jwxt.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GradeService {

    private final GradeRepository gradeRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public GradeService(GradeRepository gradeRepository,
                        CourseRepository courseRepository,
                        UserRepository userRepository) {
        this.gradeRepository = gradeRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Grade saveOrUpdate(GradeRequest request) {
        Grade grade = gradeRepository.findByStudentIdAndCourseId(request.getStudentId(), request.getCourseId())
                .orElseGet(() -> {
                    Grade g = new Grade();
                    g.setStudentId(request.getStudentId());
                    g.setCourseId(request.getCourseId());
                    return g;
                });

        if (grade.getStatus() == GradeStatus.PUBLISHED) {
            throw new BusinessException("成绩已发布，无法修改");
        }

        grade.setScore(request.getScore());
        grade.setGpaPoint(calculateGpaPoint(request.getScore()));
        return gradeRepository.save(grade);
    }

    @Transactional
    public void submit(Long courseId) {
        List<Grade> grades = gradeRepository.findByCourseId(courseId);
        for (Grade g : grades) {
            if (g.getStatus() == GradeStatus.DRAFT) {
                g.setStatus(GradeStatus.SUBMITTED);
                g.setSubmittedAt(LocalDateTime.now());
            }
        }
        gradeRepository.saveAll(grades);
    }

    @Transactional
    public void publish(Long courseId) {
        List<Grade> grades = gradeRepository.findByCourseIdAndStatus(courseId, GradeStatus.SUBMITTED);
        if (grades.isEmpty()) {
            throw new BusinessException("没有待发布的成绩");
        }
        for (Grade g : grades) {
            g.setStatus(GradeStatus.PUBLISHED);
            g.setPublishedAt(LocalDateTime.now());
        }
        gradeRepository.saveAll(grades);
    }

    public List<GradeVO> getStudentGrades(Long studentId) {
        List<Grade> grades = gradeRepository.findByStudentId(studentId);
        return grades.stream().map(this::toVO).toList();
    }

    public List<GradeVO> getCourseGrades(Long courseId) {
        List<Grade> grades = gradeRepository.findByCourseId(courseId);
        return grades.stream().map(this::toVO).toList();
    }

    public float calculateGPA(Long studentId, String semester) {
        List<Grade> grades = gradeRepository.findByStudentIdAndSemester(studentId, semester);
        if (grades.isEmpty()) return 0f;

        float totalPoints = 0;
        float totalCredits = 0;
        for (Grade g : grades) {
            if (g.getStatus() == GradeStatus.PUBLISHED && g.getGpaPoint() != null) {
                Course course = courseRepository.findById(g.getCourseId()).orElse(null);
                if (course != null) {
                    totalPoints += g.getGpaPoint() * course.getCredit();
                    totalCredits += course.getCredit();
                }
            }
        }
        return totalCredits > 0 ? totalPoints / totalCredits : 0f;
    }

    private GradeVO toVO(Grade grade) {
        GradeVO vo = new GradeVO();
        vo.setId(grade.getId());
        vo.setStudentId(grade.getStudentId());
        vo.setCourseId(grade.getCourseId());
        vo.setScore(grade.getScore());
        vo.setGpaPoint(grade.getGpaPoint());
        vo.setStatus(grade.getStatus().name());

        User student = userRepository.findById(grade.getStudentId()).orElse(null);
        if (student != null) vo.setStudentName(student.getName());

        Course course = courseRepository.findById(grade.getCourseId()).orElse(null);
        if (course != null) {
            vo.setCourseName(course.getName());
            vo.setCredit(course.getCredit());
        }
        return vo;
    }

    private float calculateGpaPoint(Float score) {
        if (score == null) return 0f;
        if (score >= 90) return 4.0f;
        if (score >= 85) return 3.7f;
        if (score >= 82) return 3.3f;
        if (score >= 78) return 3.0f;
        if (score >= 75) return 2.7f;
        if (score >= 72) return 2.3f;
        if (score >= 68) return 2.0f;
        if (score >= 64) return 1.5f;
        if (score >= 60) return 1.0f;
        return 0f;
    }
}

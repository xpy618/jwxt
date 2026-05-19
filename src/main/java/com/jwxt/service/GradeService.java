package com.jwxt.service;

import com.jwxt.common.BusinessException;
import com.jwxt.dto.GradeRequest;
import com.jwxt.dto.GradeVO;
import com.jwxt.entity.*;
import com.jwxt.repository.CourseRepository;
import com.jwxt.repository.EnrollmentRepository;
import com.jwxt.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GradeService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public GradeService(EnrollmentRepository enrollmentRepository,
                        CourseRepository courseRepository,
                        UserRepository userRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Enrollment saveOrUpdate(GradeRequest request) {
        Enrollment enrollment = enrollmentRepository
                .findByStudentIdAndCourseId(request.getStudentId(), request.getCourseId())
                .orElseThrow(() -> new BusinessException("该学生未选此课程"));

        if (enrollment.getStatus() == GradeStatus.PUBLISHED) {
            throw new BusinessException("成绩已发布，无法修改");
        }

        enrollment.setScore(request.getScore());
        enrollment.setGpaPoint(calculateGpaPoint(request.getScore()));
        enrollment.setStatus(GradeStatus.DRAFT);
        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void publish(Long courseId) {
        List<Enrollment> drafts = enrollmentRepository.findByCourseIdAndStatus(courseId, GradeStatus.DRAFT);
        if (drafts.isEmpty()) {
            throw new BusinessException("没有待发布的成绩");
        }
        for (Enrollment e : drafts) {
            e.setStatus(GradeStatus.PUBLISHED);
            e.setPublishedAt(LocalDateTime.now());
        }
        enrollmentRepository.saveAll(drafts);
    }

    @Transactional
    public void withdraw(Long courseId) {
        List<Enrollment> published = enrollmentRepository.findByCourseIdAndStatus(courseId, GradeStatus.PUBLISHED);
        if (published.isEmpty()) {
            throw new BusinessException("没有可撤回的成绩");
        }
        for (Enrollment e : published) {
            e.setStatus(GradeStatus.DRAFT);
            e.setPublishedAt(null);
        }
        enrollmentRepository.saveAll(published);
    }

    public List<GradeVO> getStudentGrades(Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        return enrollments.stream()
                .filter(e -> e.getStatus() != null)
                .map(this::toVO)
                .toList();
    }

    public List<GradeVO> getCourseGrades(Long courseId) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        Course course = courseRepository.findById(courseId).orElse(null);

        return enrollments.stream()
                .map(e -> e.getStatus() != null ? toVO(e) : toEmptyVO(e, course))
                .toList();
    }

    public float calculateGPA(Long studentId, String semester) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdAndSemester(studentId, semester);
        if (enrollments.isEmpty()) return 0f;

        float totalPoints = 0;
        float totalCredits = 0;
        for (Enrollment e : enrollments) {
            if (e.getStatus() == GradeStatus.PUBLISHED && e.getGpaPoint() != null) {
                Course course = courseRepository.findById(e.getCourseId()).orElse(null);
                if (course != null) {
                    totalPoints += e.getGpaPoint() * course.getCredit();
                    totalCredits += course.getCredit();
                }
            }
        }
        return totalCredits > 0 ? totalPoints / totalCredits : 0f;
    }

    private GradeVO toVO(Enrollment e) {
        GradeVO vo = new GradeVO();
        vo.setId(e.getId());
        vo.setStudentId(e.getStudentId());
        vo.setCourseId(e.getCourseId());
        vo.setScore(e.getScore());
        vo.setGpaPoint(e.getGpaPoint());
        vo.setStatus(e.getStatus().name());

        User student = userRepository.findById(e.getStudentId()).orElse(null);
        if (student != null) vo.setStudentName(student.getName());

        Course course = courseRepository.findById(e.getCourseId()).orElse(null);
        if (course != null) {
            vo.setCourseName(course.getName());
            vo.setCredit(course.getCredit());
        }
        return vo;
    }

    private GradeVO toEmptyVO(Enrollment e, Course course) {
        GradeVO vo = new GradeVO();
        vo.setStudentId(e.getStudentId());
        vo.setCourseId(e.getCourseId());
        User student = userRepository.findById(e.getStudentId()).orElse(null);
        if (student != null) {
            vo.setStudentName(student.getName());
        }
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

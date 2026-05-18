package com.jwxt.controller;

import com.jwxt.common.Result;
import com.jwxt.dto.GradeRequest;
import com.jwxt.dto.GradeVO;
import com.jwxt.entity.Grade;
import com.jwxt.service.GradeService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grades")
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<List<GradeVO>> myGrades(Authentication auth) {
        Long studentId = (Long) auth.getCredentials();
        return Result.success(gradeService.getStudentGrades(studentId));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<List<GradeVO>> studentGrades(@PathVariable Long studentId) {
        return Result.success(gradeService.getStudentGrades(studentId));
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<List<GradeVO>> courseGrades(@PathVariable Long courseId) {
        return Result.success(gradeService.getCourseGrades(courseId));
    }

    @PostMapping("/teacher/save")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<Grade> saveGrade(@Valid @RequestBody GradeRequest request) {
        return Result.success(gradeService.saveOrUpdate(request));
    }

    @PostMapping("/publish/{courseId}")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<Void> publish(@PathVariable Long courseId) {
        gradeService.publish(courseId);
        return Result.success();
    }

    @PostMapping("/withdraw/{courseId}")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<Void> withdraw(@PathVariable Long courseId) {
        gradeService.withdraw(courseId);
        return Result.success();
    }

    @GetMapping("/gpa")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<Float> gpa(Authentication auth, @RequestParam String semester) {
        Long studentId = (Long) auth.getCredentials();
        return Result.success(gradeService.calculateGPA(studentId, semester));
    }
}

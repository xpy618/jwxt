package com.jwxt.controller;

import com.jwxt.common.Result;
import com.jwxt.dto.CourseRequest;
import com.jwxt.dto.CourseVO;
import com.jwxt.entity.Course;
import com.jwxt.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public Result<List<Course>> listAll() {
        return Result.success(courseService.listAll());
    }

    @GetMapping("/with-enrollment")
    public Result<List<CourseVO>> listWithEnrollment(Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        return Result.success(courseService.listWithEnrollmentStatus(userId));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<List<Course>> listMyCourses(Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        return Result.success(courseService.listByTeacher(userId));
    }

    @GetMapping("/{id}")
    public Result<Course> getById(@PathVariable Long id) {
        return Result.success(courseService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<Course> create(@Valid @RequestBody CourseRequest request, Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        return Result.success(courseService.create(userId, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<Course> update(@PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        return Result.success(courseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return Result.success();
    }
}

package com.jwxt.controller;

import com.jwxt.common.Result;
import com.jwxt.dto.EnrollmentPreviewRequest;
import com.jwxt.dto.EnrollmentPreviewVO;
import com.jwxt.dto.ScheduleVO;
import com.jwxt.service.CourseService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
@PreAuthorize("hasRole('STUDENT')")
public class EnrollmentController {

    private final CourseService courseService;

    public EnrollmentController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public Result<Void> enroll(@RequestBody Map<String, Long> body, Authentication auth) {
        Long studentId = (Long) auth.getCredentials();
        courseService.enroll(studentId, body.get("courseId"));
        return Result.success();
    }

    @DeleteMapping("/{courseId}")
    public Result<Void> unenroll(@PathVariable Long courseId, Authentication auth) {
        Long studentId = (Long) auth.getCredentials();
        courseService.unenroll(studentId, courseId);
        return Result.success();
    }

    @PostMapping("/preview")
    public Result<EnrollmentPreviewVO> preview(@RequestBody EnrollmentPreviewRequest request, Authentication auth) {
        Long studentId = (Long) auth.getCredentials();
        return Result.success(courseService.previewEnrollments(studentId, request.getCourseIds()));
    }

    @GetMapping("/schedule")
    public Result<List<ScheduleVO>> schedule(Authentication auth) {
        Long studentId = (Long) auth.getCredentials();
        return Result.success(courseService.getSchedule(studentId));
    }
}

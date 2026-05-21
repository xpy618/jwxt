package com.jwxt.controller;

import com.jwxt.common.Result;
import com.jwxt.dto.AcademicProgressVO;
import com.jwxt.dto.AdminDashboardVO;
import com.jwxt.dto.StudentDashboardVO;
import com.jwxt.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<StudentDashboardVO> studentDashboard(Authentication auth) {
        Long studentId = (Long) auth.getCredentials();
        return Result.success(dashboardService.getStudentDashboard(studentId));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<AdminDashboardVO> adminDashboard() {
        return Result.success(dashboardService.getAdminDashboard());
    }

    @GetMapping("/academic-progress")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<AcademicProgressVO> academicProgress(Authentication auth) {
        Long studentId = (Long) auth.getCredentials();
        return Result.success(dashboardService.getAcademicProgress(studentId));
    }
}

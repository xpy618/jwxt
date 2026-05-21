package com.jwxt.dto;

import java.util.List;

public class StudentDashboardVO {
    private Double selectedCredits;
    private Double creditLimit;
    private Integer selectedCourseCount;
    private List<ScheduleVO> todaySchedules;
    private List<GradeVO> latestGrades;
    private List<String> tips;

    public Double getSelectedCredits() { return selectedCredits; }
    public void setSelectedCredits(Double selectedCredits) { this.selectedCredits = selectedCredits; }
    public Double getCreditLimit() { return creditLimit; }
    public void setCreditLimit(Double creditLimit) { this.creditLimit = creditLimit; }
    public Integer getSelectedCourseCount() { return selectedCourseCount; }
    public void setSelectedCourseCount(Integer selectedCourseCount) { this.selectedCourseCount = selectedCourseCount; }
    public List<ScheduleVO> getTodaySchedules() { return todaySchedules; }
    public void setTodaySchedules(List<ScheduleVO> todaySchedules) { this.todaySchedules = todaySchedules; }
    public List<GradeVO> getLatestGrades() { return latestGrades; }
    public void setLatestGrades(List<GradeVO> latestGrades) { this.latestGrades = latestGrades; }
    public List<String> getTips() { return tips; }
    public void setTips(List<String> tips) { this.tips = tips; }
}

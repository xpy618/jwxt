package com.jwxt.dto;

import java.util.List;

public class AdminDashboardVO {
    private Long studentCount;
    private Long teacherCount;
    private Long adminCount;
    private Long courseCount;
    private Long unassignedCourseCount;
    private Long enrollmentCount;
    private Long publishedGradeCount;
    private Long draftGradeCount;
    private Long missingGradeCount;
    private List<CourseSummaryVO> popularCourses;
    private List<CourseSummaryVO> fullCourses;

    public Long getStudentCount() { return studentCount; }
    public void setStudentCount(Long studentCount) { this.studentCount = studentCount; }
    public Long getTeacherCount() { return teacherCount; }
    public void setTeacherCount(Long teacherCount) { this.teacherCount = teacherCount; }
    public Long getAdminCount() { return adminCount; }
    public void setAdminCount(Long adminCount) { this.adminCount = adminCount; }
    public Long getCourseCount() { return courseCount; }
    public void setCourseCount(Long courseCount) { this.courseCount = courseCount; }
    public Long getUnassignedCourseCount() { return unassignedCourseCount; }
    public void setUnassignedCourseCount(Long unassignedCourseCount) { this.unassignedCourseCount = unassignedCourseCount; }
    public Long getEnrollmentCount() { return enrollmentCount; }
    public void setEnrollmentCount(Long enrollmentCount) { this.enrollmentCount = enrollmentCount; }
    public Long getPublishedGradeCount() { return publishedGradeCount; }
    public void setPublishedGradeCount(Long publishedGradeCount) { this.publishedGradeCount = publishedGradeCount; }
    public Long getDraftGradeCount() { return draftGradeCount; }
    public void setDraftGradeCount(Long draftGradeCount) { this.draftGradeCount = draftGradeCount; }
    public Long getMissingGradeCount() { return missingGradeCount; }
    public void setMissingGradeCount(Long missingGradeCount) { this.missingGradeCount = missingGradeCount; }
    public List<CourseSummaryVO> getPopularCourses() { return popularCourses; }
    public void setPopularCourses(List<CourseSummaryVO> popularCourses) { this.popularCourses = popularCourses; }
    public List<CourseSummaryVO> getFullCourses() { return fullCourses; }
    public void setFullCourses(List<CourseSummaryVO> fullCourses) { this.fullCourses = fullCourses; }
}

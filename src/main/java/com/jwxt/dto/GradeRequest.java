package com.jwxt.dto;

import jakarta.validation.constraints.NotNull;

public class GradeRequest {
    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    private Float score;

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Float getScore() { return score; }
    public void setScore(Float score) { this.score = score; }
}

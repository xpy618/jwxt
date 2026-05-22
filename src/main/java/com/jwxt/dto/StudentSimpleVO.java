package com.jwxt.dto;

public class StudentSimpleVO {
    private Long studentId;
    private String studentName;

    public StudentSimpleVO() {}

    public StudentSimpleVO(Long studentId, String studentName) {
        this.studentId = studentId;
        this.studentName = studentName;
    }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
}

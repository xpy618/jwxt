package com.jwxt.dto;

public class GradeVO {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseName;
    private Float score;
    private Float gpaPoint;
    private String status;
    private Float credit;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public Float getScore() { return score; }
    public void setScore(Float score) { this.score = score; }
    public Float getGpaPoint() { return gpaPoint; }
    public void setGpaPoint(Float gpaPoint) { this.gpaPoint = gpaPoint; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Float getCredit() { return credit; }
    public void setCredit(Float credit) { this.credit = credit; }
}

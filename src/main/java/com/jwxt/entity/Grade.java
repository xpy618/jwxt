package com.jwxt.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "grade")
public class Grade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column
    private Float score;

    @Column(name = "gpa_point")
    private Float gpaPoint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private GradeStatus status = GradeStatus.DRAFT;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Float getScore() { return score; }
    public void setScore(Float score) { this.score = score; }
    public Float getGpaPoint() { return gpaPoint; }
    public void setGpaPoint(Float gpaPoint) { this.gpaPoint = gpaPoint; }
    public GradeStatus getStatus() { return status; }
    public void setStatus(GradeStatus status) { this.status = status; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
}

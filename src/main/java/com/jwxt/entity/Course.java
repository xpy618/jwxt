package com.jwxt.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "course")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @Column(name = "max_students", nullable = false)
    private Integer maxStudents = 30;

    @Column(nullable = false, length = 20)
    private String semester;

    @Column(nullable = false)
    private Float credit = 1.0f;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public Integer getMaxStudents() { return maxStudents; }
    public void setMaxStudents(Integer maxStudents) { this.maxStudents = maxStudents; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public Float getCredit() { return credit; }
    public void setCredit(Float credit) { this.credit = credit; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    private void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

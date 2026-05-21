package com.jwxt.dto;

public class CourseVO {
    private Long id;
    private String name;
    private String description;
    private Long teacherId;
    private String teacherName;
    private Integer maxStudents;
    private Long enrolledCount;
    private String semester;
    private Float credit;
    private String schedule;
    private String location;
    private boolean enrolled;
    private String category;
    private Integer startWeek;
    private Integer endWeek;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public Integer getMaxStudents() { return maxStudents; }
    public void setMaxStudents(Integer maxStudents) { this.maxStudents = maxStudents; }
    public Long getEnrolledCount() { return enrolledCount; }
    public void setEnrolledCount(Long enrolledCount) { this.enrolledCount = enrolledCount; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public Float getCredit() { return credit; }
    public void setCredit(Float credit) { this.credit = credit; }
    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public boolean isEnrolled() { return enrolled; }
    public void setEnrolled(boolean enrolled) { this.enrolled = enrolled; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getStartWeek() { return startWeek; }
    public void setStartWeek(Integer startWeek) { this.startWeek = startWeek; }
    public Integer getEndWeek() { return endWeek; }
    public void setEndWeek(Integer endWeek) { this.endWeek = endWeek; }
}

package com.jwxt.dto;

public class ScheduleVO {
    private Long courseId;
    private String courseName;
    private String teacherName;
    private String schedule;
    private String semester;
    private Float credit;
    private String location;
    private Integer startWeek;
    private Integer endWeek;

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public Float getCredit() { return credit; }
    public void setCredit(Float credit) { this.credit = credit; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getStartWeek() { return startWeek; }
    public void setStartWeek(Integer startWeek) { this.startWeek = startWeek; }
    public Integer getEndWeek() { return endWeek; }
    public void setEndWeek(Integer endWeek) { this.endWeek = endWeek; }
}

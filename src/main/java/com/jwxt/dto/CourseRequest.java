package com.jwxt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CourseRequest {
    @NotBlank(message = "课程名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "最大人数不能为空")
    private Integer maxStudents;

    @NotBlank(message = "学期不能为空")
    private String semester;

    @NotNull(message = "学分不能为空")
    private Float credit;

    private String schedule;

    private String location;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getMaxStudents() { return maxStudents; }
    public void setMaxStudents(Integer maxStudents) { this.maxStudents = maxStudents; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public Float getCredit() { return credit; }
    public void setCredit(Float credit) { this.credit = credit; }
    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}

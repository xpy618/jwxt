package com.jwxt.dto;

import java.util.List;

public class EnrollmentPreviewRequest {
    private List<Long> courseIds;

    public List<Long> getCourseIds() { return courseIds; }
    public void setCourseIds(List<Long> courseIds) { this.courseIds = courseIds; }
}

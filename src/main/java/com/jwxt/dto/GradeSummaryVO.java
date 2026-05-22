package com.jwxt.dto;

import java.util.List;

public class GradeSummaryVO {
    private Long courseId;
    private String courseName;
    private Integer enrolledCount;
    private Integer recordedCount;
    private Integer missingCount;
    private Integer draftCount;
    private Integer publishedCount;
    private Double averageScore;
    private Double maxScore;
    private Double minScore;
    private Boolean readyToPublish;
    private List<StudentSimpleVO> missingStudents;
    private List<String> warnings;

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public Integer getEnrolledCount() { return enrolledCount; }
    public void setEnrolledCount(Integer enrolledCount) { this.enrolledCount = enrolledCount; }
    public Integer getRecordedCount() { return recordedCount; }
    public void setRecordedCount(Integer recordedCount) { this.recordedCount = recordedCount; }
    public Integer getMissingCount() { return missingCount; }
    public void setMissingCount(Integer missingCount) { this.missingCount = missingCount; }
    public Integer getDraftCount() { return draftCount; }
    public void setDraftCount(Integer draftCount) { this.draftCount = draftCount; }
    public Integer getPublishedCount() { return publishedCount; }
    public void setPublishedCount(Integer publishedCount) { this.publishedCount = publishedCount; }
    public Double getAverageScore() { return averageScore; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
    public Double getMaxScore() { return maxScore; }
    public void setMaxScore(Double maxScore) { this.maxScore = maxScore; }
    public Double getMinScore() { return minScore; }
    public void setMinScore(Double minScore) { this.minScore = minScore; }
    public Boolean getReadyToPublish() { return readyToPublish; }
    public void setReadyToPublish(Boolean readyToPublish) { this.readyToPublish = readyToPublish; }
    public List<StudentSimpleVO> getMissingStudents() { return missingStudents; }
    public void setMissingStudents(List<StudentSimpleVO> missingStudents) { this.missingStudents = missingStudents; }
    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
}

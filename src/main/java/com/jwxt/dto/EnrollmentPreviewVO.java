package com.jwxt.dto;

import java.util.List;

public class EnrollmentPreviewVO {
    private Double totalCredits;
    private Double selectedCredits;
    private Double availableCredits;
    private Boolean valid;
    private List<String> warnings;
    private List<Long> conflictCourseIds;

    public Double getTotalCredits() { return totalCredits; }
    public void setTotalCredits(Double totalCredits) { this.totalCredits = totalCredits; }
    public Double getSelectedCredits() { return selectedCredits; }
    public void setSelectedCredits(Double selectedCredits) { this.selectedCredits = selectedCredits; }
    public Double getAvailableCredits() { return availableCredits; }
    public void setAvailableCredits(Double availableCredits) { this.availableCredits = availableCredits; }
    public Boolean getValid() { return valid; }
    public void setValid(Boolean valid) { this.valid = valid; }
    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
    public List<Long> getConflictCourseIds() { return conflictCourseIds; }
    public void setConflictCourseIds(List<Long> conflictCourseIds) { this.conflictCourseIds = conflictCourseIds; }
}

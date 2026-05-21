package com.jwxt.dto;

public class AcademicProgressItemVO {
    private String category;
    private String categoryName;
    private Double requiredCredits;
    private Double completedCredits;
    private Double progressPercent;

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Double getRequiredCredits() { return requiredCredits; }
    public void setRequiredCredits(Double requiredCredits) { this.requiredCredits = requiredCredits; }
    public Double getCompletedCredits() { return completedCredits; }
    public void setCompletedCredits(Double completedCredits) { this.completedCredits = completedCredits; }
    public Double getProgressPercent() { return progressPercent; }
    public void setProgressPercent(Double progressPercent) { this.progressPercent = progressPercent; }
}

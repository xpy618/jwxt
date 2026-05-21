package com.jwxt.dto;

import java.util.List;

public class AcademicProgressVO {
    private Double totalRequiredCredits;
    private Double totalCompletedCredits;
    private Double totalProgressPercent;
    private List<AcademicProgressItemVO> items;

    public Double getTotalRequiredCredits() { return totalRequiredCredits; }
    public void setTotalRequiredCredits(Double totalRequiredCredits) { this.totalRequiredCredits = totalRequiredCredits; }
    public Double getTotalCompletedCredits() { return totalCompletedCredits; }
    public void setTotalCompletedCredits(Double totalCompletedCredits) { this.totalCompletedCredits = totalCompletedCredits; }
    public Double getTotalProgressPercent() { return totalProgressPercent; }
    public void setTotalProgressPercent(Double totalProgressPercent) { this.totalProgressPercent = totalProgressPercent; }
    public List<AcademicProgressItemVO> getItems() { return items; }
    public void setItems(List<AcademicProgressItemVO> items) { this.items = items; }
}

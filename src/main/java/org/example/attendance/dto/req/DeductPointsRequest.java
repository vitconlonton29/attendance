package org.example.attendance.dto.req;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class DeductPointsRequest {
    @NotNull
    @Positive
    private Long points;

    private String description;
    private String referenceId;

    // Getters and Setters
    public Long getPoints() { return points; }
    public void setPoints(Long points) { this.points = points; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
}
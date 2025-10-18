package org.example.attendance.dto.req;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class BonusPointsRequest {
    @NotNull(message = "Points is required")
    @Positive(message = "Points must be positive")
    private Long points;

    @NotNull(message = "Description is required")
    private String description;

    private String referenceId;
}
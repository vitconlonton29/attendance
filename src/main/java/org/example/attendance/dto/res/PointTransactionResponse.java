package org.example.attendance.dto.res;

import lombok.Data;

@Data
public class PointTransactionResponse {
    private String id;
    private String transactionType;
    private Long points;
    private String description;
    private String referenceId;
    private Long balanceAfter;
    private String transactionDate;
    private String createdAt;
}
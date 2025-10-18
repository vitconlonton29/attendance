package org.example.attendance.entity;

import jakarta.persistence.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_transactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false)
    private Long points;

    private String description;

    @Column(name = "reference_id")
    private String referenceId;

    public enum TransactionType {
        ATTENDANCE, DEDUCTION, OTHER
    }

    @Column(name = "balance_after")
    private Long balanceAfter;

    @Column(name = "transaction_date", length = 10)
    private String transactionDate; // YYYY-MM-DD

}

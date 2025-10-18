package org.example.attendance.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "attendance_records")
@Data
public class AttendanceRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "points_earned", nullable = false)
    private Integer pointsEarned;

    @Column(name = "sequence_day", nullable = false)
    private Integer sequenceDay;

}

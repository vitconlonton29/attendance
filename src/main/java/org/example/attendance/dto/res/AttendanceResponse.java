package org.example.attendance.dto.res;

import lombok.Data;

@Data
public class AttendanceResponse {
    private boolean success;
    private Integer pointsEarned;
    private Integer sequenceDay;
    private String message;
    private Long currentPoints;
    private String attendanceDate;
}

package org.example.attendance.dto.res;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AttendanceStatusResponse {
    private String userId;
    private String displayName;
    private String avatarUrl;
    private Long currentPoints;
    private Integer currentMonthAttendanceDays;
    private Integer maxMonthlyAttendanceDays;
    private Boolean canAttendToday;
    private Boolean withinAttendanceTime;
    private String currentTimeSlot;
    private List<LocalDate> attendedDatesThisMonth;
    private Integer nextSequenceDay;
    private Integer nextPoints;
}
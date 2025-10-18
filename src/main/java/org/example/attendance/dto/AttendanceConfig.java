package org.example.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor

@NoArgsConstructor
public class AttendanceConfig {
    private Integer maxDaysPerMonth;
    private List<Integer> pointsSequence;
    private String morningStart;
    private String morningEnd;
    private String eveningStart;
    private String eveningEnd;


}
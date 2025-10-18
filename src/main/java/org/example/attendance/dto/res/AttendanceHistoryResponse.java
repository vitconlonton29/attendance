package org.example.attendance.dto.res;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AttendanceHistoryResponse {
    private String userId;
    private String displayName;
    private Integer totalRecords;
    private List<AttendanceRecordDTO> records;

    @Data
    public static class AttendanceRecordDTO {
        private String id;
        private LocalDate attendanceDate;
        private Integer pointsEarned;
        private Integer sequenceDay;
        private String createdAt;
    }
}

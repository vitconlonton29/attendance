package org.example.attendance.controller;


import org.example.attendance.dto.PagingResponse;
import org.example.attendance.dto.ResponseGeneral;
import org.example.attendance.dto.req.AttendanceMarkRequest;
import org.example.attendance.dto.res.AttendanceHistoryResponse;
import org.example.attendance.dto.res.AttendanceResponse;
import org.example.attendance.dto.res.AttendanceStatusResponse;
import org.example.attendance.service.AttendanceService;
import org.example.attendance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private UserService userService;


//API điểm danh hàng ngày
    @PostMapping("/users/{userId}/mark")
    public ResponseEntity<ResponseGeneral<AttendanceResponse>> markAttendance(
            @PathVariable String userId,
            @RequestBody(required = false) AttendanceMarkRequest request) {

        AttendanceResponse response = attendanceService.markAttendance(userId);
        return ResponseEntity.ok(ResponseGeneral.ofSuccess(response.getMessage(), response));
    }

    @GetMapping("/users/{userId}/history")
    public ResponseEntity<ResponseGeneral<PagingResponse<List<AttendanceHistoryResponse.AttendanceRecordDTO>>>> getAttendanceHistory(
            @PathVariable String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        // Verify user exists
        userService.detail(userId);

        PagingResponse<List<AttendanceHistoryResponse.AttendanceRecordDTO>> history =
                attendanceService.getAttendanceHistory(userId, startDate, endDate, page, size);

        return ResponseEntity.ok(ResponseGeneral.ofSuccess("Attendance history retrieved successfully", history));
    }


}

package org.example.attendance.controller;

import org.example.attendance.dto.PagingResponse;
import org.example.attendance.dto.ResponseGeneral;
import jakarta.validation.Valid;
import org.example.attendance.dto.req.DeductPointsRequest;
import org.example.attendance.dto.res.PointTransactionResponse;
import org.example.attendance.entity.PointTransaction;
import org.example.attendance.service.PointTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/points")
public class PointController {

    @Autowired
    private PointTransactionService pointService;

    @PostMapping("/users/{userId}/deduct")
    public ResponseEntity<ResponseGeneral<PointTransaction>> deductPoints(
            @PathVariable String userId,
            @Valid @RequestBody DeductPointsRequest request) {

        PointTransaction transaction = pointService.deductPoints(
                userId,
                request.getPoints(),
                request.getDescription(),
                request.getReferenceId()
        );

        return ResponseEntity.ok(ResponseGeneral.ofSuccess("SUCCESS", transaction));
    }

    @GetMapping("/users/{userId}/history")
    public ResponseEntity<ResponseGeneral<PagingResponse<List<PointTransactionResponse>>>> getPointHistory(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PagingResponse<List<PointTransactionResponse>> history =
                pointService.getPointHistory(userId, page, size);

        return ResponseEntity.ok(ResponseGeneral.ofSuccess("Attendance history retrieved successfully", history));
    }


}
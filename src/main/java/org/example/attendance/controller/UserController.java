package org.example.attendance.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.attendance.dto.ResponseGeneral;
import org.example.attendance.dto.req.UserRequest;
import org.example.attendance.dto.res.UserResponse;
import org.example.attendance.service.UserService;
import org.springframework.web.bind.annotation.*;


import static org.example.attendance.constant.AttendanceConstant.MessageCode.SUCCESS;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseGeneral<UserResponse> create(@RequestBody UserRequest request) {
        log.info("(create) request: {}", request);
        userService.create(request);

        return ResponseGeneral.ofSuccess(
                SUCCESS
        );
    }

    @GetMapping("/{id}")
    public ResponseGeneral<UserResponse> get(@PathVariable String id) {
        log.info("(get) id: {}", id);

        UserResponse response = userService.detail(id);
        log.info("(get) user: {}", response);

        return ResponseGeneral.ofSuccess(
                "Get user successfully",
                response
        );
    }
}

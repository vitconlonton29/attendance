package org.example.attendance.service;

import org.example.attendance.dto.req.UserRequest;
import org.example.attendance.dto.res.UserResponse;
import org.example.attendance.entity.User;
import org.example.attendance.service.base.BaseService;

public interface UserService extends BaseService<User> {
    UserResponse create(UserRequest request);

    UserResponse detail(String id);

}

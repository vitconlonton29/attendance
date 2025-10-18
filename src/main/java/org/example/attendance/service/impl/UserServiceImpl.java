package org.example.attendance.service.impl;


import lombok.extern.slf4j.Slf4j;
import org.example.attendance.dto.req.UserRequest;
import org.example.attendance.dto.res.UserResponse;
import org.example.attendance.entity.User;
import org.example.attendance.exception.base.BadRequestException;
import org.example.attendance.repository.UserRepository;
import org.example.attendance.service.UserService;
import org.example.attendance.service.base.BaseServiceImpl;
import org.springframework.stereotype.Service;

import static org.example.attendance.constant.AttendanceConstant.ErrorCode.USERNAME_ALREADY_EXISTS;
import static org.example.attendance.utils.MapperUtils.*;
import static org.example.attendance.constant.AttendanceConstant.AuthConstant.*;


@Service
@Slf4j
public class UserServiceImpl extends BaseServiceImpl<User> implements UserService {

    private final UserRepository repository;

    public UserServiceImpl(UserRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    public UserResponse create(UserRequest request) {
        checkExistedByUsername(request.getUsername());

        User user = toEntity(request, User.class);
        user.setIsActive(ENABLED);

        return toDTO(create(user), UserResponse.class);
    }

    public UserResponse detail(String id) {
        log.info("(detail) id:{}", id);

        return toDTO(get(id), UserResponse.class);
    }


    private void checkExistedByUsername(String username) {
        log.info("(checkExistedByUsername) username:{}", username);

        if (repository.existsByUsername(username)) {
            log.warn("(checkExistedByUsername) Username already exists: {}", username);
            throw new BadRequestException(USERNAME_ALREADY_EXISTS);
        }
    }
}

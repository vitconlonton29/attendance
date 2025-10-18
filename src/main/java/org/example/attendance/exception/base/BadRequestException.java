package org.example.attendance.exception.base;


import static org.example.attendance.constant.AttendanceConstant.StatusException.BAD_REQUEST;

public class BadRequestException extends BaseException {
  public BadRequestException() {
    setCode("BadRequestException");
    setStatus(BAD_REQUEST);
  }
    public BadRequestException(String message) {
        setCode("message");
        setStatus(BAD_REQUEST);
    }
}

package org.example.attendance.exception.base;


import static org.example.attendance.constant.AttendanceConstant.StatusException.NOT_FOUND;

public class NotFoundException extends BaseException {
  public NotFoundException(String id, String objectName) {
    setCode("com.ncsgroup.profiling.exception.base.NotFoundException");
    setStatus(NOT_FOUND);
    addParam("id", id);
    addParam("objectName", objectName);
  }

  public NotFoundException() {
    setCode("com.ncsgroup.profiling.exception.base.NotFoundException");
    setStatus(NOT_FOUND);
  }
}

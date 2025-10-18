package org.example.attendance.constant;

public class AttendanceConstant {
    public static class MessageCode {
        public static String SUCCESS = "Success";
    }
    public static class StatusException {
        public static final Integer NOT_FOUND = 404;
        public static final Integer CONFLICT = 409;
        public static final Integer BAD_REQUEST = 400;
        public static final Integer INTERNAL_SERVER_ERROR = 500;
    }

    public static class ErrorCode {
        public static final String  USERNAME_ALREADY_EXISTS = "ATT_0001";

        public static final String  ATTENDANCE_TIME_INVALID = "ATT_0002";
        public static final String  ATTENDANCE_ALREADY_MARKED = "ATT_0003";
        public static final String  MAX_ATTENDANCE_REACHED = "ATT_0004";

    }

    public static class AuthConstant {
        public static Integer ENABLED = 1;
        public static Integer DISABLED = 0;
    }
    public static class ConfigConstants {
        public static final String ATTENDANCE_MAX_DAYS_PER_MONTH = "attendance.max_days_per_month";
        public static final String ATTENDANCE_POINTS_SEQUENCE = "attendance.points_sequence";
        public static final String ATTENDANCE_MORNING_START = "attendance.time.morning_start";
        public static final String ATTENDANCE_MORNING_END = "attendance.time.morning_end";
        public static final String ATTENDANCE_EVENING_START = "attendance.time.evening_start";
        public static final String ATTENDANCE_EVENING_END = "attendance.time.evening_end";

        public static final String REDIS_CONFIG_PREFIX = "config:";
        public static final String REDIS_CONFIG_ALL_KEY = "config:all";

        public static final int DEFAULT_MAX_DAYS_PER_MONTH = 7;
        public static final String DEFAULT_POINTS_SEQUENCE = "1,2,3,5,8,13,21";
        public static final String DEFAULT_MORNING_START = "09:00";
        public static final String DEFAULT_MORNING_END = "11:00";
        public static final String DEFAULT_EVENING_START = "19:00";
        public static final String DEFAULT_EVENING_END = "21:00";
    }
}

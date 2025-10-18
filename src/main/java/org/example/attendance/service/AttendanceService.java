package org.example.attendance.service;

import org.example.attendance.constant.AttendanceConstant;
import org.example.attendance.dto.PagingResponse;
import org.example.attendance.dto.res.AttendanceHistoryResponse;
import org.example.attendance.dto.res.AttendanceResponse;
import org.example.attendance.entity.AttendanceRecord;
import org.example.attendance.entity.PointTransaction;
import org.example.attendance.entity.User;
import org.example.attendance.exception.base.BadRequestException;
import org.example.attendance.exception.base.NotFoundException;
import org.example.attendance.repository.AttendanceRecordRepository;
import org.example.attendance.repository.PointTransactionRepository;
import org.example.attendance.repository.UserRepository;
import org.example.attendance.utils.MapperUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Autowired
    private PointTransactionRepository pointTransactionRepository;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private RedissonClient redissonClient;

    @Transactional
    public AttendanceResponse markAttendance(String userId) {
        LocalDate today = LocalDate.now();
        String lockKey = String.format("attendance:lock:user:%s:date:%s", userId, today);
        RLock lock = redissonClient.getLock(lockKey);

        logger.info("Attempting to acquire distributed lock for key: {}", lockKey);

        try {
            boolean locked = lock.tryLock(5, 30, TimeUnit.SECONDS);

            if (!locked) {
                throw new BadRequestException();
            }

            logger.info("Successfully acquired distributed lock for user: {}, date: {}", userId, today);
            return processAttendance(userId, today);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Thread interrupted while acquiring lock for user: {}", userId, e);
            throw new BadRequestException();
        } finally {
            try {
                if (lock != null && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                    logger.info("Released distributed lock for user: {}, date: {}", userId, today);
                }
            } catch (Exception e) {
                logger.error("Error releasing lock for user: {}", userId, e);
            }
        }
    }


    @Transactional(isolation = Isolation.REPEATABLE_READ)
    protected AttendanceResponse processAttendance(String userId, LocalDate today) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));

        LocalTime now = LocalTime.now();

        if (!isWithinAttendanceTime(now)) {
            throw new BadRequestException(
                    AttendanceConstant.ErrorCode.ATTENDANCE_TIME_INVALID
            );
        }

        if (attendanceRecordRepository.existsByUserIdAndAttendanceDate(userId, today)) {
            throw new BadRequestException(
                    AttendanceConstant.ErrorCode.ATTENDANCE_ALREADY_MARKED
            );
        }

        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();
        long monthlyAttendanceCount = attendanceRecordRepository.countByUserIdAndMonth(userId, currentYear, currentMonth);

        var attendanceConfig = systemConfigService.getAttendanceConfig();
        if (monthlyAttendanceCount >= attendanceConfig.getMaxDaysPerMonth()) {
            throw new org.example.attendance.exception.base.BadRequestException(
                    AttendanceConstant.ErrorCode.MAX_ATTENDANCE_REACHED
            );
        }

        int sequenceDay = getNextSequenceDay(userId, currentYear, currentMonth);
        int points = getPointsForSequenceDay(sequenceDay, attendanceConfig.getPointsSequence());

        // Create attendance record
        AttendanceRecord record = new AttendanceRecord();
        record.setUser(user);
        record.setAttendanceDate(today);
        record.setPointsEarned(points);
        record.setSequenceDay(sequenceDay);
        attendanceRecordRepository.save(record);

        // Update user points
        user.setLotusPoints(user.getLotusPoints() + points);
        userRepository.save(user);

        // Create point transaction
        PointTransaction transaction = new PointTransaction();
        transaction.setUser(user);
        transaction.setTransactionType(PointTransaction.TransactionType.ATTENDANCE);
        transaction.setPoints((long) points);
        transaction.setDescription("Điểm danh ngày thứ " + sequenceDay);
        transaction.setReferenceId("ATTENDANCE_" + today + "_" + System.currentTimeMillis());
        transaction.setBalanceAfter(user.getLotusPoints());
        pointTransactionRepository.save(transaction);

        logger.info("User {} marked attendance successfully for day {}, earned {} points",
                userId, sequenceDay, points);

        AttendanceResponse response = new AttendanceResponse();
        response.setSuccess(true);
        response.setPointsEarned(points);
        response.setSequenceDay(sequenceDay);
        response.setMessage("Điểm danh thành công! Nhận được " + points + " điểm");
        response.setCurrentPoints(user.getLotusPoints());
        response.setAttendanceDate(today.toString());

        return response;
    }

    private boolean isWithinAttendanceTime(LocalTime time) {
        var config = systemConfigService.getAttendanceConfig();
        LocalTime morningStart = LocalTime.parse(config.getMorningStart());
        LocalTime morningEnd = LocalTime.parse(config.getMorningEnd());
        LocalTime eveningStart = LocalTime.parse(config.getEveningStart());
        LocalTime eveningEnd = LocalTime.parse(config.getEveningEnd());

        return (time.isAfter(morningStart) && time.isBefore(morningEnd)) ||
                (time.isAfter(eveningStart) && time.isBefore(eveningEnd));
    }

    private int getNextSequenceDay(String userId, int year, int month) {
        Optional<Integer> maxSequenceDay = attendanceRecordRepository
                .findMaxSequenceDayByUserIdAndMonth(userId, year, month);
        return maxSequenceDay.map(day -> day + 1).orElse(1);
    }

    private int getPointsForSequenceDay(int sequenceDay, List<Integer> pointsSequence) {
        if (sequenceDay <= 0 || sequenceDay > pointsSequence.size()) {
            return pointsSequence.get(pointsSequence.size() - 1);
        }
        return pointsSequence.get(sequenceDay - 1);
    }

    public boolean canUserAttendToday(String userId) {
        LocalDate today = LocalDate.now();
        return !attendanceRecordRepository.existsByUserIdAndAttendanceDate(userId, today);
    }

    public boolean isWithinAttendanceTime() {
        return isWithinAttendanceTime(LocalTime.now());
    }

    public PagingResponse<List<AttendanceHistoryResponse.AttendanceRecordDTO>> getAttendanceHistory(
            String userId, LocalDate startDate, LocalDate endDate, int page, int size) {


        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));

        Pageable pageable =  PageRequest.of(page, size);
        Page<AttendanceRecord> recordsPage;

        // Nếu không có startDate và endDate, mặc định lấy tháng hiện tại
        if (startDate == null && endDate == null) {
            YearMonth currentMonth = YearMonth.now();
            startDate = currentMonth.atDay(1); // Ngày đầu tháng
            endDate = currentMonth.atEndOfMonth(); // Ngày cuối tháng
            logger.debug("No date range provided, using current month: {} to {}", startDate, endDate);
        }


        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BadRequestException("INVALID_DATE_RANGE");
        }


            recordsPage = attendanceRecordRepository.findByUserIdAndDateRange(userId, startDate, endDate, pageable);



        List<AttendanceHistoryResponse.AttendanceRecordDTO> recordDTOs = recordsPage.getContent().stream()
                .map(this::convertToAttendanceRecordDTO)
                .collect(Collectors.toList());

        PagingResponse<List<AttendanceHistoryResponse.AttendanceRecordDTO>> response = PagingResponse.of(
                recordDTOs,
                page,
                size,
                recordsPage.getTotalElements(),
                recordsPage.getTotalPages()
        );

        logger.info("Retrieved {} attendance records for user {} in period {} to {}",
                recordDTOs.size(), userId, startDate, endDate);

        return response;
    }

    public Map<String, Object> getCurrentMonthStats(String userId) {
        // Verify user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));

        YearMonth currentMonth = YearMonth.now();
        LocalDate monthStart = currentMonth.atDay(1);
        LocalDate monthEnd = currentMonth.atEndOfMonth();

        Map<String, Object> stats = new HashMap<>();

        Long monthAttendance = attendanceRecordRepository.countByUserIdAndDateRange(userId, monthStart, monthEnd);
        Long monthPoints = attendanceRecordRepository.sumPointsByUserIdAndDateRange(userId, monthStart, monthEnd);

        Long totalAttendance = attendanceRecordRepository.countByUserId(userId);
        Long totalPoints = pointTransactionRepository.sumAttendancePointsByUserId(userId);

        var attendanceConfig = systemConfigService.getAttendanceConfig();

        stats.put("userId", userId);
        stats.put("userName", user.getDisplayName());
        stats.put("currentMonth", currentMonth.toString());
        stats.put("monthStart", monthStart.toString());
        stats.put("monthEnd", monthEnd.toString());
        stats.put("monthAttendance", monthAttendance != null ? monthAttendance : 0);
        stats.put("monthPoints", monthPoints != null ? monthPoints : 0);
        stats.put("maxMonthlyAttendance", attendanceConfig.getMaxDaysPerMonth());
        stats.put("remainingAttendance", attendanceConfig.getMaxDaysPerMonth() - (monthAttendance != null ? monthAttendance : 0));
        stats.put("totalAttendance", totalAttendance != null ? totalAttendance : 0);
        stats.put("totalPoints", totalPoints != null ? totalPoints : 0);
        stats.put("currentPoints", user.getLotusPoints());

        logger.debug("Current month stats for user {}: {}/{} attendance",
                userId, monthAttendance, attendanceConfig.getMaxDaysPerMonth());

        return stats;
    }


    private AttendanceHistoryResponse.AttendanceRecordDTO convertToAttendanceRecordDTO(AttendanceRecord record) {
        AttendanceHistoryResponse.AttendanceRecordDTO dto =
                MapperUtils.toDTO(record, AttendanceHistoryResponse.AttendanceRecordDTO.class);

        String dayOfWeek = record.getAttendanceDate().getDayOfWeek()
                .getDisplayName(TextStyle.FULL, new Locale("vi", "VN"));


        if (record.getCreatedAt() != null) {
            dto.setCreatedAt(java.time.Instant.ofEpochMilli(record.getCreatedAt())
                    .atZone(java.time.ZoneId.systemDefault())
                    .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        return dto;
    }

}
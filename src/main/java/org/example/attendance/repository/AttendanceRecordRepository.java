package org.example.attendance.repository;

import org.example.attendance.entity.AttendanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends BaseRepository<AttendanceRecord> {

    Optional<AttendanceRecord> findByUserIdAndAttendanceDate(String userId, LocalDate attendanceDate);

    boolean existsByUserIdAndAttendanceDate(String userId, LocalDate attendanceDate);

    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.user.id = :userId AND EXTRACT(YEAR FROM ar.attendanceDate) = :year AND EXTRACT(MONTH FROM ar.attendanceDate) = :month ORDER BY ar.attendanceDate")
    List<AttendanceRecord> findByUserIdAndMonth(@Param("userId") String userId,
                                                @Param("year") int year,
                                                @Param("month") int month);

    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar WHERE ar.user.id = :userId AND EXTRACT(YEAR FROM ar.attendanceDate) = :year AND EXTRACT(MONTH FROM ar.attendanceDate) = :month")
    long countByUserIdAndMonth(@Param("userId") String userId,
                               @Param("year") int year,
                               @Param("month") int month);

    @Query("SELECT MAX(ar.sequenceDay) FROM AttendanceRecord ar WHERE ar.user.id = :userId AND EXTRACT(YEAR FROM ar.attendanceDate) = :year AND EXTRACT(MONTH FROM ar.attendanceDate) = :month")
    Optional<Integer> findMaxSequenceDayByUserIdAndMonth(@Param("userId") String userId,
                                                         @Param("year") int year,
                                                         @Param("month") int month);


    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.user.id = :userId AND ar.attendanceDate BETWEEN :startDate AND :endDate ORDER BY ar.attendanceDate DESC")
    Page<AttendanceRecord> findByUserIdAndDateRange(@Param("userId") String userId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate,
                                                    Pageable pageable);


    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar WHERE ar.user.id = :userId")
    Long countByUserId(@Param("userId") String userId);

    @Query("SELECT SUM(ar.pointsEarned) FROM AttendanceRecord ar WHERE ar.user.id = :userId AND ar.attendanceDate BETWEEN :startDate AND :endDate")
    Long sumPointsByUserIdAndDateRange(@Param("userId") String userId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar WHERE ar.user.id = :userId AND ar.attendanceDate BETWEEN :startDate AND :endDate")
    Long countByUserIdAndDateRange(@Param("userId") String userId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

}
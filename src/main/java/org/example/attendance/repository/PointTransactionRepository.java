package org.example.attendance.repository;

import org.example.attendance.entity.PointTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PointTransactionRepository extends BaseRepository<PointTransaction> {

    Page<PointTransaction> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    @Query("SELECT SUM(pt.points) FROM PointTransaction pt WHERE pt.user.id = :userId AND pt.transactionType = 'ATTENDANCE'")
    Long sumAttendancePointsByUserId(@Param("userId") String userId);

    @Query("SELECT SUM(pt.points) FROM PointTransaction pt WHERE pt.user.id = :userId AND pt.transactionType = 'DEDUCTION'")
    Long sumDeductionPointsByUserId(@Param("userId") String userId);
}
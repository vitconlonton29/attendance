package org.example.attendance.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.example.attendance.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepository<User> {
    boolean existsByUsername(String username);

    @Query("SELECT u.lotusPoints FROM User u WHERE u.id = :userId")
    Optional<Long> findLotusPointsByUserId(@Param("userId") String userId);
}

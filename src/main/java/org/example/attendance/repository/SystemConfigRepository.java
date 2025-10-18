package org.example.attendance.repository;

import org.example.attendance.entity.SystemConfig;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemConfigRepository extends BaseRepository<SystemConfig> {

    Optional<SystemConfig> findByKey(String key);

    List<SystemConfig> findByIsActiveTrue();

    @Query("SELECT sc FROM SystemConfig sc WHERE sc.isActive = true AND sc.key IN :keys")
    List<SystemConfig> findByKeys(List<String> keys);

    boolean existsByKey(String key);
}
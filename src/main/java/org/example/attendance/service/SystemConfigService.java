package org.example.attendance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.attendance.constant.AttendanceConstant;
import org.example.attendance.constant.AttendanceConstant.ConfigConstants;
import org.example.attendance.dto.AttendanceConfig;
import org.example.attendance.entity.SystemConfig;
import org.example.attendance.repository.SystemConfigRepository;
import org.example.attendance.service.base.BaseServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SystemConfigService extends BaseServiceImpl<SystemConfig> {

    private static final Logger logger = LoggerFactory.getLogger(SystemConfigService.class);

    private final SystemConfigRepository systemConfigRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final long REDIS_CACHE_TTL = 24 * 60 * 60;

    public SystemConfigService(SystemConfigRepository systemConfigRepository) {
        super(systemConfigRepository);
        this.systemConfigRepository = systemConfigRepository;
    }


    @EventListener(ApplicationReadyEvent.class)
    public void autoLoadConfigsOnStartup() {
        logger.info("Auto-loading system configurations to Redis on startup...");
        try {
            loadAllConfigsToRedis();
            logger.info("System configurations loaded successfully on startup");
        } catch (Exception e) {
            logger.error("Failed to auto-load configurations on startup", e);
        }
    }


    public void loadAllConfigsToRedis() {
        try {
            List<SystemConfig> activeConfigs = systemConfigRepository.findByIsActiveTrue();

            if (activeConfigs.isEmpty()) {
                logger.warn(" No active configurations found in database");
                return;
            }

            Map<String, String> configMap = activeConfigs.stream()
                    .collect(Collectors.toMap(
                            SystemConfig::getKey,
                            SystemConfig::getValue
                    ));

            redisTemplate.delete(ConfigConstants.REDIS_CONFIG_ALL_KEY);
            redisTemplate.opsForHash().putAll(ConfigConstants.REDIS_CONFIG_ALL_KEY, configMap);
            redisTemplate.expire(ConfigConstants.REDIS_CONFIG_ALL_KEY, REDIS_CACHE_TTL, TimeUnit.SECONDS);

            logger.info("Loaded {} configs to Redis cache with key: {}", configMap.size(), ConfigConstants.REDIS_CONFIG_ALL_KEY);

            configMap.forEach((key, value) ->
                    logger.debug("Config loaded - {} = {}", key, value)
            );

        } catch (Exception e) {
            logger.error("Failed to load configs to Redis", e);
            throw new RuntimeException("Failed to initialize system configurations", e);
        }
    }

    public String getConfigValue(String key, String defaultValue) {
        try {

            Object value = redisTemplate.opsForHash().get(ConfigConstants.REDIS_CONFIG_ALL_KEY, key);
            if (value != null) {
                logger.debug("Config [{}] found in Redis: {}", key, value);
                return value.toString();
            }

            logger.debug("Config [{}] not in Redis, checking database...", key);

            Optional<SystemConfig> config = systemConfigRepository.findByKey(key);
            if (config.isPresent() && config.get().getIsActive()) {
                String configValue = config.get().getValue();
                logger.debug("Config [{}] found in database: {}", key, configValue);

                redisTemplate.opsForHash().put(ConfigConstants.REDIS_CONFIG_ALL_KEY, key, configValue);
                return configValue;
            }

            logger.warn("Config [{}] not found, using default: {}", key, defaultValue);
            return defaultValue;

        } catch (Exception e) {
            logger.warn("Failed to get config [{}] from Redis, using default: {}", key, defaultValue, e);
            return defaultValue;
        }
    }

    public AttendanceConfig getAttendanceConfig() {
        try {
            logger.debug("Getting attendance configuration");

            Map<Object, Object> configMap = redisTemplate.opsForHash().entries(ConfigConstants.REDIS_CONFIG_ALL_KEY);

            if (configMap == null || configMap.isEmpty()) {
                logger.warn("Rdis cache empty, loading from database");
                loadAllConfigsToRedis();
                configMap = redisTemplate.opsForHash().entries(ConfigConstants.REDIS_CONFIG_ALL_KEY);
            }

            int maxDays = Integer.parseInt(getConfigFromMap(configMap,
                    ConfigConstants.ATTENDANCE_MAX_DAYS_PER_MONTH,
                    String.valueOf(ConfigConstants.DEFAULT_MAX_DAYS_PER_MONTH)));

            String pointsSequenceStr = getConfigFromMap(configMap,
                    ConfigConstants.ATTENDANCE_POINTS_SEQUENCE,
                    ConfigConstants.DEFAULT_POINTS_SEQUENCE);

            List<Integer> pointsSequence = Arrays.stream(pointsSequenceStr.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            String morningStart = getConfigFromMap(configMap,
                    ConfigConstants.ATTENDANCE_MORNING_START,
                    ConfigConstants.DEFAULT_MORNING_START);

            String morningEnd = getConfigFromMap(configMap,
                    ConfigConstants.ATTENDANCE_MORNING_END,
                    ConfigConstants.DEFAULT_MORNING_END);

            String eveningStart = getConfigFromMap(configMap,
                    ConfigConstants.ATTENDANCE_EVENING_START,
                    ConfigConstants.DEFAULT_EVENING_START);

            String eveningEnd = getConfigFromMap(configMap,
                    ConfigConstants.ATTENDANCE_EVENING_END,
                    ConfigConstants.DEFAULT_EVENING_END);

            logger.debug("Attendance config loaded - MaxDays: {}, Points: {}, Time: {}-{} & {}-{}",
                    maxDays, pointsSequence, morningStart, morningEnd, eveningStart, eveningEnd);

            return new AttendanceConfig(maxDays, pointsSequence, morningStart, morningEnd, eveningStart, eveningEnd);

        } catch (Exception e) {
            logger.error("Failed to get attendance config, using defaults", e);
            return getDefaultAttendanceConfig();
        }
    }

    private String getConfigFromMap(Map<Object, Object> configMap, String key, String defaultValue) {
        Object value = configMap.get(key);
        if (value == null) {
            logger.warn("Config key [{}] not found in cache, using default: {}", key, defaultValue);
            return defaultValue;
        }
        return value.toString();
    }

    private AttendanceConfig getDefaultAttendanceConfig() {
        logger.warn("Using default attendance configuration");

        List<Integer> defaultPoints = Arrays.stream(ConfigConstants.DEFAULT_POINTS_SEQUENCE.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        return new AttendanceConfig(
                ConfigConstants.DEFAULT_MAX_DAYS_PER_MONTH,
                defaultPoints,
                ConfigConstants.DEFAULT_MORNING_START,
                ConfigConstants.DEFAULT_MORNING_END,
                ConfigConstants.DEFAULT_EVENING_START,
                ConfigConstants.DEFAULT_EVENING_END
        );
    }

    public void refreshConfigCache() {
        try {
            logger.info("Refreshing configuration cache...");
            redisTemplate.delete(ConfigConstants.REDIS_CONFIG_ALL_KEY);
            loadAllConfigsToRedis();
            logger.info("Configuration cache refreshed successfully");
        } catch (Exception e) {
            logger.error("Failed to refresh config cache", e);
            throw new RuntimeException("Failed to refresh configuration cache", e);
        }
    }


    public Map<String, Object> getCacheStatus() {
        Map<String, Object> status = new HashMap<>();
        try {
            Long size = redisTemplate.opsForHash().size(ConfigConstants.REDIS_CONFIG_ALL_KEY);
            Long ttl = redisTemplate.getExpire(ConfigConstants.REDIS_CONFIG_ALL_KEY, TimeUnit.SECONDS);

            status.put("cacheKey", ConfigConstants.REDIS_CONFIG_ALL_KEY);
            status.put("configCount", size != null ? size : 0);
            status.put("ttlSeconds", ttl != null ? ttl : 0);
            status.put("cacheEnabled", true);

        } catch (Exception e) {
            status.put("cacheEnabled", false);
            status.put("error", e.getMessage());
        }
        return status;
    }

    public SystemConfig updateConfig(String key, String value, String description) {
        Optional<SystemConfig> existingConfig = systemConfigRepository.findByKey(key);
        SystemConfig config;

        if (existingConfig.isPresent()) {
            config = existingConfig.get();
            config.setValue(value);
            if (description != null) {
                config.setDescription(description);
            }
        } else {
            config = new SystemConfig();
            config.setKey(key);
            config.setValue(value);
            config.setDescription(description);
            config.setType("STRING");
            config.setIsActive(true);
        }

        SystemConfig savedConfig = systemConfigRepository.save(config);

        refreshConfigCache();

        return savedConfig;
    }
}
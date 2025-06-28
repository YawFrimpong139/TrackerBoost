package org.codewithzea.trackerboost.cache;



import org.codewithzea.trackerboost.optimize.InstrumentedStatsCounter;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${spring.cache.type:caffeine}")
    private String cacheType;

    @Value("${app.caffeine.initial.size:100}")
    private int initialSize;

    @Value("${app.caffeine.max.size:1000}")
    private int maxSize;

    @Value("${app.cache.ttl.tasks:2h}")
    private String tasksTtl;

    @Value("${app.cache.ttl.projects:1h}")
    private String projectsTtl;

    @Value("${app.cache.ttl.developers:2h}")
    private String developersTtl;

    @Value("${app.cache.ttl.projectTasks:30m}")
    private String projectTasksTtl;

    @Value("${app.cache.ttl.developerTasks:1h}")
    private String developerTasksTtl;

    @Value("${app.cache.ttl.overdueTasks:15m}")
    private String overdueTasksTtl;

    @Value("${app.cache.ttl.taskStatusCounts:10m}")
    private String taskStatusCountsTtl;

    @Value("${app.cache.ttl.userTasks:1h}")
    private String userTasksTtl;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory,
                                     MeterRegistry meterRegistry) {
        return switch (cacheType.toLowerCase()) {
            case "redis" -> redisCacheManager(redisConnectionFactory, meterRegistry);
            default -> caffeineCacheManager(meterRegistry);
        };
    }

    private CacheManager caffeineCacheManager(MeterRegistry meterRegistry) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(defaultCaffeineBuilder(meterRegistry));

        Map<String, Caffeine<Object, Object>> builders = new HashMap<>();
        builders.put("tasks", Caffeine.newBuilder()
                .expireAfterWrite(convertToDuration(tasksTtl))
                .maximumSize(500)
                .recordStats(() -> new InstrumentedStatsCounter(meterRegistry, "tasks")));

        builders.put("projects", Caffeine.newBuilder()
                .expireAfterWrite(convertToDuration(projectsTtl))
                .maximumSize(300)
                .recordStats(() -> new InstrumentedStatsCounter(meterRegistry, "projects")));

        builders.put("developers", Caffeine.newBuilder()
                .expireAfterAccess(convertToDuration(developersTtl))
                .maximumSize(200)
                .recordStats(() -> new InstrumentedStatsCounter(meterRegistry, "developers")));

        builders.put("projectTasks", Caffeine.newBuilder()
                .expireAfterWrite(convertToDuration(projectTasksTtl))
                .maximumSize(1000)
                .recordStats(() -> new InstrumentedStatsCounter(meterRegistry, "projectTasks")));

        builders.put("developerTasks", Caffeine.newBuilder()
                .expireAfterWrite(convertToDuration(developerTasksTtl))
                .maximumSize(800)
                .recordStats(() -> new InstrumentedStatsCounter(meterRegistry, "developerTasks")));

        builders.put("overdueTasks", Caffeine.newBuilder()
                .expireAfterWrite(convertToDuration(overdueTasksTtl))
                .maximumSize(200)
                .recordStats(() -> new InstrumentedStatsCounter(meterRegistry, "overdueTasks")));

        builders.put("taskStatusCounts", Caffeine.newBuilder()
                .expireAfterWrite(convertToDuration(taskStatusCountsTtl))
                .maximumSize(50)
                .recordStats(() -> new InstrumentedStatsCounter(meterRegistry, "taskStatusCounts")));

        builders.put("userTasks", Caffeine.newBuilder()
                .expireAfterAccess(convertToDuration(userTasksTtl))
                .maximumSize(1000)
                .recordStats(() -> new InstrumentedStatsCounter(meterRegistry, "userTasks")));

        builders.forEach((name, caffeineBuilder) -> {
            com.github.benmanes.caffeine.cache.Cache<Object, Object> cache = caffeineBuilder.build();
            cacheManager.registerCustomCache(name, cache);
        });

        return cacheManager;
    }

    private Caffeine<Object, Object> defaultCaffeineBuilder(MeterRegistry meterRegistry) {
        return Caffeine.newBuilder()
                .initialCapacity(initialSize)
                .maximumSize(maxSize)
                .expireAfterWrite(convertToDuration("30m"))
                .recordStats(() -> new InstrumentedStatsCounter(meterRegistry, "default"));
    }

    private CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory,
                                           MeterRegistry meterRegistry) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("tasks", defaultConfig.entryTtl(convertToDuration(tasksTtl)));
        cacheConfigs.put("projects", defaultConfig.entryTtl(convertToDuration(projectsTtl)));
        cacheConfigs.put("developers", defaultConfig.entryTtl(convertToDuration(developersTtl)));
        cacheConfigs.put("projectTasks", defaultConfig.entryTtl(convertToDuration(projectTasksTtl)));
        cacheConfigs.put("developerTasks", defaultConfig.entryTtl(convertToDuration(developerTasksTtl)));
        cacheConfigs.put("overdueTasks", defaultConfig.entryTtl(convertToDuration(overdueTasksTtl)));
        cacheConfigs.put("taskStatusCounts", defaultConfig.entryTtl(convertToDuration(taskStatusCountsTtl)));
        cacheConfigs.put("userTasks", defaultConfig.entryTtl(convertToDuration(userTasksTtl)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }

    private Duration convertToDuration(String durationStr) {
        if (durationStr.endsWith("ms")) {
            return Duration.ofMillis(Long.parseLong(durationStr.substring(0, durationStr.length() - 2)));
        } else if (durationStr.endsWith("s")) {
            return Duration.ofSeconds(Long.parseLong(durationStr.substring(0, durationStr.length() - 1)));
        } else if (durationStr.endsWith("m")) {
            return Duration.ofMinutes(Long.parseLong(durationStr.substring(0, durationStr.length() - 1)));
        } else if (durationStr.endsWith("h")) {
            return Duration.ofHours(Long.parseLong(durationStr.substring(0, durationStr.length() - 1)));
        } else if (durationStr.endsWith("d")) {
            return Duration.ofDays(Long.parseLong(durationStr.substring(0, durationStr.length() - 1)));
        }
        return Duration.parse(durationStr);
    }
}

package org.codewithzea.trackerboost.cache;


import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

@RestController
@RequestMapping("/api/v1/cache")
@RequiredArgsConstructor
public class CacheMonitoringController {

    private final CacheManager cacheManager;

    @GetMapping("/stats")
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();

        cacheManager.getCacheNames().forEach(name -> {
            Cache springCache = cacheManager.getCache(name);
            if (springCache != null) {
                Object nativeCache = springCache.getNativeCache();

                if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache) {
                    com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache =
                            (com.github.benmanes.caffeine.cache.Cache<?, ?>) nativeCache;

                    CacheStats cacheStats = caffeineCache.stats();
                    stats.put(name, Map.of(
                            "size", caffeineCache.estimatedSize(),
                            "hitRate", cacheStats.hitRate(),
                            "missRate", cacheStats.missRate(),
                            "loadSuccessCount", cacheStats.loadSuccessCount(),
                            "loadFailureCount", cacheStats.loadFailureCount(),
                            "totalLoadTime", cacheStats.totalLoadTime(),
                            "evictionCount", cacheStats.evictionCount()
                    ));
                } else if (nativeCache instanceof ConcurrentMap) {
                    // Handle simple ConcurrentMap-based caches
                    ConcurrentMap<?, ?> mapCache = (ConcurrentMap<?, ?>) nativeCache;
                    stats.put(name, Map.of(
                            "size", mapCache.size(),
                            "type", "ConcurrentMap"
                    ));
                } else {
                    stats.put(name, Map.of(
                            "type", nativeCache.getClass().getSimpleName(),
                            "message", "Statistics not available for this cache type"
                    ));
                }
            }
        });

        return stats;
    }

    @GetMapping("/config")
    public Map<String, Object> getCacheConfigurations() {
        Map<String, Object> configs = new HashMap<>();

        if (cacheManager instanceof CaffeineCacheManager caffeineManager) {
            caffeineManager.getCacheNames().forEach(name -> {
                Cache cache = caffeineManager.getCache(name);
                if (cache != null) {
                    Object nativeCache = cache.getNativeCache();

                    if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache) {
                        Map<String, Object> cacheConfig = new LinkedHashMap<>();

                        // Basic info
                        cacheConfig.put("estimatedSize", caffeineCache.estimatedSize());

                        // Expiration policies
                        caffeineCache.policy().expireAfterWrite().ifPresent(expiration ->
                                cacheConfig.put("expireAfterWrite", formatDuration(expiration.getExpiresAfter())));

                        caffeineCache.policy().expireAfterAccess().ifPresent(expiration ->
                                cacheConfig.put("expireAfterAccess", formatDuration(expiration.getExpiresAfter())));

                        caffeineCache.policy().refreshAfterWrite().ifPresent(refresh -> {
                            // Get refresh duration through reflection as API doesn't expose it directly
                            try {
                                Field durationField = refresh.getClass().getDeclaredField("duration");
                                durationField.setAccessible(true);
                                Duration duration = (Duration) durationField.get(refresh);
                                cacheConfig.put("refreshAfterWrite", formatDuration(duration));
                            } catch (Exception e) {
                                cacheConfig.put("refreshAfterWrite", "unknown");
                            }
                        });
                        // Eviction policy
                        caffeineCache.policy().eviction().ifPresent(eviction -> {
                            cacheConfig.put("maximumSize", eviction.getMaximum());
                            cacheConfig.put("weightedSize", eviction.weightedSize().orElse(-1L));
                        });

                        // Stats
                        cacheConfig.put("statsEnabled", caffeineCache.policy().isRecordingStats());

                        configs.put(name, cacheConfig);
                    }
                }
            });
        }

        return configs;
    }

    private String formatDuration(Duration duration) {
        if (duration == null) return "null";

        long seconds = duration.getSeconds();
        if (seconds < 60) return seconds + "s";
        if (seconds < 3600) return seconds / 60 + "m";
        if (seconds < 86400) return seconds / 3600 + "h";
        return seconds / 86400 + "d";
    }

    @PostMapping("/clear/{cacheName}")
    public String clearCache(@PathVariable String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            return "Cache '" + cacheName + "' cleared successfully";
        }
        return "Cache '" + cacheName + "' not found";
    }

    @PostMapping("/clear-all")
    public String clearAllCaches() {
        cacheManager.getCacheNames().forEach(name -> {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        });
        return "All caches cleared successfully";
    }
}

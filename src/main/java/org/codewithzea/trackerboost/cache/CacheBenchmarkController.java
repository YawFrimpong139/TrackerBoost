package org.codewithzea.trackerboost.cache;



import org.codewithzea.trackerboost.task.TaskService;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/benchmark/cache")
public class CacheBenchmarkController {

    private final TaskService taskService;
    private final CacheManager cacheManager;

    public CacheBenchmarkController(TaskService taskService,
                                    CacheManager cacheManager) {
        this.taskService = taskService;
        this.cacheManager = cacheManager;
    }

    @GetMapping("/performance")
    public Map<String, Object> benchmarkCachePerformance() {
        // Warm up
        taskService.getTaskById(1L);

        Map<String, Object> results = new HashMap<>();

        // Without cache
        long start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            taskService.getTaskById(1L);
        }
        results.put("withoutCacheNanos", System.nanoTime() - start);

        // With cache
        start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            taskService.getTaskById(1L);
        }
        results.put("withCacheNanos", System.nanoTime() - start);

        // Add cache statistics
        results.put("cacheStats", getCacheStatistics());

        return results;
    }

    private Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();

        cacheManager.getCacheNames().forEach(cacheName -> {
            if (cacheManager.getCache(cacheName) != null) {
                stats.put(cacheName + "_size", cacheManager.getCache(cacheName).getNativeCache());
            }
        });

        return stats;
    }
}

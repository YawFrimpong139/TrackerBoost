package org.codewithzea.trackerboost.optimize;



import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;


import java.util.concurrent.atomic.AtomicLong;

@Component
public class TaskMetrics {
    private final AtomicLong tasksProcessed = new AtomicLong(0);
    private final Counter taskCreationCounter;
    private final Counter taskCompletionCounter;
    private final Timer taskProcessingTimer;
    private final DistributionSummary taskSizeSummary;
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;

    public TaskMetrics(MeterRegistry registry) {
        Tags commonTags = Tags.of("application", "project-tracker");

        this.taskCreationCounter = Counter.builder("tasks.created")
                .tags(commonTags)
                .register(registry);

        this.taskCompletionCounter = Counter.builder("tasks.completed")
                .tags(commonTags)
                .register(registry);

        this.taskProcessingTimer = Timer.builder("tasks.processing.time")
                .tags(commonTags)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        this.taskSizeSummary = DistributionSummary.builder("tasks.size")
                .tags(commonTags)
                .baseUnit("bytes")
                .register(registry);

        this.cacheHitCounter = Counter.builder("tasks.cache.hits")
                .tags(commonTags)
                .register(registry);

        this.cacheMissCounter = Counter.builder("tasks.cache.misses")
                .tags(commonTags)
                .register(registry);

        Gauge.builder("tasks.processed.total", tasksProcessed, AtomicLong::get)
                .tags(commonTags)
                .register(registry);
    }

    public void incrementTaskCreated() {
        tasksProcessed.incrementAndGet();
        taskCreationCounter.increment();
    }

    public void incrementTaskCompleted() {
        taskCompletionCounter.increment();
    }

    public void incrementCacheHit() {
        cacheHitCounter.increment();
    }

    public void incrementCacheMiss() {
        cacheMissCounter.increment();
    }

    public Timer.Sample startProcessingTimer() {
        return Timer.start();
    }



    public void stopProcessingTimer(Timer.Sample sample) {
        sample.stop(taskProcessingTimer);
    }

    public void recordTaskSize(long bytes) {
        taskSizeSummary.record(bytes);
    }
}

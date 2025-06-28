package org.codewithzea.trackerboost.optimize;



import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.stats.StatsCounter;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import io.micrometer.core.instrument.*;

import java.util.concurrent.TimeUnit;

public class InstrumentedStatsCounter implements StatsCounter {
    private final Counter hitCounter;
    private final Counter missCounter;
    private final Counter loadSuccessCounter;
    private final Counter loadFailureCounter;
    private final Timer loadTimer;
    private final Counter evictionCounter;
    private final DistributionSummary loadPenaltySummary;

    public InstrumentedStatsCounter(MeterRegistry registry, String cacheName) {
        Tags tags = Tags.of("cache", cacheName);

        this.hitCounter = Counter.builder("cache.requests")
                .tags(tags.and("result", "hit"))
                .register(registry);

        this.missCounter = Counter.builder("cache.requests")
                .tags(tags.and("result", "miss"))
                .register(registry);

        this.loadSuccessCounter = Counter.builder("cache.loads")
                .tags(tags.and("result", "success"))
                .register(registry);

        this.loadFailureCounter = Counter.builder("cache.loads")
                .tags(tags.and("result", "failure"))
                .register(registry);

        this.loadTimer = Timer.builder("cache.load.duration")
                .tags(tags)
                .register(registry);

        this.evictionCounter = Counter.builder("cache.evictions")
                .tags(tags)
                .register(registry);

        this.loadPenaltySummary = DistributionSummary.builder("cache.load.penalty")
                .tags(tags)
                .baseUnit("milliseconds")
                .register(registry);
    }

    @Override
    public void recordHits(int count) {
        hitCounter.increment(count);
    }

    @Override
    public void recordMisses(int count) {
        missCounter.increment(count);
    }

    @Override
    public void recordLoadSuccess(long loadTimeNanos) {
        loadSuccessCounter.increment();
        loadTimer.record(loadTimeNanos, TimeUnit.NANOSECONDS);
        loadPenaltySummary.record(TimeUnit.NANOSECONDS.toMillis(loadTimeNanos));
    }

    @Override
    public void recordLoadFailure(long loadTimeNanos) {
        loadFailureCounter.increment();
        loadTimer.record(loadTimeNanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public void recordEviction(int weight, RemovalCause cause) {
        evictionCounter.increment();
    }


    @Override
    public CacheStats snapshot() {
        return CacheStats.of(
                (long) hitCounter.count(),
                (long) missCounter.count(),
                (long) loadSuccessCounter.count(),
                (long) loadFailureCounter.count(),
                (long) loadTimer.totalTime(TimeUnit.NANOSECONDS),
                (long) evictionCounter.count(),
                0L
        );
    }
}

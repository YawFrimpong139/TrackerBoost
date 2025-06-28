package org.codewithzea.trackerboost.optimize;

public record BenchmarkResult(
        String testName,
        long originalTimeNanos,
        long optimizedTimeNanos,
        long originalSizeBytes,
        long optimizedSizeBytes
) {
    public double getImprovementPercentage() {
        return 100.0 * (originalTimeNanos - optimizedTimeNanos) / originalTimeNanos;
    }
}

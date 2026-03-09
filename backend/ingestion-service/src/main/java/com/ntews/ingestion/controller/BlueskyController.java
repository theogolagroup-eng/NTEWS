package com.ntews.ingestion.controller;

import com.ntews.ingestion.model.UnifiedPost;
import com.ntews.ingestion.service.BlueskyJetstreamService;
import com.ntews.ingestion.service.BlueskyMetricsAggregator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for Bluesky integration endpoints
 * Provides monitoring and metrics for the NTEWS dashboard
 */
@RestController
@RequestMapping("/api/bluesky")
@RequiredArgsConstructor
@Slf4j
public class BlueskyController {

    private final BlueskyJetstreamService jetstreamService;
    private final BlueskyMetricsAggregator metricsAggregator;

    /**
     * Get metrics summary for dashboard
     */
    @GetMapping("/metrics/summary")
    public ResponseEntity<Map<String, Object>> getMetricsSummary() {
        try {
            Map<String, Object> summary = metricsAggregator.getMetricsSummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error getting metrics summary: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get top posts by engagement (for threat prioritization)
     */
    @GetMapping("/metrics/top-posts")
    public ResponseEntity<List<Map<String, Object>>> getTopPosts(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> topPosts = metricsAggregator.getTopPostsByEngagement(limit);
            return ResponseEntity.ok(topPosts);
        } catch (Exception e) {
            log.error("Error getting top posts: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get trending posts (high engagement, recent)
     */
    @GetMapping("/metrics/trending")
    public ResponseEntity<List<Map<String, Object>>> getTrendingPosts(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> trendingPosts = metricsAggregator.getTrendingPosts(limit);
            return ResponseEntity.ok(trendingPosts);
        } catch (Exception e) {
            log.error("Error getting trending posts: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get metrics for a specific post
     */
    @GetMapping("/metrics/post/{postId}")
    public ResponseEntity<UnifiedPost.PostMetrics> getPostMetrics(@PathVariable String postId) {
        try {
            UnifiedPost.PostMetrics metrics = metricsAggregator.getPostMetrics(postId);
            if (metrics != null) {
                return ResponseEntity.ok(metrics);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting post metrics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get cache statistics for monitoring
     */
    @GetMapping("/cache/stats")
    public ResponseEntity<Map<String, Object>> getCacheStatistics() {
        try {
            Map<String, Object> stats = metricsAggregator.getCacheStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting cache statistics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check for Bluesky integration
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            Map<String, Object> health = Map.of(
                "status", "UP",
                "service", "bluesky-jetstream",
                "timestamp", java.time.LocalDateTime.now(),
                "cacheSize", metricsAggregator.getAllMetrics().size()
            );
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("Error during health check: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "DOWN",
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Force cleanup of old metrics (admin endpoint)
     */
    @PostMapping("/admin/cleanup")
    public ResponseEntity<Map<String, Object>> forceCleanup() {
        try {
            metricsAggregator.cleanupOldMetrics();
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Cleanup completed",
                "timestamp", java.time.LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.error("Error during cleanup: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "ERROR",
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Force metrics status logging (admin endpoint)
     */
    @PostMapping("/admin/status")
    public ResponseEntity<Map<String, Object>> forceStatusLog() {
        try {
            metricsAggregator.logMetricsStatus();
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Metrics status logged",
                "timestamp", java.time.LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.error("Error during status logging: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "ERROR",
                "error", e.getMessage()
            ));
        }
    }
}

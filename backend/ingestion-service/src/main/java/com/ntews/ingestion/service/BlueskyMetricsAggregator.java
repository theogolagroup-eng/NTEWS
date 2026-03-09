package com.ntews.ingestion.service;

import com.ntews.ingestion.model.UnifiedPost;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Metrics aggregation service for Bluesky posts
 * Optimized for 8GB laptop with efficient caching and batch processing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BlueskyMetricsAggregator {

        
    // In-memory cache for active posts (limited size for memory management)
    private final Map<String, UnifiedPost.PostMetrics> activePosts = new ConcurrentHashMap<>();
    
    // Maximum number of posts to keep in memory (memory optimization)
    private static final int MAX_CACHE_SIZE = 10000;
    
    // Metrics persistence interval
    private static final int PERSISTENCE_INTERVAL_MINUTES = 5;
    
    /**
     * Update metrics for a post when a like event occurs
     */
    public void incrementLikes(String postUri) {
        activePosts.compute(postUri, (uri, metrics) -> {
            if (metrics == null) {
                metrics = UnifiedPost.PostMetrics.builder()
                    .likes(0)
                    .reposts(0)
                    .replies(0)
                    .quotes(0)
                    .lastUpdated(LocalDateTime.now())
                    .build();
            }
            metrics.setLikes(metrics.getLikes() + 1);
            metrics.setLastUpdated(LocalDateTime.now());
            return metrics;
        });
        
        log.debug("❤️ Like incremented for post: {}", postUri);
    }
    
    /**
     * Update metrics for a post when a repost event occurs
     */
    public void incrementReposts(String postUri) {
        activePosts.compute(postUri, (uri, metrics) -> {
            if (metrics == null) {
                metrics = UnifiedPost.PostMetrics.builder()
                    .likes(0)
                    .reposts(0)
                    .replies(0)
                    .quotes(0)
                    .lastUpdated(LocalDateTime.now())
                    .build();
            }
            metrics.setReposts(metrics.getReposts() + 1);
            metrics.setLastUpdated(LocalDateTime.now());
            return metrics;
        });
        
        log.debug("🔄 Repost incremented for post: {}", postUri);
    }
    
    /**
     * Update metrics for a post when a reply event occurs
     */
    public void incrementReplies(String postUri) {
        activePosts.compute(postUri, (uri, metrics) -> {
            if (metrics == null) {
                metrics = UnifiedPost.PostMetrics.builder()
                    .likes(0)
                    .reposts(0)
                    .replies(0)
                    .quotes(0)
                    .lastUpdated(LocalDateTime.now())
                    .build();
            }
            metrics.setReplies(metrics.getReplies() + 1);
            metrics.setLastUpdated(LocalDateTime.now());
            return metrics;
        });
        
        log.debug("💬 Reply incremented for post: {}", postUri);
    }
    
    /**
     * Update metrics for a post when a quote event occurs
     */
    public void incrementQuotes(String postUri) {
        activePosts.compute(postUri, (uri, metrics) -> {
            if (metrics == null) {
                metrics = UnifiedPost.PostMetrics.builder()
                    .likes(0)
                    .reposts(0)
                    .replies(0)
                    .quotes(0)
                    .lastUpdated(LocalDateTime.now())
                    .build();
            }
            metrics.setQuotes(metrics.getQuotes() + 1);
            metrics.setLastUpdated(LocalDateTime.now());
            return metrics;
        });
        
        log.debug("📝 Quote incremented for post: {}", postUri);
    }
    
    /**
     * Initialize metrics for a new post
     */
    public void initializePostMetrics(String postUri) {
        activePosts.putIfAbsent(postUri, UnifiedPost.PostMetrics.builder()
            .likes(0)
            .reposts(0)
            .replies(0)
            .quotes(0)
            .lastUpdated(LocalDateTime.now())
            .build());
        
        // Manage cache size to prevent memory overflow
        if (activePosts.size() > MAX_CACHE_SIZE) {
            evictOldestPosts();
        }
    }
    
    /**
     * Get metrics for a specific post
     */
    public UnifiedPost.PostMetrics getPostMetrics(String postUri) {
        return activePosts.get(postUri);
    }
    
    /**
     * Get all cached metrics
     */
    public Map<String, UnifiedPost.PostMetrics> getAllMetrics() {
        return new HashMap<>(activePosts);
    }
    
    /**
     * Get top posts by engagement (for threat prioritization)
     */
    public List<Map<String, Object>> getTopPostsByEngagement(int limit) {
        return activePosts.entrySet().stream()
            .map(entry -> {
                Map<String, Object> item = new HashMap<>();
                item.put("postUri", entry.getKey());
                item.put("metrics", entry.getValue());
                item.put("engagementScore", calculateEngagementScore(entry.getValue()));
                return item;
            })
            .sorted((a, b) -> Double.compare(
                (Double) b.get("engagementScore"), 
                (Double) a.get("engagementScore")
            ))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Calculate engagement score for threat prioritization
     */
    private double calculateEngagementScore(UnifiedPost.PostMetrics metrics) {
        // Weighted engagement calculation
        double score = metrics.getLikes() * 1.0 +
                      metrics.getReposts() * 2.0 +  // Reposts are more valuable
                      metrics.getReplies() * 1.5 +    // Replies show engagement
                      metrics.getQuotes() * 2.5;     // Quotes are highest value
        
        // Add time decay factor (recent posts get higher score)
        long minutesSinceUpdate = java.time.Duration.between(
            metrics.getLastUpdated(), 
            LocalDateTime.now()
        ).toMinutes();
        
        double timeDecay = Math.exp(-minutesSinceUpdate / 60.0); // Decay over hours
        
        return score * timeDecay;
    }
    
    /**
     * Get trending posts (high engagement, recent)
     */
    public List<Map<String, Object>> getTrendingPosts(int limit) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        
        return activePosts.entrySet().stream()
            .filter(entry -> entry.getValue().getLastUpdated().isAfter(oneHourAgo))
            .map(entry -> {
                Map<String, Object> item = new HashMap<>();
                item.put("postUri", entry.getKey());
                item.put("metrics", entry.getValue());
                item.put("engagementScore", calculateEngagementScore(entry.getValue()));
                return item;
            })
            .sorted((a, b) -> Double.compare(
                (Double) b.get("engagementScore"), 
                (Double) a.get("engagementScore")
            ))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Get metrics summary for dashboard
     */
    public Map<String, Object> getMetricsSummary() {
        int totalPosts = activePosts.size();
        int totalLikes = activePosts.values().stream()
            .mapToInt(UnifiedPost.PostMetrics::getLikes)
            .sum();
        int totalReposts = activePosts.values().stream()
            .mapToInt(UnifiedPost.PostMetrics::getReposts)
            .sum();
        int totalReplies = activePosts.values().stream()
            .mapToInt(UnifiedPost.PostMetrics::getReplies)
            .sum();
        int totalQuotes = activePosts.values().stream()
            .mapToInt(UnifiedPost.PostMetrics::getQuotes)
            .sum();
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalPosts", totalPosts);
        summary.put("totalLikes", totalLikes);
        summary.put("totalReposts", totalReposts);
        summary.put("totalReplies", totalReplies);
        summary.put("totalQuotes", totalQuotes);
        summary.put("averageEngagement", totalPosts > 0 ? 
            (double) (totalLikes + totalReposts + totalReplies + totalQuotes) / totalPosts : 0.0);
        summary.put("lastUpdated", LocalDateTime.now());
        
        return summary;
    }
    
    /**
     * Scheduled task to log metrics status
     * Runs every 5 minutes to monitor performance
     */
    @Scheduled(fixedDelay = PERSISTENCE_INTERVAL_MINUTES * 60 * 1000)
    public void logMetricsStatus() {
        try {
            log.info("� Current metrics status: {} active posts, {} max cache size", 
                activePosts.size(), MAX_CACHE_SIZE);
            
            // Trigger cleanup if needed
            if (activePosts.size() > MAX_CACHE_SIZE * 0.9) {
                log.info("🧹 Cache approaching limit, triggering cleanup");
                evictOldestPosts();
            }
            
        } catch (Exception e) {
            log.error("❌ Error logging metrics status: {}", e.getMessage());
        }
    }
    
        
    /**
     * Evict oldest posts to manage memory usage
     */
    private void evictOldestPosts() {
        int evictCount = MAX_CACHE_SIZE / 10; // Evict 10% of cache
        
        List<Map.Entry<String, UnifiedPost.PostMetrics>> oldestPosts = activePosts.entrySet().stream()
            .sorted((a, b) -> a.getValue().getLastUpdated().compareTo(b.getValue().getLastUpdated()))
            .limit(evictCount)
            .collect(Collectors.toList());
        
        for (Map.Entry<String, UnifiedPost.PostMetrics> entry : oldestPosts) {
            activePosts.remove(entry.getKey());
        }
        
        log.info("🗑️ Evicted {} oldest posts to manage memory", evictCount);
    }
    
    /**
     * Clean up old metrics (scheduled task)
     */
    @Scheduled(fixedDelay = 60 * 60 * 1000) // Every hour
    public void cleanupOldMetrics() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusHours(6); // Keep 6 hours of data
            
            int beforeCount = activePosts.size();
            
            activePosts.entrySet().removeIf(entry -> 
                entry.getValue().getLastUpdated().isBefore(cutoff)
            );
            
            int afterCount = activePosts.size();
            log.info("🧹 Cleaned up {} old metrics ({} -> {})", 
                beforeCount - afterCount, beforeCount, afterCount);
            
        } catch (Exception e) {
            log.error("❌ Error during metrics cleanup: {}", e.getMessage());
        }
    }
    
    /**
     * Get cache statistics for monitoring
     */
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheSize", activePosts.size());
        stats.put("maxCacheSize", MAX_CACHE_SIZE);
        stats.put("memoryUsagePercent", (double) activePosts.size() / MAX_CACHE_SIZE * 100);
        stats.put("lastCleanup", LocalDateTime.now());
        
        return stats;
    }
}

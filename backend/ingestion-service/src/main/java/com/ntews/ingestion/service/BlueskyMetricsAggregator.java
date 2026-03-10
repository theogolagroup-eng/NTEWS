package com.ntews.ingestion.service;

import com.ntews.ingestion.model.UnifiedPost;
import com.ntews.ingestion.repository.ProcessedPostRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class BlueskyMetricsAggregator {
    
    private static final Logger log = LoggerFactory.getLogger(BlueskyMetricsAggregator.class);

    private final ProcessedPostRepository processedPostRepository;
        
    // In-memory cache for active posts (limited size for memory management)
    private final Map<String, UnifiedPost.PostMetrics> activePosts = new ConcurrentHashMap<>();
    
    // Cache for full post data (needed for frontend display)
    private final Map<String, UnifiedPost> fullPosts = new ConcurrentHashMap<>();
    
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
            metrics.likes = metrics.likes + 1;
            metrics.lastUpdated = LocalDateTime.now();
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
            metrics.reposts = metrics.reposts + 1;
            metrics.lastUpdated = LocalDateTime.now();
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
            metrics.replies = metrics.replies + 1;
            metrics.lastUpdated = LocalDateTime.now();
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
            metrics.quotes = metrics.quotes + 1;
            metrics.lastUpdated = LocalDateTime.now();
            return metrics;
        });
        
        log.debug("📝 Quote incremented for post: {}", postUri);
    }
    
    /**
     * Store a full post with threat level (needed for frontend display)
     */
    public void storePost(UnifiedPost post, String threatLevel) {
        log.debug("🚨 SECURITY POST STORED: ID={}, Threat={}, Length={}", 
                post.id, threatLevel, post.text != null ? post.text.length() : 0);
        fullPosts.put(post.id, post);
        
        // Also initialize metrics for this post
        initializePostMetrics(post.id);
        
        // Manage cache size based on threat level
        int maxSize = "high".equals(threatLevel) ? 1000 : 
                     "medium".equals(threatLevel) ? 5000 : 10000;
                     
        if (fullPosts.size() > maxSize) {
            evictOldestFullPosts();
        }
        
        log.info("📊 Cache size after storing: {} | Threat level: {}", fullPosts.size(), threatLevel);
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
        // Combine posts from both sources: local cache + processed repository
        Map<String, UnifiedPost> allPosts = new HashMap<>(fullPosts);
        allPosts.putAll(processedPostRepository.getAllPosts());
        
        return allPosts.entrySet().stream()
            .map(entry -> {
                UnifiedPost post = entry.getValue();
                Map<String, Object> item = new HashMap<>();
                
                // Post details
                item.put("postUri", post.id);
                item.put("id", post.id);
                item.put("author", post.author);
                item.put("author_handle", post.authorHandle);
                item.put("text", post.text);
                item.put("cleaned_text", post.cleanedText);
                item.put("timestamp", post.timestamp);
                item.put("url", post.url);
                item.put("language", post.language);
                item.put("hashtags", post.hashtags);
                item.put("threat_level", post.metadata != null ? post.metadata.get("threat_level") : "unknown");
                
                // Metrics
                UnifiedPost.PostMetrics metrics = activePosts.get(post.id);
                if (metrics != null) {
                    item.put("metrics", Map.of(
                        "likes", metrics.likes,
                        "reposts", metrics.reposts,
                        "replies", metrics.replies,
                        "quotes", metrics.quotes,
                        "engagement_rate", calculateEngagementScore(metrics)
                    ));
                } else {
                    item.put("metrics", Map.of(
                        "likes", 0, "reposts", 0, "replies", 0, "quotes", 0
                    ));
                }
                
                // Include comprehensive metadata from post
                if (post.metadata != null) {
                    post.metadata.forEach((key, value) -> {
                        if (!key.equals("ai_analysis")) {
                            item.put(key, value);
                        }
                    });
                }
                
                // AI Analysis (from metadata field)
                Map<String, Object> aiAnalysis = post.metadata != null ? 
                    (Map<String, Object>) post.metadata.get("ai_analysis") : null;
                    
                if (aiAnalysis != null) {
                    item.put("risk_score", aiAnalysis.getOrDefault("risk_score", 0.0));
                    item.put("risk_category", aiAnalysis.getOrDefault("risk_category", "low"));
                    item.put("threat_keywords", aiAnalysis.getOrDefault("threat_keywords", new ArrayList<>()));
                    item.put("confidence", aiAnalysis.getOrDefault("confidence", 0.0));
                    item.put("classification", aiAnalysis.getOrDefault("classification", "neutral"));
                } else {
                    item.put("risk_score", 0.3); // Default risk score for demonstration
                    item.put("risk_category", "medium");
                    item.put("threat_keywords", List.of("bluesky", "threat"));
                    item.put("confidence", 0.7);
                    item.put("classification", "suspicious");
                }
                
                item.put("engagementScore", calculateEngagementScore(
                    metrics != null ? metrics : UnifiedPost.PostMetrics.builder().build()
                ));
                
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
        double score = metrics.likes * 1.0 +
                      metrics.reposts * 2.0 +  // Reposts are more valuable
                      metrics.replies * 1.5 +    // Replies show engagement
                      metrics.quotes * 2.5;     // Quotes are highest value
        
        // Add time decay factor (recent posts get higher score)
        long minutesSinceUpdate = java.time.Duration.between(
            metrics.lastUpdated, 
            LocalDateTime.now()
        ).toMinutes();
        
        double timeDecay = Math.exp(-minutesSinceUpdate / 60.0); // Decay over hours
        
        return score * timeDecay;
    }
    
    /**
     * Get trending posts (high engagement, recent)
     */
    public List<Map<String, Object>> getTrendingPosts(int limit) {
        log.info("📊 Getting trending posts: limit={}, localCacheSize={}, repositorySize={}", 
                limit, fullPosts.size(), processedPostRepository.getAllPosts().size());
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        
        // Combine posts from both sources: local cache + processed repository
        Map<String, UnifiedPost> allPosts = new HashMap<>(fullPosts);
        allPosts.putAll(processedPostRepository.getAllPosts());
        
        return allPosts.entrySet().stream()
            .filter(entry -> entry.getValue() != null && entry.getValue().processedAt != null && entry.getValue().processedAt.isAfter(oneHourAgo))
            .map(entry -> {
                UnifiedPost post = entry.getValue();
                Map<String, Object> item = new HashMap<>();
                
                // Post details with null safety for getTrendingPosts
                item.put("postUri", post.id != null ? post.id : "");
                item.put("id", post.id != null ? post.id : "");
                item.put("author", post.author != null ? post.author : "Unknown");
                item.put("author_handle", post.authorHandle != null ? post.authorHandle : "@unknown");
                item.put("text", post.text != null ? post.text : "");
                item.put("cleaned_text", post.cleanedText != null ? post.cleanedText : "");
                item.put("timestamp", post.timestamp != null ? post.timestamp : "");
                item.put("url", post.url != null ? post.url : "");
                item.put("language", post.language != null ? post.language : "en");
                item.put("hashtags", post.hashtags != null ? post.hashtags : new ArrayList<>());
                item.put("threat_level", post.metadata != null ? post.metadata.get("threat_level") : "unknown");
                
                // Metrics
                UnifiedPost.PostMetrics metrics = activePosts.get(post.id);
                if (metrics != null) {
                    item.put("metrics", Map.of(
                        "likes", metrics.likes,
                        "reposts", metrics.reposts,
                        "replies", metrics.replies,
                        "quotes", metrics.quotes,
                        "views", metrics.views,
                        "engagement_rate", calculateEngagementScore(metrics)
                    ));
                } else {
                    item.put("metrics", Map.of(
                        "likes", 0, "reposts", 0, "replies", 0, "quotes", 0, "views", 0
                    ));
                }
                
                // Include comprehensive metadata from post
                if (post.metadata != null) {
                    post.metadata.forEach((key, value) -> {
                        if (!key.equals("ai_analysis")) {
                            item.put(key, value);
                        }
                    });
                }
                
                // AI Analysis (from metadata field)
                Map<String, Object> aiAnalysis = post.metadata != null ? 
                    (Map<String, Object>) post.metadata.get("ai_analysis") : null;
                    
                if (aiAnalysis != null) {
                    item.put("risk_score", aiAnalysis.getOrDefault("risk_score", 0.0));
                    item.put("risk_category", aiAnalysis.getOrDefault("risk_category", "low"));
                    item.put("threat_keywords", aiAnalysis.getOrDefault("threat_keywords", new ArrayList<>()));
                    item.put("confidence", aiAnalysis.getOrDefault("confidence", 0.0));
                    item.put("classification", aiAnalysis.getOrDefault("classification", "neutral"));
                } else {
                    item.put("risk_score", 0.3); // Default risk score for demonstration
                    item.put("risk_category", "medium");
                    item.put("threat_keywords", List.of("bluesky", "threat"));
                    item.put("confidence", 0.7);
                    item.put("classification", "suspicious");
                }
                
                item.put("engagementScore", calculateEngagementScore(
                    metrics != null ? metrics : UnifiedPost.PostMetrics.builder().build()
                ));
                
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
            .mapToInt(metrics -> metrics.likes)
            .sum();
        int totalReposts = activePosts.values().stream()
            .mapToInt(metrics -> metrics.reposts)
            .sum();
        int totalReplies = activePosts.values().stream()
            .mapToInt(metrics -> metrics.replies)
            .sum();
        int totalQuotes = activePosts.values().stream()
            .mapToInt(metrics -> metrics.quotes)
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
            .sorted((a, b) -> a.getValue().lastUpdated.compareTo(b.getValue().lastUpdated))
            .limit(evictCount)
            .collect(Collectors.toList());
        
        for (Map.Entry<String, UnifiedPost.PostMetrics> entry : oldestPosts) {
            activePosts.remove(entry.getKey());
        }
        
        log.info("🗑️ Evicted {} oldest posts to manage memory", evictCount);
    }
    
    /**
     * Evict oldest full posts to manage memory usage
     */
    private void evictOldestFullPosts() {
        int evictCount = MAX_CACHE_SIZE / 10; // Evict 10% of cache
        
        List<Map.Entry<String, UnifiedPost>> oldestPosts = fullPosts.entrySet().stream()
            .sorted((a, b) -> a.getValue().processedAt.compareTo(b.getValue().processedAt))
            .limit(evictCount)
            .collect(Collectors.toList());
        
        for (Map.Entry<String, UnifiedPost> entry : oldestPosts) {
            fullPosts.remove(entry.getKey());
        }
        
        log.info("🗑️ Evicted {} oldest full posts to manage memory", evictCount);
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
                entry.getValue().lastUpdated.isBefore(cutoff)
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

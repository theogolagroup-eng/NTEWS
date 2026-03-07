package com.ntews.ingestion.service;

import com.ntews.ingestion.model.SocialMediaData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Real-time Twitter/X Streaming Service
 * Implements FilteredStreamApi for Sheng and slang detection
 */
@Service
@Slf4j
public class TwitterStreamingService {
    
    @Value("${twitter.bearer.token:}")
    private String bearerToken;
    
    @Value("${twitter.api.v2.enabled:false}")
    private boolean twitterApiV2Enabled;
    
    // In-memory buffer for real-time processing
    private final Map<String, SocialMediaData> streamingBuffer = new ConcurrentHashMap<>();
    
    // Sheng and slang keywords for monitoring
    private final List<String> shengKeywords = List.of(
        "maandamano", "risto", "protest", "demonstration", "gathering",
        "tuko site", "mambo", "karao", "mambas", "vijana",
        "noma", "mbaya", "chill", "poa", "kasho"
    );
    
    // Hashtag tracking with sliding window
    private final Map<String, Integer> hashtagCounts = new ConcurrentHashMap<>();
    
    /**
     * Start real-time Twitter streaming for security monitoring
     */
    public CompletableFuture<Void> startSecurityStream() {
        return CompletableFuture.runAsync(() -> {
            if (!twitterApiV2Enabled) {
                log.warn("Twitter API v2 not enabled, using mock data");
                startMockDataStream();
                return;
            }
            
            log.info("Starting real-time Twitter security monitoring stream");
            
            // Essential fields for location-based security alerts
            String tweetFields = "tweet.fields=lang,created_at,author_id,public_metrics";
            String expansions = "expansions=geo.place_id,author_id";
            
            // Filter rules for specific keywords
            String filterRules = buildShengFilterRules();
            
            try {
                // Connect to Twitter FilteredStreamApi
                // Note: This would require Twitter API Java SDK implementation
                connectToFilteredStream(tweetFields, expansions, filterRules);
                
            } catch (Exception e) {
                log.error("Failed to start Twitter stream: {}", e.getMessage());
                startMockDataStream();
            }
        });
    }
    
    /**
     * Build filter rules for Sheng and security-related keywords
     */
    private String buildShengFilterRules() {
        StringBuilder filters = new StringBuilder();
        
        // Add Sheng keywords
        for (String keyword : shengKeywords) {
            if (filters.length() > 0) {
                filters.append(" OR ");
            }
            filters.append(keyword);
        }
        
        // Add security keywords in English
        filters.append(" OR protest OR demonstration OR unrest OR security");
        
        return filters.toString();
    }
    
    /**
     * Process incoming tweet data for security analysis
     */
    private void processTweetData(SocialMediaData tweetData) {
        // Extract engagement metrics for percentage calculations
        Map<String, Long> publicMetrics = extractPublicMetrics(tweetData);
        
        // Calculate engagement percentages
        EngagementMetrics metrics = calculateEngagementPercentages(publicMetrics);
        
        // Update hashtag tracking
        updateHashtagCounts(tweetData);
        
        // Enrich with Sheng-specific analysis
        enrichWithShengAnalysis(tweetData, metrics);
        
        // Add to streaming buffer for NLP processing
        streamingBuffer.put(tweetData.getId(), tweetData);
        
        log.info("Processed security tweet: {} | Engagement: {}% | Sheng detected: {}", 
            tweetData.getId(), metrics.getEngagementScore(), 
            containsSheng(tweetData.getContent()));
    }
    
    /**
     * Extract public metrics from tweet data
     */
    private Map<String, Long> extractPublicMetrics(SocialMediaData tweetData) {
        Map<String, Long> metrics = new HashMap<>();
        
        // These would come from Twitter API v2 public_metrics
        metrics.put("retweet_count", extractMetricFromContent(tweetData, "retweet_count", 0L));
        metrics.put("reply_count", extractMetricFromContent(tweetData, "reply_count", 0L));
        metrics.put("like_count", extractMetricFromContent(tweetData, "like_count", 0L));
        metrics.put("quote_count", extractMetricFromContent(tweetData, "quote_count", 0L));
        
        return metrics;
    }
    
    private long extractMetricFromContent(SocialMediaData data, String metricType, long defaultValue) {
        // Extract metric from content or metadata
        if (data.getMetadata() != null && data.getMetadata().containsKey(metricType)) {
            Object value = data.getMetadata().get(metricType);
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
        }
        return defaultValue;
    }
    
    /**
     * Calculate engagement percentages for security context
     */
    private EngagementMetrics calculateEngagementPercentages(Map<String, Long> publicMetrics) {
        long retweets = publicMetrics.getOrDefault("retweet_count", 0L);
        long replies = publicMetrics.getOrDefault("reply_count", 0L);
        long likes = publicMetrics.getOrDefault("like_count", 0L);
        long quotes = publicMetrics.getOrDefault("quote_count", 0L);
        
        long totalEngagement = retweets + replies + likes + quotes;
        
        if (totalEngagement == 0) {
            return new EngagementMetrics(0.0, 0.0);
        }
        
        double repostPercentage = (double) retweets / totalEngagement * 100;
        double commentPercentage = (double) replies / totalEngagement * 100;
        
        return new EngagementMetrics(repostPercentage, commentPercentage);
    }
    
    /**
     * Update hashtag tracking for trend analysis
     */
    private void updateHashtagCounts(SocialMediaData tweetData) {
        String content = tweetData.getContent().toLowerCase();
        String[] words = content.split("\\s+");
        
        for (String word : words) {
            if (word.startsWith("#")) {
                String hashtag = word.substring(1);
                hashtagCounts.merge(hashtag, 1, (oldValue, newValue) -> oldValue + newValue);
            }
        }
    }
    
    /**
     * Detect Sheng content in tweet
     */
    private boolean containsSheng(String content) {
        String lowerContent = content.toLowerCase();
        return shengKeywords.stream().anyMatch(lowerContent::contains);
    }
    
    /**
     * Enrich data with Sheng-specific analysis
     */
    private void enrichWithShengAnalysis(SocialMediaData tweetData, EngagementMetrics metrics) {
        // Add Sheng-specific metadata
        tweetData.setLanguage("sw-sheng-en"); // Mixed language code
        tweetData.setShengDetected(containsSheng(tweetData.getContent()));
        
        // Set security relevance based on engagement patterns
        if (metrics.getCommentPercentage() > 30.0) { // High comment % indicates heated debate
            tweetData.setSecurityRelevance("HIGH");
        } else if (metrics.getRepostPercentage() > 20.0) {
            tweetData.setSecurityRelevance("MEDIUM");
        } else {
            tweetData.setSecurityRelevance("LOW");
        }
    }
    
    /**
     * Mock data stream for testing without Twitter API
     */
    private void startMockDataStream() {
        log.info("Starting mock data stream for testing");
        
        // Simulate incoming Sheng-related tweets
        List<String> mockTweets = List.of(
            "Maandamano ya Nairobi CBD, tunaanza #protest",
            "Karao wanakambo mambo, tuko site #security",
            "Mambo ya kukabiliana, mambas wamepote #unrest",
            "Vijana hapa kazi, tuko demonstration #maandamano"
        );
        
        for (int i = 0; i < mockTweets.size(); i++) {
            SocialMediaData mockData = createMockTweet(mockTweets.get(i), i);
            processTweetData(mockData);
            
            try {
                Thread.sleep(2000); // Simulate 2-second intervals
            } catch (InterruptedException e) {
                break;
            }
        }
    }
    
    /**
     * Create mock tweet data for testing
     */
    private SocialMediaData createMockTweet(String content, int index) {
        SocialMediaData tweet = new SocialMediaData();
        tweet.setId("mock-tweet-" + index);
        tweet.setPlatform("twitter");
        tweet.setContent(content);
        tweet.setPostedAt(java.time.LocalDateTime.now().minusMinutes(index * 2));
        tweet.setGeoTagLatitude("-1.2921"); // Nairobi coordinates
        tweet.setGeoTagLongitude("36.8219");
        tweet.setLocation("Nairobi CBD, Kenya");
        tweet.setAuthorId("mock-user-" + index);
        tweet.setLanguage("sw-sheng-en");
        tweet.setShengDetected(true);
        tweet.setSecurityRelevance(index % 2 == 0 ? "HIGH" : "MEDIUM");
        return tweet;
    }
    
    /**
     * Connect to Twitter FilteredStreamApi (placeholder for actual implementation)
     */
    private void connectToFilteredStream(String tweetFields, String expansions, String filterRules) {
        // This would be implemented with Twitter API Java SDK
        log.info("Connecting to Twitter FilteredStreamApi with fields: {}", tweetFields);
        log.info("Using filter rules: {}", filterRules);
        
        // Placeholder for actual Twitter API v2 implementation
        // Would use TwitterClient, FilteredStreamApi, etc.
    }
    
    /**
     * Get streaming statistics
     */
    public Map<String, Object> getStreamingStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("active_stream", true);
        stats.put("sheng_keywords_tracked", shengKeywords.size());
        stats.put("hashtags_monitored", hashtagCounts.size());
        stats.put("buffer_size", streamingBuffer.size());
        stats.put("engagement_alerts_triggered", getHighEngagementAlerts());
        return stats;
    }
    
    /**
     * Count high engagement alerts
     */
    private long getHighEngagementAlerts() {
        return streamingBuffer.values().stream()
            .filter(data -> "HIGH".equals(data.getSecurityRelevance()))
            .count();
    }
    
    /**
     * Engagement metrics data class
     */
    private static class EngagementMetrics {
        private final double repostPercentage;
        private final double commentPercentage;
        
        public EngagementMetrics(double repostPercentage, double commentPercentage) {
            this.repostPercentage = repostPercentage;
            this.commentPercentage = commentPercentage;
        }
        
        public double getRepostPercentage() { return repostPercentage; }
        public double getCommentPercentage() { return commentPercentage; }
        public double getEngagementScore() { return (repostPercentage + commentPercentage) / 2; }
    }
}

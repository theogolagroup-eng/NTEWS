package com.ntews.ingestion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

/**
 * Configuration properties for Bluesky Jetstream integration
 * Optimized settings for 8GB laptop deployment
 */
@Configuration
@ConfigurationProperties(prefix = "bluesky")
@Data
public class BlueskyConfig {
    
    /**
     * Jetstream WebSocket endpoints
     */
    private String jetstreamUrl = "wss://jetstream1.us-west.bsky.network/subscribe";
    private String backupJetstreamUrl = "wss://jetstream2.us-east.bsky.network/subscribe";
    
    /**
     * Connection settings
     */
    private int connectTimeoutSeconds = 30;
    private int pingIntervalSeconds = 30;
    private int readTimeoutSeconds = 0; // No timeout for streaming
    private int reconnectDelaySeconds = 5;
    
    /**
     * Collections to subscribe to
     */
    private String[] collections = {
        "app.bsky.feed.post",
        "app.bsky.feed.like", 
        "app.bsky.feed.repost",
        "app.bsky.graph.follow"
    };
    
    /**
     * Keywords for threat detection (multi-language)
     */
    private String[] threatKeywords = {
        "maandamano", "karao", "wanakambo", "tishio", "usalama",
        "protest", "unrest", "violence", "threat", "danger",
        "flood", "disaster", "emergency", "security", "attack"
    };
    
    /**
     * Target languages for filtering
     */
    private String[] targetLanguages = {"en", "sw", "ke"};
    
    /**
     * Memory optimization settings
     */
    private int maxCacheSize = 10000;
    private int metricsPersistenceIntervalMinutes = 5;
    private int cleanupIntervalHours = 1;
    private int dataRetentionHours = 6;
    
    /**
     * Kafka settings
     */
    private String threatTopic = "threat_analysis_queue";
    private String metricsTopic = "bluesky_metrics";
    
    /**
     * Redis settings
     */
    private String postsKey = "bluesky_posts";
    private String metricsKey = "bluesky_metrics";
    private int redisExpirationHours = 24;
    
    /**
     * Performance tuning
     */
    private boolean enableMetricsAggregation = true;
    private boolean enableKeywordFiltering = true;
    private boolean enableLanguageFiltering = true;
    private int batchSize = 100;
    private int maxConcurrentConnections = 3;
    
    /**
     * Monitoring and logging
     */
    private boolean enableDetailedLogging = false;
    private int logSampleRate = 100; // Log every 100th message
    private boolean enableMetricsReporting = true;
}

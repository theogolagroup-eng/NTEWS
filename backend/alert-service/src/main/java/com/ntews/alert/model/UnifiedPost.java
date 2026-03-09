package com.ntews.alert.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Unified post model for cross-service communication
 * Matches the structure used by ingestion service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedPost {
    
    private String id;
    private String source;
    private String sourceType;
    private String text;
    private LocalDateTime timestamp;
    private Map<String, Object> metadata;
    private String location;
    private float confidence;
    private String severity;
    
    // Nested metrics class
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostMetrics {
        private int likes;
        private int reposts;
        private int replies;
        private int quotes;
        private double engagementScore;
        private LocalDateTime lastUpdated;
    }
}

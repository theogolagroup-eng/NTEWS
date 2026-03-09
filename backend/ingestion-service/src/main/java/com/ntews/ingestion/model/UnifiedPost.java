package com.ntews.ingestion.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Unified schema for posts from different social media platforms
 * This ensures the AI engine receives consistent data regardless of source
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UnifiedPost {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("source")
    private String source; // "bluesky", "twitter", "reddit", etc.
    
    @JsonProperty("author")
    private String author;
    
    @JsonProperty("author_handle")
    private String authorHandle;
    
    @JsonProperty("text")
    private String text;
    
    @JsonProperty("cleaned_text")
    private String cleanedText;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("url")
    private String url;
    
    @JsonProperty("language")
    private String language;
    
    @JsonProperty("hashtags")
    private List<String> hashtags;
    
    @JsonProperty("mentions")
    private List<String> mentions;
    
    @JsonProperty("metrics")
    private PostMetrics metrics;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    @JsonProperty("processed_at")
    private LocalDateTime processedAt;
    
    /**
     * Engagement metrics for the post
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PostMetrics {
        @JsonProperty("likes")
        private Integer likes;
        
        @JsonProperty("reposts")
        private Integer reposts;
        
        @JsonProperty("replies")
        private Integer replies;
        
        @JsonProperty("quotes")
        private Integer quotes;
        
        @JsonProperty("views")
        private Integer views;
        
        @JsonProperty("engagement_rate")
        private Double engagementRate;
        
        @JsonProperty("last_updated")
        private LocalDateTime lastUpdated;
    }
    
    /**
     * Platform-specific metadata
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BlueskyMetadata {
        @JsonProperty("uri")
        private String uri;
        
        @JsonProperty("cid")
        private String cid;
        
        @JsonProperty("author_did")
        private String authorDid;
        
        @JsonProperty("reply_count")
        private Integer replyCount;
        
        @JsonProperty("embed_images")
        private List<String> embedImages;
        
        @JsonProperty("embed_external")
        private Map<String, Object> embedExternal;
    }
    
    /**
     * Check if post contains any of the target keywords
     */
    public boolean containsKeywords(List<String> keywords) {
        if (text == null || keywords == null || keywords.isEmpty()) {
            return false;
        }
        
        String lowerText = text.toLowerCase();
        return keywords.stream().anyMatch(lowerText::contains);
    }
    
    /**
     * Check if post is in target language
     */
    public boolean isTargetLanguage(List<String> targetLanguages) {
        if (language == null || targetLanguages == null || targetLanguages.isEmpty()) {
            return true; // Assume target if unknown
        }
        
        return targetLanguages.contains(language.toLowerCase());
    }
}

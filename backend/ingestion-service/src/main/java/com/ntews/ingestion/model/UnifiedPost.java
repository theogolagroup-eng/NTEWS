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
// TODO: Lombok disabled - using manual implementation
public class UnifiedPost {
    
    @JsonProperty("id")
    public String id;
    
    @JsonProperty("source")
    private String source; // "bluesky", "twitter", "reddit", etc.
    
    @JsonProperty("author")
    public String author;
    
    @JsonProperty("author_handle")
    public String authorHandle;
    
    @JsonProperty("text")
    public String text;
    
    @JsonProperty("cleaned_text")
    public String cleanedText;
    
    @JsonProperty("timestamp")
    public LocalDateTime timestamp;
    
    @JsonProperty("url")
    public String url;
    
    @JsonProperty("language")
    public String language;
    
    @JsonProperty("hashtags")
    public List<String> hashtags;
    
    @JsonProperty("mentions")
    private List<String> mentions;
    
    @JsonProperty("metrics")
    private PostMetrics metrics;
    
    @JsonProperty("metadata")
    public Map<String, Object> metadata;
    
    @JsonProperty("processed_at")
    public LocalDateTime processedAt;
    
    /**
     * Engagement metrics for the post
     */
    // TODO: Lombok disabled - using manual implementation
    public static class PostMetrics {
        @JsonProperty("likes")
        public Integer likes;
        
        @JsonProperty("reposts")
        public Integer reposts;
        
        @JsonProperty("replies")
        public Integer replies;
        
        @JsonProperty("quotes")
        public Integer quotes;
        
        @JsonProperty("views")
        public Integer views;
        
        @JsonProperty("engagement_rate")
        public Double engagementRate;
        
        @JsonProperty("last_updated")
        public LocalDateTime lastUpdated;
        
        // Manual constructor
        public PostMetrics() {}
        
        public PostMetrics(Integer likes, Integer reposts, Integer replies, Integer quotes, 
                          Integer views, Double engagementRate, LocalDateTime lastUpdated) {
            this.likes = likes;
            this.reposts = reposts;
            this.replies = replies;
            this.quotes = quotes;
            this.views = views;
            this.engagementRate = engagementRate;
            this.lastUpdated = lastUpdated;
        }
        
        // Manual builder pattern
        public static PostMetricsBuilder builder() {
            return new PostMetricsBuilder();
        }
        
        public static class PostMetricsBuilder {
            private Integer likes;
            private Integer reposts;
            private Integer replies;
            private Integer quotes;
            private Integer views;
            private Double engagementRate;
            private LocalDateTime lastUpdated;
            
            public PostMetricsBuilder likes(Integer likes) {
                this.likes = likes;
                return this;
            }
            
            public PostMetricsBuilder reposts(Integer reposts) {
                this.reposts = reposts;
                return this;
            }
            
            public PostMetricsBuilder replies(Integer replies) {
                this.replies = replies;
                return this;
            }
            
            public PostMetricsBuilder quotes(Integer quotes) {
                this.quotes = quotes;
                return this;
            }
            
            public PostMetricsBuilder lastUpdated(LocalDateTime lastUpdated) {
                this.lastUpdated = lastUpdated;
                return this;
            }
            
            public PostMetrics build() {
                return new PostMetrics(likes, reposts, replies, quotes, views, engagementRate, lastUpdated);
            }
        }
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
    
    // Manual constructors
    public UnifiedPost() {}
    
    public UnifiedPost(String id, String source, String author, String authorHandle, String text, 
                       String cleanedText, LocalDateTime timestamp, String url, String language,
                       List<String> hashtags, List<String> mentions, PostMetrics metrics,
                       Map<String, Object> metadata, LocalDateTime processedAt) {
        this.id = id;
        this.source = source;
        this.author = author;
        this.authorHandle = authorHandle;
        this.text = text;
        this.cleanedText = cleanedText;
        this.timestamp = timestamp;
        this.url = url;
        this.language = language;
        this.hashtags = hashtags;
        this.mentions = mentions;
        this.metrics = metrics;
        this.metadata = metadata;
        this.processedAt = processedAt;
    }
    
    // Manual builder pattern
    public static UnifiedPostBuilder builder() {
        return new UnifiedPostBuilder();
    }
    
    public static class UnifiedPostBuilder {
        private String id;
        private String source;
        private String author;
        private String authorHandle;
        private String text;
        private String cleanedText;
        private LocalDateTime timestamp;
        private String url;
        private String language;
        private List<String> hashtags;
        private List<String> mentions;
        private PostMetrics metrics;
        private Map<String, Object> metadata;
        private LocalDateTime processedAt;
        
        public UnifiedPostBuilder id(String id) {
            this.id = id;
            return this;
        }
        
        public UnifiedPostBuilder source(String source) {
            this.source = source;
            return this;
        }
        
        public UnifiedPostBuilder author(String author) {
            this.author = author;
            return this;
        }
        
        public UnifiedPostBuilder authorHandle(String authorHandle) {
            this.authorHandle = authorHandle;
            return this;
        }
        
        public UnifiedPostBuilder text(String text) {
            this.text = text;
            return this;
        }
        
        public UnifiedPostBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public UnifiedPostBuilder url(String url) {
            this.url = url;
            return this;
        }
        
        public UnifiedPostBuilder language(String language) {
            this.language = language;
            return this;
        }
        
        public UnifiedPostBuilder hashtags(List<String> hashtags) {
            this.hashtags = hashtags;
            return this;
        }
        
        public UnifiedPostBuilder mentions(List<String> mentions) {
            this.mentions = mentions;
            return this;
        }
        
        public UnifiedPostBuilder metrics(PostMetrics metrics) {
            this.metrics = metrics;
            return this;
        }
        
        public UnifiedPostBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public UnifiedPostBuilder processedAt(LocalDateTime processedAt) {
            this.processedAt = processedAt;
            return this;
        }
        
        public UnifiedPost build() {
            return new UnifiedPost(id, source, author, authorHandle, text, cleanedText, 
                                 timestamp, url, language, hashtags, mentions, metrics, 
                                 metadata, processedAt);
        }
    }
}

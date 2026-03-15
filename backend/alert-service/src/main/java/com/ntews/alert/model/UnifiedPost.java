package com.ntews.alert.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Unified schema for posts from different social media platforms
 * Simplified version for Alert service
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UnifiedPost {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("source")
    private String source;
    
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
    
    @JsonProperty("processed_at")
    private LocalDateTime processedAt;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    @JsonProperty("threat_level")
    private String threatLevel;
}

package com.ntews.ingestion.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialMediaData {
    private String id;
    private String platform; // twitter, facebook, telegram, tiktok
    private String postId;
    private String userId;
    private String username;
    private String displayName;
    private String content;
    private String language;
    
    // Engagement metrics
    private Integer likes;
    private Integer shares;
    private Integer comments;
    private Integer views;
    
    // Location data
    private String location;
    private String geoTagLatitude;
    private String geoTagLongitude;
    
    // Media content
    private List<String> imageUrls;
    private List<String> videoUrls;
    private Boolean hasMedia;
    
    // Timestamps
    private LocalDateTime postedAt;
    private LocalDateTime ingestedAt;
    
    // Metadata
    private Map<String, Object> metadata;
    
    // Sentiment analysis results (if pre-processed)
    private Double sentimentScore;
    private String sentimentLabel; // positive, negative, neutral
    
    // Threat indicators
    private List<String> threatKeywords;
    private Double threatScore;
    private Boolean isThreat;
    
    // Verification status
    private Boolean verified;
    private Double verificationScore;
}

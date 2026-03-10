package com.ntews.ingestion.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// TODO: Lombok disabled - manual implementation
public class SocialMediaData {
    public String id;
    public String platform; // twitter, facebook, telegram, tiktok
    public String postId;
    public String userId;
    public String username;
    public String displayName;
    public String content;
    public String language;
    
    // Engagement metrics
    public Integer likes;
    public Integer shares;
    public Integer comments;
    public Integer views;
    
    // Location data
    public String location;
    public String geoTagLatitude;
    public String geoTagLongitude;
    
    // Media content
    public List<String> imageUrls;
    public List<String> videoUrls;
    public Boolean hasMedia;
    
    // Timestamps
    public LocalDateTime postedAt;
    public LocalDateTime ingestedAt;
    
    // Metadata
    public Map<String, Object> metadata;
    
    // Sentiment analysis results (if pre-processed)
    public Double sentimentScore;
    
    // Sheng and security detection
    public Boolean shengDetected;
    public String securityRelevance;
    public List<String> shengWordsDetected;
    
    // Additional fields for Twitter streaming
    public String authorId;
    public String riskCategory;
    public String sentimentLabel; // positive, negative, neutral
    
    // Threat indicators
    public List<String> threatKeywords;
    public Double threatScore;
    public Boolean isThreat;
    
    // Verification status
    public Boolean verified;
    public Double verificationScore;
    
    // Manual getter methods
    public String getId() { return id; }
    public String getPlatform() { return platform; }
    public String getContent() { return content; }
    public String getGeoTagLatitude() { return geoTagLatitude; }
    public String getGeoTagLongitude() { return geoTagLongitude; }
    public String getLocation() { return location; }
    public LocalDateTime getPostedAt() { return postedAt; }
    public Map<String, Object> getMetadata() { return metadata; }
    public String getSecurityRelevance() { return securityRelevance; }
    
    // Manual setter methods
    public void setId(String id) { this.id = id; }
    public void setPlatform(String platform) { this.platform = platform; }
    public void setContent(String content) { this.content = content; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }
    public void setGeoTagLatitude(String geoTagLatitude) { this.geoTagLatitude = geoTagLatitude; }
    public void setGeoTagLongitude(String geoTagLongitude) { this.geoTagLongitude = geoTagLongitude; }
    public void setLocation(String location) { this.location = location; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public void setLanguage(String language) { this.language = language; }
    public void setShengDetected(Boolean shengDetected) { this.shengDetected = shengDetected; }
    public void setSecurityRelevance(String securityRelevance) { this.securityRelevance = securityRelevance; }
}

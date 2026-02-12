package com.ntews.alert.model;

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
public class IntelligenceReport {
    private String id;
    private String title;
    private String summary;
    private ThreatLevel threatLevel;
    private ThreatCategory category;
    private Double threatScore;
    private Double confidence;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocationInfo location;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private String createdBy;
    private Boolean verified;
    private AIAnalysis aiAnalysis;
    private List<String> recommendations;
    
    // Manual getters and setters to bypass Lombok issues
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    
    public ThreatLevel getThreatLevel() { return threatLevel; }
    public void setThreatLevel(ThreatLevel threatLevel) { this.threatLevel = threatLevel; }
    
    public ThreatCategory getCategory() { return category; }
    public void setCategory(ThreatCategory category) { this.category = category; }
    
    public Double getThreatScore() { return threatScore; }
    public void setThreatScore(Double threatScore) { this.threatScore = threatScore; }
    
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public LocationInfo getLocation() { return location; }
    public void setLocation(LocationInfo location) { this.location = location; }
    
    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }
    
    public AIAnalysis getAiAnalysis() { return aiAnalysis; }
    public void setAiAnalysis(AIAnalysis aiAnalysis) { this.aiAnalysis = aiAnalysis; }
    
    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    
    public enum ThreatLevel {
        LOW("low"),
        MEDIUM("medium"), 
        HIGH("high"),
        CRITICAL("critical");
        
        private final String value;
        
        ThreatLevel(String value) {
            this.value = value;
        }
        
        public String getValue() { return value; }
        
        public int getSeverity() {
            switch (this) {
                case LOW: return 1;
                case MEDIUM: return 2;
                case HIGH: return 3;
                case CRITICAL: return 4;
                default: return 0;
            }
        }
    }
    
    public enum ThreatCategory {
        SOCIAL_UNREST("social_unrest"),
        PHYSICAL_SECURITY("physical_security"),
        CYBER("cyber"),
        TERRORISM("terrorism"),
        NATURAL_DISASTER("natural_disaster"),
        OTHER("other");
        
        private final String value;
        
        ThreatCategory(String value) {
            this.value = value;
        }
        
        public String getValue() { return value; }
    }
    
    public enum ReportStatus {
        DRAFT, PENDING_REVIEW, VERIFIED, ARCHIVED
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AIAnalysis {
        private Double confidence;
        private String analysis;
        private List<String> threatKeywords;
        private List<String> keyEntities;
        private Map<String, Object> metadata;
        
        // Manual getters and setters
        public Double getConfidence() { return confidence; }
        public void setConfidence(Double confidence) { this.confidence = confidence; }
        
        public String getAnalysis() { return analysis; }
        public void setAnalysis(String analysis) { this.analysis = analysis; }
        
        public List<String> getThreatKeywords() { return threatKeywords; }
        public void setThreatKeywords(List<String> threatKeywords) { this.threatKeywords = threatKeywords; }
        
        public List<String> getKeyEntities() { return keyEntities; }
        public void setKeyEntities(List<String> keyEntities) { this.keyEntities = keyEntities; }
        
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LocationInfo {
        private String latitude;
        private String longitude;
        private String city;
        private String region;
        private String country;
        private String address;
        private Double radius;
        private String geoHash;
        
        // Manual getters and setters
        public String getLatitude() { return latitude; }
        public void setLatitude(String latitude) { this.latitude = latitude; }
        
        public String getLongitude() { return longitude; }
        public void setLongitude(String longitude) { this.longitude = longitude; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public Double getRadius() { return radius; }
        public void setRadius(Double radius) { this.radius = radius; }
        
        public String getGeoHash() { return geoHash; }
        public void setGeoHash(String geoHash) { this.geoHash = geoHash; }
    }
}

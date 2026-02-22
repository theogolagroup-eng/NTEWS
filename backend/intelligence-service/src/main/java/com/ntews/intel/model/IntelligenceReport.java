package com.ntews.intel.model;

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
    private String description;
    private String content; // AI Engine integration field
    
    // Threat classification
    private ThreatLevel threatLevel;
    private ThreatCategory category;
    private Double threatScore;
    private Double confidence;
    
    // Temporal and spatial information
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocationInfo location;
    private List<String> affectedAreas;
    
    // Source intelligence
    private List<ThreatIntelligence> sources;
    private List<CorrelatedEvent> correlatedEvents;
    
    // AI analysis results
    private AIAnalysis aiAnalysis;
    
    // AI Engine integration fields
    private Double aiConfidence;
    private String aiThreatLevel;
    private Integer aiRiskScore;
    private String aiRecommendations;
    
    // Recommendations
    private List<String> recommendations;
    private List<String> requiredActions;
    
    // Status and metadata
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String assignedTo;
    
    // Verification
    private Boolean verified;
    private String verificationNotes;
    private LocalDateTime verifiedAt;
    private String verifiedBy;
    
    // Explicit getters and setters for AI fields
    public Double getAiConfidence() { return aiConfidence; }
    public void setAiConfidence(Double aiConfidence) { this.aiConfidence = aiConfidence; }
    
    public String getAiThreatLevel() { return aiThreatLevel; }
    public void setAiThreatLevel(String aiThreatLevel) { this.aiThreatLevel = aiThreatLevel; }
    
    public Integer getAiRiskScore() { return aiRiskScore; }
    public void setAiRiskScore(Integer aiRiskScore) { this.aiRiskScore = aiRiskScore; }
    
    public String getAiRecommendations() { return aiRecommendations; }
    public void setAiRecommendations(String aiRecommendations) { this.aiRecommendations = aiRecommendations; }
    
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
        private Double radius;  // Affected radius in meters
        private String geoHash;
    }
    
    public enum ThreatLevel {
        LOW("low", 1),
        MEDIUM("medium", 2),
        HIGH("high", 3),
        CRITICAL("critical", 4);
        
        private final String value;
        private final int severity;
        
        ThreatLevel(String value, int severity) {
            this.value = value;
            this.severity = severity;
        }
        
        public String getValue() {
            return value;
        }
        
        public int getSeverity() {
            return severity;
        }
    }
    
    public enum ThreatCategory {
        TERRORISM("terrorism"),
        CYBER("cyber"),
        PHYSICAL_SECURITY("physical_security"),
        SECURITY("security"),  // Added for compatibility
        SOCIAL_UNREST("social_unrest"),
        NATURAL_DISASTER("natural_disaster"),
        ECONOMIC("economic"),
        POLITICAL("political"),
        OTHER("other");
        
        private final String value;
        
        ThreatCategory(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ThreatIntelligence {
        private String id;
        private String source;
        private String sourceType;
        private String content;
        private LocalDateTime timestamp;
        private Double relevanceScore;
        private Map<String, Object> metadata;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CorrelatedEvent {
        private String eventId;
        private String eventType;
        private String description;
        private LocalDateTime timestamp;
        private LocationInfo location;
        private Double correlationScore;
        private String correlationReason;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AIAnalysis {
        private String nlpAnalysis;
        private String visionAnalysis;
        private String predictiveAnalysis;
        private List<String> keyEntities;
        private List<String> threatKeywords;
        private Double sentimentScore;
        private String sentimentLabel;
        private Map<String, Double> featureImportance;
        private String explanation;
    }
    
    public enum ReportStatus {
        DRAFT("draft"),
        PENDING_REVIEW("pending_review"),
        UNDER_REVIEW("under_review"),
        APPROVED("approved"),
        REJECTED("rejected"),
        PUBLISHED("published"),
        ARCHIVED("archived");
        
        private final String value;
        
        ReportStatus(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
}

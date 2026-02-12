package com.ntews.ingestion.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThreatData {
    private String id;
    private String source;
    private String sourceType; // social_media, cctv, cyber, news
    private String contentType; // text, image, video, log
    private String rawContent;
    private String processedContent;
    
    // Location information
    private LocationInfo location;
    
    // Metadata
    private Map<String, Object> metadata;
    private LocalDateTime timestamp;
    private LocalDateTime ingestedAt;
    
    // Processing status
    private ProcessingStatus status;
    private String errorMessage;
    
    // AI Engine integration fields
    private Double aiThreatScore;
    private String aiRiskLevel;
    private Double aiConfidence;
    private String aiKeywords;
    private String aiAnalysis;
    private String predictedSeverity;
    private Integer timeToCritical; // minutes
    private String evolutionTrend;
    
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
        private Double confidence;
    }
    
    public enum ProcessingStatus {
        PENDING("pending"),
        PROCESSING("processing"),
        PROCESSED("processed"),
        FAILED("failed");
        
        private final String value;
        
        ProcessingStatus(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    // Getter methods for AI fields (since Lombok might not generate them properly)
    public String getType() { return sourceType; }
    public String getContent() { return processedContent != null ? processedContent : rawContent; }
    public String getSeverity() { return "medium"; } // Default severity
    public Double getConfidence() { return aiConfidence != null ? aiConfidence : 0.5; }
    public Double getAiThreatScore() { return aiThreatScore; }
    public String getAiRiskLevel() { return aiRiskLevel; }
    public Double getAiConfidence() { return aiConfidence; }
    public String getAiKeywords() { return aiKeywords; }
    public String getAiAnalysis() { return aiAnalysis; }
    public String getPredictedSeverity() { return predictedSeverity; }
    public Integer getTimeToCritical() { return timeToCritical; }
    public String getEvolutionTrend() { return evolutionTrend; }
    
    // Setter methods for AI fields
    public void setAiThreatScore(Double aiThreatScore) { this.aiThreatScore = aiThreatScore; }
    public void setAiRiskLevel(String aiRiskLevel) { this.aiRiskLevel = aiRiskLevel; }
    public void setAiConfidence(Double aiConfidence) { this.aiConfidence = aiConfidence; }
    public void setAiKeywords(String aiKeywords) { this.aiKeywords = aiKeywords; }
    public void setAiAnalysis(String aiAnalysis) { this.aiAnalysis = aiAnalysis; }
    public void setPredictedSeverity(String predictedSeverity) { this.predictedSeverity = predictedSeverity; }
    public void setTimeToCritical(Integer timeToCritical) { this.timeToCritical = timeToCritical; }
    public void setEvolutionTrend(String evolutionTrend) { this.evolutionTrend = evolutionTrend; }
}

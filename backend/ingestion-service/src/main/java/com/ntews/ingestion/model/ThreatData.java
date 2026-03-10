package com.ntews.ingestion.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

// TODO: Lombok disabled - manual implementation
public class ThreatData {
    public String id;
    public String source;
    public String sourceType; // social_media, cctv, cyber, news
    public String contentType; // text, image, video, log
    public String rawContent;
    public String processedContent;
    
    // Location information
    public LocationInfo location;
    
    // Metadata
    public Map<String, Object> metadata;
    public LocalDateTime timestamp;
    public LocalDateTime ingestedAt;
    
    // Processing status
    public ProcessingStatus status;
    public String errorMessage;
    
    // AI Engine integration fields
    public Double aiThreatScore;
    public String aiRiskLevel;
    public Double aiConfidence;
    public String aiKeywords;
    public String aiAnalysis;
    public String predictedSeverity;
    public Integer timeToCritical; // minutes
    public String evolutionTrend;
    
    // TODO: Lombok disabled - manual implementation
    public static class LocationInfo {
        public String latitude;
        public String longitude;
        public String city;
        public String region;
        public String country;
        public String address;
        public Double confidence;
        
        // Manual constructor
        public LocationInfo() {}
        
        public LocationInfo(String latitude, String longitude, String city, String region, 
                          String country, String address, Double confidence) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.city = city;
            this.region = region;
            this.country = country;
            this.address = address;
            this.confidence = confidence;
        }
        
        // Manual builder pattern
        public static LocationInfoBuilder builder() {
            return new LocationInfoBuilder();
        }
        
        public static class LocationInfoBuilder {
            private String latitude;
            private String longitude;
            private String city;
            private String region;
            private String country;
            private String address;
            private Double confidence;
            
            public LocationInfoBuilder latitude(String latitude) {
                this.latitude = latitude;
                return this;
            }
            
            public LocationInfoBuilder longitude(String longitude) {
                this.longitude = longitude;
                return this;
            }
            
            public LocationInfoBuilder address(String address) {
                this.address = address;
                return this;
            }
            
            public LocationInfoBuilder confidence(Double confidence) {
                this.confidence = confidence;
                return this;
            }
            
            public LocationInfoBuilder city(String city) {
                this.city = city;
                return this;
            }
            
            public LocationInfoBuilder region(String region) {
                this.region = region;
                return this;
            }
            
            public LocationInfoBuilder country(String country) {
                this.country = country;
                return this;
            }
            
            public LocationInfo build() {
                return new LocationInfo(latitude, longitude, city, region, country, address, confidence);
            }
        }
        
        // Manual getter methods
        public String getAddress() { return address; }
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
    
    // Missing getter methods needed by other classes
    public String getId() { return id; }
    public String getSource() { return source; }
    public String getSourceType() { return sourceType; }
    public String getContentType() { return contentType; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public LocationInfo getLocation() { return location; }
    public ProcessingStatus getStatus() { return status; }
    public void setStatus(ProcessingStatus status) { this.status = status; }
    public void setId(String id) { this.id = id; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public void setIngestedAt(LocalDateTime ingestedAt) { this.ingestedAt = ingestedAt; }
    
    // Manual builder pattern
    public static ThreatDataBuilder builder() {
        return new ThreatDataBuilder();
    }
    
    public static class ThreatDataBuilder {
        private String id;
        private String source;
        private String sourceType;
        private String contentType;
        private String rawContent;
        private String processedContent;
        private LocationInfo location;
        private Map<String, Object> metadata;
        private LocalDateTime timestamp;
        private LocalDateTime ingestedAt;
        private ProcessingStatus status;
        private String errorMessage;
        private Double aiThreatScore;
        private String aiRiskLevel;
        private Double aiConfidence;
        private String aiKeywords;
        private String aiAnalysis;
        private String predictedSeverity;
        private Integer timeToCritical;
        private String evolutionTrend;
        
        public ThreatDataBuilder id(String id) {
            this.id = id;
            return this;
        }
        
        public ThreatDataBuilder source(String source) {
            this.source = source;
            return this;
        }
        
        public ThreatDataBuilder sourceType(String sourceType) {
            this.sourceType = sourceType;
            return this;
        }
        
        public ThreatDataBuilder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }
        
        public ThreatDataBuilder rawContent(String rawContent) {
            this.rawContent = rawContent;
            return this;
        }
        
        public ThreatDataBuilder processedContent(String processedContent) {
            this.processedContent = processedContent;
            return this;
        }
        
        public ThreatDataBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public ThreatDataBuilder ingestedAt(LocalDateTime ingestedAt) {
            this.ingestedAt = ingestedAt;
            return this;
        }
        
        public ThreatDataBuilder status(ProcessingStatus status) {
            this.status = status;
            return this;
        }
        
        public ThreatDataBuilder location(LocationInfo location) {
            this.location = location;
            return this;
        }
        
        public ThreatDataBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public ThreatDataBuilder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }
        
        public ThreatData build() {
            ThreatData data = new ThreatData();
            data.id = this.id;
            data.source = this.source;
            data.sourceType = this.sourceType;
            data.contentType = this.contentType;
            data.rawContent = this.rawContent;
            data.processedContent = this.processedContent;
            data.location = this.location;
            data.metadata = this.metadata;
            data.timestamp = this.timestamp;
            data.ingestedAt = this.ingestedAt;
            data.status = this.status;
            data.errorMessage = this.errorMessage;
            data.aiThreatScore = this.aiThreatScore;
            data.aiRiskLevel = this.aiRiskLevel;
            data.aiConfidence = this.aiConfidence;
            data.aiKeywords = this.aiKeywords;
            data.aiAnalysis = this.aiAnalysis;
            data.predictedSeverity = this.predictedSeverity;
            data.timeToCritical = this.timeToCritical;
            data.evolutionTrend = this.evolutionTrend;
            return data;
        }
    }
}

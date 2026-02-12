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
public class CCTVData {
    private String id;
    private String cameraId;
    private String location;
    private String latitude;
    private String longitude;
    private String streamUrl;
    
    // Video metadata
    private String format;
    private Long duration;
    private Integer frameRate;
    private String resolution;
    private Long fileSize;
    
    // Detection results
    private List<Detection> detections;
    private Double crowdDensity;
    private Boolean hasAnomaly;
    private Double anomalyScore;
    
    // Timestamps
    private LocalDateTime recordedAt;
    private LocalDateTime ingestedAt;
    
    // Processing information
    private String processingStatus;
    private String errorMessage;
    
    // Metadata
    private Map<String, Object> metadata;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Detection {
        private String type; // person, vehicle, weapon, suspicious_object
        private String label;
        private Double confidence;
        private BoundingBox boundingBox;
        private LocalDateTime detectedAt;
        private Map<String, Object> attributes;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BoundingBox {
        private Double x;
        private Double y;
        private Double width;
        private Double height;
    }
}

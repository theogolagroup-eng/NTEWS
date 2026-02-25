package com.ntews.intel.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "predictions")
public class Prediction {
    
    @Id
    private String id;
    
    // Core prediction data
    private String type; // weather, traffic, social, economic, security
    private String category; // sub-category
    private String title;
    private String description;
    private String severity; // low, medium, high, critical
    private Double confidence; // 0.0 to 1.0
    
    // Temporal data
    private LocalDateTime predictedAt;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Integer timeWindowHours; // prediction window
    
    // Geographic data
    private String location;
    private String latitude;
    private String longitude;
    private Double radiusKm;
    private List<String> affectedAreas;
    
    // Prediction details
    private List<String> riskFactors;
    private List<String> indicators;
    private List<String> recommendedActions;
    
    // AI/ML metadata
    private String aiModel;
    private String aiVersion;
    private String aiConfidence;
    private String aiExplanation;
    private Boolean aiProcessed;
    
    // Status and metadata
    private String status; // ACTIVE, EXPIRED, RESOLVED, CANCELLED
    private String source;
    private String dataSource;
    private LocalDateTime lastUpdated;
    private String verifiedBy;
    private String notes;
    
    // Integration data
    private String intelligenceReportId;
    private String alertId;
    private List<String> relatedPredictions;
}

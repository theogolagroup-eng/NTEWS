package com.ntews.prediction.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Enhanced Threat Prediction model with AI Engine Integration
 * Supports multilingual threat prediction with Sheng-aware analysis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "threat_predictions")
public class ThreatPrediction {
    
    @Id
    private String id;
    
    // Core prediction fields
    private String predictionType; // POLITICAL_UNREST, BORDER_SECURITY, SOCIAL_MEDIA_VIRAL, CYBER_SECURITY, COMMUNITY_EVENT
    private String title;
    private String description;
    private double confidenceLevel; // 0.0 to 1.0
    private double riskScore; // 0.0 to 1.0
    private String affectedRegion;
    private String timeframe;
    private String dataSource;
    
    // AI Engine Integration Fields
    private String languageContext; // english, swahili, sheng-heavy, mixed
    private String shengKeywords; // comma-separated Sheng keywords
    private String swahiliKeywords; // comma-separated Swahili keywords
    private String englishKeywords; // comma-separated English keywords
    
    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String status; // ACTIVE, EXPIRED, RESOLVED
    
    // Helper methods
    public boolean isHighRisk() {
        return riskScore >= 0.7;
    }
    
    public boolean isMediumRisk() {
        return riskScore >= 0.4 && riskScore < 0.7;
    }
    
    public boolean isLowRisk() {
        return riskScore < 0.4;
    }
    
    public boolean hasShengContent() {
        return shengKeywords != null && !shengKeywords.trim().isEmpty();
    }
    
    public boolean hasMultilingualContent() {
        return !("english".equals(languageContext));
    }
    
    public String getRiskLevelDescription() {
        if (riskScore >= 0.7) return "High Risk";
        if (riskScore >= 0.4) return "Medium Risk";
        return "Low Risk";
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}

package com.ntews.alert.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Enhanced Security Alert model with AI Engine Integration
 * Handles multilingual threat detection with Sheng-aware analysis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityAlert {
    
    // Core alert fields
    private String id;
    private String title;
    private String description;
    private LocalDateTime timestamp;
    
    // Classification fields
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL
    private String status; // ACTIVE, RESOLVED, ACKNOWLEDGED
    private String category; // SECURITY, THREAT, CIVIL_UNREST, BENIGN
    private String source;
    private String location;
    
    // AI Engine Analysis Fields
    private String riskCategory; // threat, suspicious, benign, civil_unrest
    private double confidence; // 0.0 to 1.0
    private double threatScore; // 0.0 to 1.0
    private String threatKeywords; // comma-separated threat keywords
    private String shengWordsDetected; // comma-separated Sheng words
    private String detectedLanguage; // english, swahili, sheng-mixed, unknown
    private double eastAfricanRelevance; // 0.0 to 1.0
    private boolean politicalContext; // true/false
    private Map<String, Double> threatProbabilities; // threat probability breakdown
    private String processingMethod; // rule_based, ml_model, fallback
    
    // Legacy engagement fields (for backward compatibility)
    private double repostPercentage;
    private double commentPercentage;
    private double engagementScore;
    private String originalLanguage;
    private String contextEnhancement;
    
    // Alert management
    private boolean acknowledged;
    private String assignedTo;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime resolvedAt;
    
    /**
     * Calculate engagement metrics from social media data
     */
    public void calculateEngagementMetrics(long retweets, long replies, long likes, long quotes) {
        long totalEngagement = retweets + replies + likes + quotes;
        
        if (totalEngagement > 0) {
            this.repostPercentage = (double) retweets / totalEngagement * 100;
            this.commentPercentage = (double) replies / totalEngagement * 100;
            this.engagementScore = (double) totalEngagement / 1000.0; // Normalize to 0-1 scale
        } else {
            this.repostPercentage = 0.0;
            this.commentPercentage = 0.0;
            this.engagementScore = 0.0;
        }
    }
    
    /**
     * Check if alert is high priority based on AI analysis
     */
    public boolean isHighPriority() {
        return "CRITICAL".equals(severity) || 
               ("HIGH".equals(severity) && confidence > 0.7) ||
               (threatScore > 0.8 && eastAfricanRelevance > 0.5);
    }
    
    /**
     * Check if alert has multilingual content
     */
    public boolean hasMultilingualContent() {
        return !("english".equals(detectedLanguage) || "unknown".equals(detectedLanguage));
    }
    
    /**
     * Check if alert has Sheng content
     */
    public boolean hasShengContent() {
        return shengWordsDetected != null && !shengWordsDetected.trim().isEmpty();
    }
    
    /**
     * Get risk level as human readable
     */
    public String getRiskLevelDescription() {
        if (threatScore >= 0.8) return "Critical Risk";
        if (threatScore >= 0.6) return "High Risk";
        if (threatScore >= 0.4) return "Medium Risk";
        if (threatScore >= 0.2) return "Low Risk";
        return "Minimal Risk";
    }
    
    /**
     * Legacy method for backward compatibility
     */
    public String getText() {
        return description != null ? description : (title != null ? title : "");
    }
    
    /**
     * Legacy method for backward compatibility
     */
    public String getUsername() {
        return source != null ? source : "system";
    }
}

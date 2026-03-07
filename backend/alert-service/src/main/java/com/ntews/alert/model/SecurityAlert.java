package com.ntews.alert.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Security Alert model for real-time monitoring
 * Handles Sheng-aware threat detection with engagement metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityAlert {
    
    private String id;
    private String username;
    private String text;
    private String timestamp;
    private double repostPercentage;
    private double commentPercentage;
    private double confidenceScore;
    private String riskCategory;
    private String originalLanguage;
    private String contextEnhancement;
    private List<String> shengWordsDetected;
    private String location;
    private String source;
    
    /**
     * Calculate engagement metrics from X/Twitter API public_metrics
     * 
     * @param retweets Number of retweets
     * @param replies Number of replies/comments
     * @param likes Number of likes
     * @param quotes Number of quote tweets
     */
    public void calculateMetrics(long retweets, long replies, long likes, long quotes) {
        long totalEngagement = retweets + replies + likes + quotes;
        
        if (totalEngagement > 0) {
            this.repostPercentage = (double) retweets / totalEngagement * 100;
            this.commentPercentage = (double) replies / totalEngagement * 100;
        } else {
            this.repostPercentage = 0.0;
            this.commentPercentage = 0.0;
        }
    }
    
    /**
     * Risk categories for classification
     */
    public enum RiskCategory {
        BENIGN("benign"),
        SUSPICIOUS("suspicious"), 
        THREAT("threat"),
        CIVIL_UNREST("civil_unrest"),
        HIGH_ENGAGEMENT("high_engagement");
        
        private final String value;
        
        RiskCategory(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
}

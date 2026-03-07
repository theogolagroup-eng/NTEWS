package com.ntews.alert.service;

import com.ntews.alert.model.SecurityAlert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Security Alert Service for real-time monitoring
 * Processes Sheng-aware security alerts with engagement metrics
 */
@Service
@Slf4j
public class SecurityAlertService {
    
    // In-memory storage for real-time processing
    private final Map<String, SecurityAlert> activeAlerts = new ConcurrentHashMap<>();
    private final Map<String, Integer> hashtagCounts = new ConcurrentHashMap<>();
    private final Map<String, Integer> shengKeywordCounts = new ConcurrentHashMap<>();
    
    // Daily statistics
    private int alertsToday = 0;
    private int highRiskAlertsCount = 0;
    private int shengDetectedCount = 0;
    private int engagementAlertsCount = 0;
    
    /**
     * Process incoming security alert
     */
    public void processSecurityAlert(SecurityAlert alert) {
        try {
            // Store alert
            activeAlerts.put(alert.getId(), alert);
            
            // Update daily statistics
            if (alert.getTimestamp().toLocalDate().equals(LocalDate.now())) {
                alertsToday++;
            }
            
            // Update risk category counts
            if ("threat".equals(alert.getRiskCategory()) || "civil_unrest".equals(alert.getRiskCategory())) {
                highRiskAlertsCount++;
            }
            
            // Update Sheng detection count
            if (alert.getShengWordsDetected() != null && !alert.getShengWordsDetected().isEmpty()) {
                shengDetectedCount++;
            }
            
            // Update engagement alert count
            if ("high_engagement".equals(alert.getRiskCategory())) {
                engagementAlertsCount++;
            }
            
            // Update hashtag tracking
            updateHashtagTracking(alert.getText());
            
            log.info("Processed security alert: {} | Risk: {} | Sheng: {} | Engagement: {}%",
                alert.getId(), alert.getRiskCategory(), 
                alert.getShengWordsDetected() != null ? alert.getShengWordsDetected().size() : 0,
                alert.getEngagementScore());
            
        } catch (Exception e) {
            log.error("Error processing security alert: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Update hashtag tracking from alert text
     */
    private void updateHashtagTracking(String text) {
        if (text == null) return;
        
        String[] words = text.toLowerCase().split("\\s+");
        for (String word : words) {
            if (word.startsWith("#")) {
                String hashtag = word.substring(1);
                hashtagCounts.merge(hashtag, 1, Integer::sum);
            }
        }
    }
    
    /**
     * Get alerts count for today
     */
    public int getAlertsCountToday() {
        return alertsToday;
    }
    
    /**
     * Get high risk alerts count
     */
    public int getHighRiskAlertsCount() {
        return highRiskAlertsCount;
    }
    
    /**
     * Get Sheng detected count
     */
    public int getShengDetectedCount() {
        return shengDetectedCount;
    }
    
    /**
     * Get engagement alerts count
     */
    public int getEngagementAlertsCount() {
        return engagementAlertsCount;
    }
    
    /**
     * Get trending hashtags
     */
    public Map<String, Integer> getTrendingHashtags() {
        // Return top 10 hashtags by count
        return hashtagCounts.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
    
    /**
     * Get top Sheng keywords
     */
    public Map<String, Integer> getTopShengKeywords() {
        // Return top 10 Sheng keywords by frequency
        return shengKeywordCounts.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
    
    /**
     * Get risk distribution
     */
    public Map<String, Integer> getRiskDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("benign", 0);
        distribution.put("suspicious", 0);
        distribution.put("threat", 0);
        distribution.put("civil_unrest", 0);
        distribution.put("high_engagement", 0);
        
        // Count active alerts by risk category
        for (SecurityAlert alert : activeAlerts.values()) {
            String category = alert.getRiskCategory();
            distribution.merge(category, 1, Integer::sum);
        }
        
        return distribution;
    }
    
    /**
     * Get trending security topics
     */
    public List<Map<String, Object>> getTrendingSecurityTopics() {
        List<Map<String, Object>> topics = new ArrayList<>();
        
        // Get recent alerts with high risk or civil unrest
        activeAlerts.values().stream()
                .filter(alert -> "threat".equals(alert.getRiskCategory()) || 
                                 "civil_unrest".equals(alert.getRiskCategory()) ||
                                 "high_engagement".equals(alert.getRiskCategory()))
                .limit(5)
                .forEach(alert -> {
                    Map<String, Object> topic = new HashMap<>();
                    topic.put("text", alert.getText());
                    topic.put("risk_category", alert.getRiskCategory());
                    topic.put("confidence", alert.getConfidenceScore());
                    topic.put("timestamp", alert.getTimestamp());
                    topic.put("sheng_detected", alert.getShengWordsDetected() != null && !alert.getShengWordsDetected().isEmpty());
                    topic.put("engagement_score", alert.getEngagementScore());
                    topic.put("original_language", alert.getOriginalLanguage());
                    topics.add(topic);
                });
        
        return topics;
    }
    
    /**
     * Reset daily statistics (call at midnight)
     */
    public void resetDailyStatistics() {
        alertsToday = 0;
        highRiskAlertsCount = 0;
        shengDetectedCount = 0;
        engagementAlertsCount = 0;
        log.info("Daily security statistics reset");
    }
}

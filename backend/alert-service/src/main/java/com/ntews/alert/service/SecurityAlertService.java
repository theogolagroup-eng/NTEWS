package com.ntews.alert.service;

import com.ntews.alert.model.SecurityAlert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enhanced Security Alert Service with AI Engine Integration
 * Processes Sheng-aware security alerts with multilingual threat analysis
 */
@Service
@Slf4j
public class SecurityAlertService {
    
    private final AIEngineIntegrationService aiEngineService;
    
    // In-memory storage for real-time processing
    private final Map<String, SecurityAlert> activeAlerts = new ConcurrentHashMap<>();
    private final Map<String, Integer> hashtagCounts = new ConcurrentHashMap<>();
    private final Map<String, Integer> shengKeywordCounts = new ConcurrentHashMap<>();
    
    // Daily statistics
    private int alertsToday = 0;
    private int highRiskAlertsCount = 0;
    private int shengDetectedCount = 0;
    private int engagementAlertsCount = 0;
    private int aiAnalyzedAlertsCount = 0;
    
    @Autowired
    public SecurityAlertService(AIEngineIntegrationService aiEngineService) {
        this.aiEngineService = aiEngineService;
    }
    
    /**
     * Process incoming security alert with AI Engine analysis
     */
    public Mono<SecurityAlert> processSecurityAlert(SecurityAlert alert) {
        return Mono.fromCallable(() -> {
            log.info("🚨 Processing security alert: {}", alert.getTitle());
            
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
            
            return alert;
        })
        .flatMap(originalAlert -> {
            // Analyze with AI Engine if description is available
            if (alert.getDescription() != null && !alert.getDescription().trim().isEmpty()) {
                aiAnalyzedAlertsCount++;
                return aiEngineService.analyzeTextThreat(alert.getDescription(), "security_alert")
                    .map(aiResult -> enhanceAlertWithAIAnalysis(originalAlert, aiResult));
            } else {
                return Mono.just(originalAlert);
            }
        })
        .doOnSuccess(enhancedAlert -> {
            // Update statistics based on AI analysis
            if (enhancedAlert.getShengWordsDetected() != null && !enhancedAlert.getShengWordsDetected().isEmpty()) {
                shengDetectedCount++;
            }
            
            // Update hashtag counts if present
            updateHashtagCounts(enhancedAlert);
            
            // Update Sheng keyword counts
            updateShengKeywordCounts(enhancedAlert);
            
            log.info("✅ Enhanced alert processed with AI analysis: {} (confidence: {})", 
                enhancedAlert.getRiskCategory(), enhancedAlert.getConfidence());
        });
    }
    
    /**
     * Enhance alert with AI Engine analysis results
     */
    private SecurityAlert enhanceAlertWithAIAnalysis(SecurityAlert originalAlert, 
                                                    AIEngineIntegrationService.ThreatAnalysisResult aiResult) {
        
        // Update risk category based on AI classification
        String aiRiskCategory = mapAIToRiskCategory(aiResult.getClassification());
        originalAlert.setRiskCategory(aiRiskCategory);
        
        // Update confidence
        originalAlert.setConfidence(aiResult.getConfidence());
        
        // Update threat score
        originalAlert.setThreatScore(aiResult.getRiskScore());
        
        // Add AI analysis metadata
        originalAlert.setThreatKeywords(aiResult.getThreatKeywords());
        originalAlert.setShengWordsDetected(aiResult.getShengWordsDetected());
        originalAlert.setEastAfricanRelevance(aiResult.getEastAfricanRelevance());
        originalAlert.setPoliticalContext(aiResult.isPoliticalContext());
        originalAlert.setDetectedLanguage(aiResult.getDetectedLanguage());
        originalAlert.setProcessingMethod(aiResult.getProcessingMethod());
        
        // Update severity based on AI analysis
        if (aiResult.isHighConfidence() && aiResult.isThreat()) {
            originalAlert.setSeverity("CRITICAL");
        } else if (aiResult.isThreat()) {
            originalAlert.setSeverity("HIGH");
        } else if (aiResult.hasEastAfricanContext()) {
            originalAlert.setSeverity("MEDIUM");
        }
        
        return originalAlert;
    }
    
    /**
     * Map AI Engine classification to alert risk category
     */
    private String mapAIToRiskCategory(String aiClassification) {
        switch (aiClassification.toLowerCase()) {
            case "threat":
                return "threat";
            case "civil_unrest":
                return "civil_unrest";
            case "suspicious":
                return "suspicious";
            case "benign":
            default:
                return "benign";
        }
    }
    
    /**
     * Update hashtag counts from alert content
     */
    private void updateHashtagCounts(SecurityAlert alert) {
        if (alert.getDescription() != null) {
            String[] words = alert.getDescription().toLowerCase().split("\\s+");
            for (String word : words) {
                if (word.startsWith("#")) {
                    String hashtag = word.substring(1);
                    hashtagCounts.merge(hashtag, 1, (oldValue, newValue) -> oldValue + newValue);
                }
            }
        }
    }
    
    /**
     * Update Sheng keyword counts
     */
    private void updateShengKeywordCounts(SecurityAlert alert) {
        if (alert.getShengWordsDetected() != null && !alert.getShengWordsDetected().isEmpty()) {
            String[] shengWords = alert.getShengWordsDetected().split(",\\s*");
            for (String shengWord : shengWords) {
                if (!shengWord.trim().isEmpty()) {
                    shengKeywordCounts.merge(shengWord.trim(), 1, (oldValue, newValue) -> oldValue + newValue);
                }
            }
        }
    }
    
    /**
     * Get alert by ID
     */
    public SecurityAlert getAlert(String id) {
        return activeAlerts.get(id);
    }
    
    /**
     * Get all active alerts
     */
    public List<SecurityAlert> getAllActiveAlerts() {
        return new ArrayList<>(activeAlerts.values());
    }
    
    /**
     * Get alerts by risk category
     */
    public List<SecurityAlert> getAlertsByRiskCategory(String riskCategory) {
        return activeAlerts.values().stream()
                .filter(alert -> riskCategory.equals(alert.getRiskCategory()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    /**
     * Get alerts with Sheng content
     */
    public List<SecurityAlert> getAlertsWithShengContent() {
        return activeAlerts.values().stream()
                .filter(alert -> alert.getShengWordsDetected() != null && !alert.getShengWordsDetected().isEmpty())
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    /**
     * Get trending hashtags
     */
    public Map<String, Integer> getTrendingHashtags() {
        return hashtagCounts.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .collect(java.util.stream.Collectors.toMap(
                    Map.Entry::getKey, 
                    Map.Entry::getValue, 
                    (e1, e2) -> e1, 
                    java.util.LinkedHashMap::new
                ));
    }
    
    /**
     * Get top Sheng keywords
     */
    public Map<String, Integer> getTopShengKeywords() {
        return shengKeywordCounts.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .collect(java.util.stream.Collectors.toMap(
                    Map.Entry::getKey, 
                    Map.Entry::getValue, 
                    (e1, e2) -> e1, 
                    java.util.LinkedHashMap::new
                ));
    }
    
    /**
     * Get daily statistics
     */
    public Map<String, Object> getDailyStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("alertsToday", alertsToday);
        stats.put("highRiskAlertsCount", highRiskAlertsCount);
        stats.put("shengDetectedCount", shengDetectedCount);
        stats.put("engagementAlertsCount", engagementAlertsCount);
        stats.put("aiAnalyzedAlertsCount", aiAnalyzedAlertsCount);
        stats.put("activeAlertsCount", activeAlerts.size());
        return stats;
    }
    
    /**
     * Check AI Engine health
     */
    public Mono<Boolean> isAIEngineHealthy() {
        return aiEngineService.isAIEngineHealthy();
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
}

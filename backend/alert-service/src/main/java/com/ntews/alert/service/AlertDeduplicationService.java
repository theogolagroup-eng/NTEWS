package com.ntews.alert.service;

import com.ntews.alert.model.IntelligenceReport;
import com.ntews.alert.model.RiskForecast;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AlertDeduplicationService {
    
    @Value("${alerts.deduplication.window:300000}")
    private long deduplicationWindow; // 5 minutes default
    
    @Value("${alerts.deduplication.similarity-threshold:0.8}")
    private double similarityThreshold;
    
    // Simple in-memory cache for recent alerts
    private final Map<String, LocalDateTime> recentAlerts = new HashMap<>();
    
    public boolean isDuplicate(IntelligenceReport report) {
        String key = generateReportKey(report);
        LocalDateTime now = LocalDateTime.now();
        
        // Check if similar report was recently processed
        if (recentAlerts.containsKey(key)) {
            LocalDateTime lastProcessed = recentAlerts.get(key);
            
            // Check if within deduplication window
            if (lastProcessed.plusNanos(deduplicationWindow * 1_000_000).isAfter(now)) {
                log.debug("Duplicate intelligence report detected: {}", report.getId());
                return true;
            }
        }
        
        // Update the cache
        recentAlerts.put(key, now);
        
        // Clean up old entries
        cleanupOldEntries(now);
        
        return false;
    }
    
    public boolean isDuplicate(RiskForecast forecast) {
        String key = generateForecastKey(forecast);
        LocalDateTime now = LocalDateTime.now();
        
        // Check if similar forecast was recently processed
        if (recentAlerts.containsKey(key)) {
            LocalDateTime lastProcessed = recentAlerts.get(key);
            
            // Check if within deduplication window
            if (lastProcessed.plusNanos(deduplicationWindow * 1_000_000).isAfter(now)) {
                log.debug("Duplicate risk forecast detected: {}", forecast.getId());
                return true;
            }
        }
        
        // Update the cache
        recentAlerts.put(key, now);
        
        // Clean up old entries
        cleanupOldEntries(now);
        
        return false;
    }
    
    private String generateReportKey(IntelligenceReport report) {
        StringBuilder key = new StringBuilder();
        
        // Include threat level and category
        key.append(report.getThreatLevel().getValue())
           .append(":")
           .append(report.getCategory().getValue());
        
        // Include location (if available)
        if (report.getLocation() != null) {
            key.append(":")
               .append(report.getLocation().getLatitude())
               .append(":")
               .append(report.getLocation().getLongitude());
        }
        
        // Include key entities from AI analysis
        if (report.getAiAnalysis() != null && report.getAiAnalysis().getKeyEntities() != null) {
            key.append(":")
               .append(String.join(",", report.getAiAnalysis().getKeyEntities().subList(0, Math.min(3, report.getAiAnalysis().getKeyEntities().size()))));
        }
        
        return key.toString();
    }
    
    private String generateForecastKey(RiskForecast forecast) {
        StringBuilder key = new StringBuilder();
        
        // Include forecast type
        key.append(forecast.getForecastType());
        
        // Include overall risk trend (if available)
        if (forecast.getOverallRiskTrend() != null) {
            key.append(":")
               .append(String.format("%.2f", forecast.getOverallRiskTrend()));
        }
        
        // Include hotspot information (if available)
        if (forecast.getHotspots() != null && !forecast.getHotspots().isEmpty()) {
            // Use the top hotspot location as part of the key
            RiskForecast.HotspotPrediction topHotspot = forecast.getHotspots().get(0);
            key.append(":")
               .append(topHotspot.getLatitude())
               .append(":")
               .append(topHotspot.getLongitude())
               .append(":")
               .append(topHotspot.getThreatType());
        }
        
        return key.toString();
    }
    
    private void cleanupOldEntries(LocalDateTime now) {
        recentAlerts.entrySet().removeIf(entry -> {
            LocalDateTime lastProcessed = entry.getValue();
            return lastProcessed.plusNanos(deduplicationWindow * 1_000_000).isBefore(now);
        });
    }
    
    public void clearCache() {
        recentAlerts.clear();
        log.info("Alert deduplication cache cleared");
    }
    
    public int getCacheSize() {
        return recentAlerts.size();
    }
}

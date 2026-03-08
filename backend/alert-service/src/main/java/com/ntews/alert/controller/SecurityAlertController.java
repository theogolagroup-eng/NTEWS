package com.ntews.alert.controller;

import com.ntews.alert.model.SecurityAlert;
import com.ntews.alert.service.SecurityAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * Real-time Security Monitoring Controller
 * Handles WebSocket connections and Sheng-aware security alerts
 */
@RestController
@RequestMapping("/api/security-alerts")
@RequiredArgsConstructor
@Slf4j
public class SecurityAlertController {
    
    private final SecurityAlertService securityAlertService;
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * WebSocket endpoint for real-time security alerts
     */
    @MessageMapping("/security-event")
    public void handleSecurityEvent(SecurityAlert alert) {
        try {
            log.info("Received security alert: {} | Risk: {} | Language: {}", 
                alert.getId(), alert.getRiskCategory(), alert.getOriginalLanguage());
            
            // Process the alert
            securityAlertService.processSecurityAlert(alert);
            
            // Broadcast to all connected clients
            messagingTemplate.convertAndSend("/topic/security-alerts", alert);
            
        } catch (Exception e) {
            log.error("Error processing security alert: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Get current security statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getSecurityStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("active_monitoring", true);
            stats.put("timestamp", LocalDateTime.now());
            stats.put("alerts_today", securityAlertService.getAlertsCountToday());
            stats.put("high_risk_alerts", securityAlertService.getHighRiskAlertsCount());
            stats.put("sheng_detected_count", securityAlertService.getShengDetectedCount());
            stats.put("engagement_alerts", securityAlertService.getEngagementAlertsCount());
            stats.put("trending_hashtags", securityAlertService.getTrendingHashtags());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting security statistics: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Get trending security topics
     */
    @GetMapping("/trending-topics")
    public ResponseEntity<Map<String, Object>> getTrendingTopics() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("trending_hashtags", securityAlertService.getTrendingHashtags());
            response.put("sheng_keywords", securityAlertService.getTopShengKeywords());
            response.put("daily_statistics", securityAlertService.getDailyStatistics());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting trending topics: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Manual alert trigger for testing
     */
    @PostMapping("/trigger-alert")
    public ResponseEntity<Map<String, Object>> triggerManualAlert(@RequestBody Map<String, Object> request) {
        try {
            SecurityAlert manualAlert = createManualAlert(request);
            
            // Process the alert
            securityAlertService.processSecurityAlert(manualAlert);
            
            // Broadcast to all connected clients
            messagingTemplate.convertAndSend("/topic/security-alerts", manualAlert);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Manual alert triggered successfully");
            response.put("alert_id", manualAlert.getId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error triggering manual alert: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Create manual security alert for testing
     */
    private SecurityAlert createManualAlert(Map<String, Object> request) {
        SecurityAlert alert = new SecurityAlert();
        alert.setId((String) request.getOrDefault("id", "manual-" + System.currentTimeMillis()));
        alert.setTitle((String) request.getOrDefault("title", "Manual Test Alert"));
        alert.setDescription((String) request.getOrDefault("text", "Manual test alert"));
        alert.setTimestamp(LocalDateTime.now());
        alert.setSeverity((String) request.getOrDefault("severity", "MEDIUM"));
        alert.setStatus("ACTIVE");
        alert.setCategory((String) request.getOrDefault("category", "SECURITY"));
        alert.setSource("Manual Test");
        alert.setLocation("Test Location");
        
        // Set AI analysis fields
        alert.setRiskCategory((String) request.getOrDefault("category", "benign"));
        alert.setConfidence(0.9);
        alert.setThreatScore(0.5);
        alert.setDetectedLanguage("english");
        alert.setProcessingMethod("manual");
        
        // Set engagement metrics if provided
        if (request.containsKey("retweets")) {
            alert.calculateEngagementMetrics(
                ((Number) request.get("retweets")).longValue(),
                ((Number) request.getOrDefault("replies", 0)).longValue(),
                ((Number) request.getOrDefault("likes", 0)).longValue(),
                ((Number) request.getOrDefault("quotes", 0)).longValue()
            );
        }
        
        return alert;
    }
}

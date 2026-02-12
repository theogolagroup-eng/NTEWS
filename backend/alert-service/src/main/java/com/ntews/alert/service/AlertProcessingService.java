package com.ntews.alert.service;

import com.ntews.alert.model.IntelligenceReport;
import com.ntews.alert.model.RiskForecast;
import com.ntews.alert.model.Alert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
// Kafka disabled for local development
// import org.springframework.kafka.annotation.KafkaListener;
// import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class AlertProcessingService {
    
    // Kafka disabled for local development
    // @Autowired
    // private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    private AlertNotificationService notificationService;
    
    @Autowired
    private AlertDeduplicationService deduplicationService;
    
    @Value("${alerts.thresholds.threat-score.critical:0.8}")
    private Double criticalThreatScore;
    
    @Value("${alerts.thresholds.threat-score.high:0.7}")
    private Double highThreatScore;
    
    @Value("${alerts.thresholds.threat-score.medium:0.4}")
    private Double mediumThreatScore;
    
    // Cache for recent alerts to avoid duplicates
    private final Map<String, LocalDateTime> recentAlerts = new ConcurrentHashMap<>();
    
    // Kafka listener disabled for local development
    // @KafkaListener(topics = "intelligence-reports", groupId = "alert-service")
    public void processIntelligenceReport(IntelligenceReport report) {
        try {
            log.debug("Processing intelligence report for alert generation: {}", report.getId());
            
            // Check if alert should be generated based on threat score
            if (report.getThreatScore() >= mediumThreatScore) {
                
                // Check for duplicates
                if (deduplicationService.isDuplicate(report)) {
                    log.debug("Duplicate intelligence report detected, skipping alert generation: {}", report.getId());
                    return;
                }
                
                // Generate alert
                Alert alert = generateAlertFromIntelligenceReport(report);
                
                // Process alert
                processAlert(alert);
                
                // Cache the alert for deduplication
                recentAlerts.put(report.getId(), LocalDateTime.now());
            }
            
        } catch (Exception e) {
            log.error("Error processing intelligence report for alert generation: {}", e.getMessage(), e);
        }
    }
    
    // Kafka listener disabled for local development
    // @KafkaListener(topics = "risk-forecasts", groupId = "alert-service")
    public void processRiskForecast(RiskForecast forecast) {
        try {
            log.debug("Processing risk forecast for alert generation: {}", forecast.getId());
            
            // Check if forecast indicates high risk
            if (forecast.getOverallRiskTrend() != null && forecast.getOverallRiskTrend() >= highThreatScore) {
                
                // Check for duplicates
                if (deduplicationService.isDuplicate(forecast)) {
                    log.debug("Duplicate risk forecast detected, skipping alert generation: {}", forecast.getId());
                    return;
                }
                
                // Generate alert
                Alert alert = generateAlertFromRiskForecast(forecast);
                
                // Process alert
                processAlert(alert);
                
                // Cache the forecast for deduplication
                recentAlerts.put(forecast.getId(), LocalDateTime.now());
            }
            
        } catch (Exception e) {
            log.error("Error processing risk forecast for alert generation: {}", e.getMessage(), e);
        }
    }
    
    // Kafka listener disabled for local development
    // @KafkaListener(topics = "hotspot-forecasts", groupId = "alert-service")
    public void processHotspotForecast(RiskForecast forecast) {
        try {
            log.debug("Processing hotspot forecast for alert generation: {}", forecast.getId());
            
            // Check if there are high-probability hotspots
            if (forecast.getHotspots() != null) {
                List<RiskForecast.HotspotPrediction> highRiskHotspots = forecast.getHotspots().stream()
                    .filter(hotspot -> hotspot.getProbability() >= highThreatScore)
                    .toList();
                
                if (!highRiskHotspots.isEmpty()) {
                    
                    // Check for duplicates
                    if (deduplicationService.isDuplicate(forecast)) {
                        log.debug("Duplicate hotspot forecast detected, skipping alert generation: {}", forecast.getId());
                        return;
                    }
                    
                    // Generate alert for each high-risk hotspot
                    for (RiskForecast.HotspotPrediction hotspot : highRiskHotspots) {
                        Alert alert = generateAlertFromHotspot(forecast, hotspot);
                        processAlert(alert);
                    }
                    
                    // Cache the forecast for deduplication
                    recentAlerts.put(forecast.getId(), LocalDateTime.now());
                }
            }
            
        } catch (Exception e) {
            log.error("Error processing hotspot forecast for alert generation: {}", e.getMessage(), e);
        }
    }
    
    private void processAlert(Alert alert) {
        try {
            // Send alert to Kafka for distribution (Kafka disabled for local development)
            // kafkaTemplate.send("alerts", alert.getId(), alert);
            
            // Send real-time notifications
            notificationService.sendNotifications(alert);
            
            // Log alert generation
            log.info("Generated alert: {} - {} - {}", 
                    alert.getId(), alert.getType().getValue(), alert.getSeverity().getValue());
            
        } catch (Exception e) {
            log.error("Error processing alert: {}", e.getMessage(), e);
        }
    }
    
    private Alert generateAlertFromIntelligenceReport(IntelligenceReport report) {
        // Determine severity based on threat score
        Alert.Severity severity = determineSeverity(report.getThreatScore());
        
        // Determine priority
        Alert.Priority priority = determinePriority(severity, report.getThreatLevel());
        
        return Alert.builder()
            .id(UUID.randomUUID().toString())
            .title("Threat Intelligence Alert")
            .description(report.getSummary())
            .summary(generateSummary(report))
            .type(Alert.AlertType.THREAT_DETECTION)
            .severity(severity)
            .priority(priority)
            .category(report.getCategory().getValue())
            .sourceId(report.getId())
            .sourceType("intelligence_report")
            .source("Intelligence Service")
            .timestamp(report.getCreatedAt())
            .expiresAt(LocalDateTime.now().plusHours(24))
            .location(convertLocation(report.getLocation()))
            .content(Map.of(
                "intelligence_report", report,
                "threat_score", report.getThreatScore(),
                "confidence", report.getConfidence(),
                "recommendations", report.getRecommendations()
            ))
            .tags(Arrays.asList("threat", "intelligence", report.getCategory().getValue()))
            .keywords(extractKeywords(report))
            .status(Alert.AlertStatus.ACTIVE)
            .createdAt(LocalDateTime.now())
            .verified(false)
            .confidence(report.getConfidence())
            .threatLevel(report.getThreatLevel().getValue())
            .notifications(buildNotificationInfo(severity))
            .build();
    }
    
    private Alert generateAlertFromRiskForecast(RiskForecast forecast) {
        // Determine severity based on risk trend
        Alert.Severity severity = determineSeverity(forecast.getOverallRiskTrend());
        
        return Alert.builder()
            .id(UUID.randomUUID().toString())
            .title("Risk Forecast Alert")
            .description("Elevated risk levels predicted for the next " + 
                        (forecast.getValidTo().getHour() - forecast.getValidFrom().getHour()) + " hours")
            .summary("Risk trend analysis indicates " + severity.getValue() + " risk level")
            .type(Alert.AlertType.RISK_FORECAST)
            .severity(severity)
            .priority(determinePriority(severity, null))
            .category("forecast")
            .sourceId(forecast.getId())
            .sourceType("risk_forecast")
            .source("Prediction Service")
            .timestamp(forecast.getGeneratedAt())
            .expiresAt(forecast.getValidTo())
            .content(Map.of(
                "risk_forecast", forecast,
                "overall_risk_trend", forecast.getOverallRiskTrend(),
                "confidence", forecast.getConfidenceScore(),
                "forecast_points", forecast.getForecastPoints()
            ))
            .tags(Arrays.asList("forecast", "risk", "prediction"))
            .keywords(Arrays.asList("risk", "trend", "forecast", "prediction"))
            .status(Alert.AlertStatus.ACTIVE)
            .createdAt(LocalDateTime.now())
            .verified(false)
            .confidence(forecast.getConfidenceScore())
            .threatLevel(severity.getValue())
            .notifications(buildNotificationInfo(severity))
            .build();
    }
    
    private Alert generateAlertFromHotspot(RiskForecast forecast, RiskForecast.HotspotPrediction hotspot) {
        // Determine severity based on hotspot probability
        Alert.Severity severity = determineSeverity(hotspot.getProbability());
        
        Alert.LocationInfo location = new Alert.LocationInfo();
        location.setLatitude(hotspot.getLatitude());
        location.setLongitude(hotspot.getLongitude());
        location.setCity(hotspot.getLocationName());
        location.setRadius(hotspot.getRadius());
        
        return Alert.builder()
            .id(UUID.randomUUID().toString())
            .title("Hotspot Alert: " + hotspot.getLocationName())
            .description("High probability threat hotspot detected: " + hotspot.getThreatType())
            .summary("Hotspot prediction indicates " + severity.getValue() + " risk in " + 
                     hotspot.getLocationName() + " around " + hotspot.getPeakTime())
            .type(Alert.AlertType.RISK_FORECAST)
            .severity(severity)
            .priority(determinePriority(severity, null))
            .category("hotspot")
            .sourceId(forecast.getId())
            .sourceType("hotspot_forecast")
            .source("Prediction Service")
            .timestamp(forecast.getGeneratedAt())
            .expiresAt(hotspot.getPeakTime().plusHours(2))
            .location(location)
            .content(Map.of(
                "hotspot_prediction", hotspot,
                "risk_forecast", forecast,
                "probability", hotspot.getProbability(),
                "peak_time", hotspot.getPeakTime(),
                "threat_type", hotspot.getThreatType()
            ))
            .tags(Arrays.asList("hotspot", "location", hotspot.getThreatType()))
            .keywords(Arrays.asList("hotspot", hotspot.getLocationName(), hotspot.getThreatType()))
            .status(Alert.AlertStatus.ACTIVE)
            .createdAt(LocalDateTime.now())
            .verified(false)
            .confidence(hotspot.getConfidence())
            .threatLevel(severity.getValue())
            .notifications(buildNotificationInfo(severity))
            .build();
    }
    
    private Alert.Severity determineSeverity(Double score) {
        if (score >= criticalThreatScore) return Alert.Severity.CRITICAL;
        if (score >= highThreatScore) return Alert.Severity.HIGH;
        if (score >= mediumThreatScore) return Alert.Severity.MEDIUM;
        return Alert.Severity.LOW;
    }
    
    private Alert.Priority determinePriority(Alert.Severity severity, IntelligenceReport.ThreatLevel threatLevel) {
        if (severity == Alert.Severity.CRITICAL || 
            (threatLevel != null && threatLevel.getSeverity() >= 3)) {
            return Alert.Priority.URGENT;
        }
        if (severity == Alert.Severity.HIGH) {
            return Alert.Priority.HIGH;
        }
        if (severity == Alert.Severity.MEDIUM) {
            return Alert.Priority.NORMAL;
        }
        return Alert.Priority.LOW;
    }
    
    private String generateSummary(IntelligenceReport report) {
        return String.format("Threat Level: %s, Score: %.2f, Location: %s", 
                           report.getThreatLevel().getValue(), 
                           report.getThreatScore(),
                           report.getLocation() != null ? report.getLocation().getAddress() : "Unknown");
    }
    
    private Alert.LocationInfo convertLocation(IntelligenceReport.LocationInfo intelLocation) {
        if (intelLocation == null) return null;
        
        return Alert.LocationInfo.builder()
            .latitude(intelLocation.getLatitude())
            .longitude(intelLocation.getLongitude())
            .city(intelLocation.getCity())
            .region(intelLocation.getRegion())
            .country(intelLocation.getCountry())
            .address(intelLocation.getAddress())
            .radius(intelLocation.getRadius())
            .geoHash(intelLocation.getGeoHash())
            .build();
    }
    
    private List<String> extractKeywords(IntelligenceReport report) {
        List<String> keywords = new ArrayList<>();
        
        if (report.getAiAnalysis() != null) {
            keywords.addAll(report.getAiAnalysis().getThreatKeywords());
            keywords.addAll(report.getAiAnalysis().getKeyEntities());
        }
        
        keywords.add(report.getCategory().getValue());
        keywords.add(report.getThreatLevel().getValue());
        
        return keywords;
    }
    
    private Alert.NotificationInfo buildNotificationInfo(Alert.Severity severity) {
        List<String> channels = new ArrayList<>();
        channels.add("websocket");  // Always send WebSocket
        
        if (severity.getLevel() >= 2) {  // Medium and above
            channels.add("email");
        }
        
        if (severity.getLevel() >= 3) {  // High and Critical
            channels.add("sms");
        }
        
        return Alert.NotificationInfo.builder()
            .channels(channels)
            .recipients(Arrays.asList("security-team", "analysts"))
            .notificationCount(0)
            .notificationSent(false)
            .build();
    }
}

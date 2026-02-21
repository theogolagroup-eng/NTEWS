package com.ntews.alert.controller;

import com.ntews.alert.model.Alert;
import com.ntews.alert.service.AlertService;
import com.ntews.alert.service.NLPAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Slf4j
public class AlertController {
    
    private final AlertService alertService;
    private final NLPAnalysisService nlpAnalysisService;
    
    @GetMapping
    public ResponseEntity<Page<Alert>> getAlerts(
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        
        Page<Alert> alerts = alertService.getAlerts(severity, status, category, startDate, endDate, pageable);
        return ResponseEntity.ok(alerts);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Alert> getAlert(@PathVariable UUID id) {
        return alertService.getAlert(id.toString())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Alert> createAlert(@RequestBody Alert alert) {
        Alert createdAlert = alertService.createAlert(alert);
        return ResponseEntity.ok(createdAlert);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Alert> updateAlert(@PathVariable UUID id, @RequestBody Alert alert) {
        alert.setId(id.toString());
        Alert updatedAlert = alertService.updateAlert(alert);
        return ResponseEntity.ok(updatedAlert);
    }
    
    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<Alert> acknowledgeAlert(@PathVariable UUID id) {
        Alert acknowledgedAlert = alertService.acknowledgeAlert(id.toString());
        return ResponseEntity.ok(acknowledgedAlert);
    }
    
    @PostMapping("/{id}/resolve")
    public ResponseEntity<Alert> resolveAlert(
            @PathVariable UUID id, @RequestBody ResolutionRequest request) {
        
        Alert resolvedAlert = alertService.resolveAlert(id.toString(), request.getResolutionNotes());
        return ResponseEntity.ok(resolvedAlert);
    }
    
    @PostMapping("/{id}/assign")
    public ResponseEntity<Alert> assignAlert(
            @PathVariable UUID id, @RequestBody AssignmentRequest request) {
        
        Alert assignedAlert = alertService.assignAlert(id.toString(), request.getAssignedTo());
        return ResponseEntity.ok(assignedAlert);
    }
    
    @GetMapping("/dashboard/summary")
    public ResponseEntity<AlertDashboardSummary> getDashboardSummary() {
        AlertDashboardSummary summary = alertService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<Alert>> getActiveAlerts() {
        List<Alert> activeAlerts = alertService.getActiveAlerts();
        return ResponseEntity.ok(activeAlerts);
    }
    
    @GetMapping("/unacknowledged")
    public ResponseEntity<List<Alert>> getUnacknowledgedAlerts() {
        List<Alert> unacknowledgedAlerts = alertService.getUnacknowledgedAlerts();
        return ResponseEntity.ok(unacknowledgedAlerts);
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<AlertStatistics> getAlertStatistics(
            @RequestParam(defaultValue = "7") int days) {
        
        AlertStatistics statistics = alertService.getAlertStatistics(days);
        return ResponseEntity.ok(statistics);
    }
    
    // NLP Analysis Endpoints
    @PostMapping("/nlp/analyze-text")
    public ResponseEntity<Map<String, Object>> analyzeTextThreat(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            String context = request.get("context");
            
            Map<String, Object> result = nlpAnalysisService.analyzeTextThreat(text, context);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error analyzing text threat", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/nlp/batch-analyze")
    public ResponseEntity<Map<String, Object>> batchAnalyzeTexts(@RequestBody List<Map<String, String>> requests) {
        try {
            Map<String, Object> result = nlpAnalysisService.batchAnalyzeTexts(requests);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error in batch text analysis", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/nlp/capabilities")
    public ResponseEntity<Map<String, Object>> getNLPCapabilities() {
        try {
            Map<String, Object> capabilities = nlpAnalysisService.getNLPCapabilities();
            return ResponseEntity.ok(capabilities);
            
        } catch (Exception e) {
            log.error("Error getting NLP capabilities", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/{alertId}/nlp-analyze")
    public ResponseEntity<Alert> analyzeAlertWithNLP(@PathVariable String alertId) {
        try {
            Alert alert = alertService.getAlertById(alertId);
            if (alert == null) {
                return ResponseEntity.notFound().build();
            }
            
            Alert analyzedAlert = nlpAnalysisService.analyzeAlertWithNLP(alert);
            Alert savedAlert = alertService.updateAlert(analyzedAlert);
            
            log.info("NLP analysis completed for alert: {}", alertId);
            return ResponseEntity.ok(savedAlert);
            
        } catch (Exception e) {
            log.error("Error analyzing alert with NLP", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Essential DTOs only - removed redundant ones
    public static class ResolutionRequest {
        private String resolutionNotes;
        
        public String getResolutionNotes() { return resolutionNotes; }
        public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
    }
    
    public static class AssignmentRequest {
        private String assignedTo;
        
        public String getAssignedTo() { return assignedTo; }
        public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    }
    
    // Simplified Dashboard Summary
    public static class AlertDashboardSummary {
        private int totalAlerts;
        private int activeAlerts;
        private int unacknowledgedAlerts;
        private int criticalAlerts;
        private int highAlerts;
        private int mediumAlerts;
        private int lowAlerts;
        
        // AI Engine integration fields
        private int highConfidenceAlerts;
        private Map<String, Long> aiRiskLevels;
        private double averageAiConfidence;
        private boolean aiEngineHealthy;
        
        // Additional fields for dashboard
        private Map<String, Integer> severityCounts;
        private List<RecentAlert> recentAlerts;
        
        // Getters and setters
        public int getTotalAlerts() { return totalAlerts; }
        public void setTotalAlerts(int totalAlerts) { this.totalAlerts = totalAlerts; }
        public int getActiveAlerts() { return activeAlerts; }
        public void setActiveAlerts(int activeAlerts) { this.activeAlerts = activeAlerts; }
        public int getUnacknowledgedAlerts() { return unacknowledgedAlerts; }
        public void setUnacknowledgedAlerts(int unacknowledgedAlerts) { this.unacknowledgedAlerts = unacknowledgedAlerts; }
        public int getCriticalAlerts() { return criticalAlerts; }
        public void setCriticalAlerts(int criticalAlerts) { this.criticalAlerts = criticalAlerts; }
        public int getHighAlerts() { return highAlerts; }
        public void setHighAlerts(int highAlerts) { this.highAlerts = highAlerts; }
        public int getMediumAlerts() { return mediumAlerts; }
        public void setMediumAlerts(int mediumAlerts) { this.mediumAlerts = mediumAlerts; }
        public int getLowAlerts() { return lowAlerts; }
        public void setLowAlerts(int lowAlerts) { this.lowAlerts = lowAlerts; }
        public int getHighConfidenceAlerts() { return highConfidenceAlerts; }
        public void setHighConfidenceAlerts(int highConfidenceAlerts) { this.highConfidenceAlerts = highConfidenceAlerts; }
        public Map<String, Long> getAiRiskLevels() { return aiRiskLevels; }
        public void setAiRiskLevels(Map<String, Long> aiRiskLevels) { this.aiRiskLevels = aiRiskLevels; }
        public double getAverageAiConfidence() { return averageAiConfidence; }
        public void setAverageAiConfidence(double averageAiConfidence) { this.averageAiConfidence = averageAiConfidence; }
        public boolean isAiEngineHealthy() { return aiEngineHealthy; }
        public void setAiEngineHealthy(boolean aiEngineHealthy) { this.aiEngineHealthy = aiEngineHealthy; }
        
        // Additional methods for dashboard summary
        public List<AlertSeverityCount> getSeverityCounts() { 
            return severityCounts != null ? new ArrayList<>() : new ArrayList<>(severityCounts.entrySet().stream()
                .map(entry -> new AlertSeverityCount(entry.getKey(), entry.getValue().intValue()))
                .collect(Collectors.toList()));
        }
        
        public void setSeverityCounts(List<AlertSeverityCount> severityCounts) { 
            this.severityCounts = severityCounts != null ? new HashMap<>() : severityCounts.stream()
                .collect(Collectors.toMap(AlertSeverityCount::getSeverity, AlertSeverityCount::getCount));
        }
        
        public List<RecentAlert> getRecentAlerts() {
            return recentAlerts != null ? new ArrayList<>() : new ArrayList<>(recentAlerts);
        }
        
        public void setRecentAlerts(List<RecentAlert> recentAlerts) {
            this.recentAlerts = recentAlerts;
        }
    }
    
    // Simplified Statistics
    public static class AlertStatistics {
        private List<AlertTrend> trends;
        private List<AlertHourlyCount> hourlyCounts;
        private List<AlertCategoryCount> categoryCounts;
        private double averageResponseTime;
        private double resolutionRate;
        
        // Getters and setters
        public List<AlertTrend> getTrends() { return trends; }
        public void setTrends(List<AlertTrend> trends) { this.trends = trends; }
        public List<AlertHourlyCount> getHourlyCounts() { return hourlyCounts; }
        public void setHourlyCounts(List<AlertHourlyCount> hourlyCounts) { this.hourlyCounts = hourlyCounts; }
        public List<AlertCategoryCount> getCategoryCounts() { return categoryCounts; }
        public void setCategoryCounts(List<AlertCategoryCount> categoryCounts) { this.categoryCounts = categoryCounts; }
        public double getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }
        public double getResolutionRate() { return resolutionRate; }
        public void setResolutionRate(double resolutionRate) { this.resolutionRate = resolutionRate; }
    }
    
    public static class AlertTrend {
        private String date;
        private int count;
        
        public AlertTrend(String date, int count) {
            this.date = date;
            this.count = count;
        }
        
        public String getDate() { return date; }
        public int getCount() { return count; }
    }
    
    public static class AlertHourlyCount {
        private int hour;
        private int count;
        
        public AlertHourlyCount(int hour, int count) {
            this.hour = hour;
            this.count = count;
        }
        
        public int getHour() { return hour; }
        public int getCount() { return count; }
    }
    
    public static class AlertCategoryCount {
        private String category;
        private int count;
        
        public AlertCategoryCount(String category, int count) {
            this.category = category;
            this.count = count;
        }
        
        public String getCategory() { return category; }
        public int getCount() { return count; }
    }
    
    public static class AlertSeverityCount {
        private String severity;
        private int count;
        
        public AlertSeverityCount(String severity, int count) {
            this.severity = severity;
            this.count = count;
        }
        
        public String getSeverity() { return severity; }
        public int getCount() { return count; }
    }
    
    public static class RecentAlert {
        private String id;
        private String title;
        private String severity;
        private String status;
        private LocalDateTime timestamp;
        private String location;
        
        public RecentAlert(String id, String title, String severity, String status, LocalDateTime timestamp, String location) {
            this.id = id;
            this.title = title;
            this.severity = severity;
            this.status = status;
            this.timestamp = timestamp;
            this.location = location;
        }
        
        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getSeverity() { return severity; }
        public String getStatus() { return status; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getLocation() { return location; }
    }
}

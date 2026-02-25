package com.ntews.intel.controller;

import com.ntews.intel.model.IntelligenceReport;
import com.ntews.intel.model.Prediction;
import com.ntews.intel.service.IntelligenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/intelligence")
@RequiredArgsConstructor
@Slf4j
public class IntelligenceController {
    
    private final IntelligenceService intelligenceService;
    
    @GetMapping("/reports")
    public ResponseEntity<Page<IntelligenceReport>> getIntelligenceReports(
            @RequestParam(required = false) String threatLevel,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        
        Page<IntelligenceReport> reports = intelligenceService.getIntelligenceReports(
                threatLevel, category, startDate, endDate, pageable);
        
        return ResponseEntity.ok(reports);
    }
    
    @GetMapping("/reports/{id}")
    public ResponseEntity<IntelligenceReport> getIntelligenceReport(@PathVariable String id) {
        return intelligenceService.getIntelligenceReport(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/reports")
    public ResponseEntity<IntelligenceReport> createIntelligenceReport(@RequestBody IntelligenceReport report) {
        IntelligenceReport createdReport = intelligenceService.createIntelligenceReport(report);
        return ResponseEntity.ok(createdReport);
    }
    
    @PutMapping("/reports/{id}")
    public ResponseEntity<IntelligenceReport> updateIntelligenceReport(
            @PathVariable String id, @RequestBody IntelligenceReport report) {
        
        report.setId(id);
        IntelligenceReport updatedReport = intelligenceService.updateIntelligenceReport(report);
        return ResponseEntity.ok(updatedReport);
    }
    
    @PostMapping("/reports/{id}/verify")
    public ResponseEntity<IntelligenceReport> verifyReport(
            @PathVariable String id, @RequestBody VerificationRequest request) {
        
        IntelligenceReport verifiedReport = intelligenceService.verifyReport(id, request.isVerified(), request.getNotes());
        return ResponseEntity.ok(verifiedReport);
    }
    
    @GetMapping("/dashboard/summary")
    public ResponseEntity<DashboardSummary> getDashboardSummary() {
        DashboardSummary summary = intelligenceService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/threat-trends")
    public ResponseEntity<List<ThreatTrend>> getThreatTrends(
            @RequestParam(defaultValue = "7") int days) {
        
        List<ThreatTrend> trends = intelligenceService.getThreatTrends(days);
        return ResponseEntity.ok(trends);
    }
    
    @GetMapping("/threat-map")
    public ResponseEntity<List<ThreatLocation>> getThreatLocations(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        
        List<ThreatLocation> locations = intelligenceService.getThreatLocations(since);
        return ResponseEntity.ok(locations);
    }
    
    @GetMapping("/predictions")
    public ResponseEntity<List<Prediction>> getPredictions(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since,
            @RequestParam(defaultValue = "24") int hours) {
        
        List<Prediction> predictions = intelligenceService.getPredictions(type, since, hours);
        return ResponseEntity.ok(predictions);
    }
    
    // DTOs
    public static class VerificationRequest {
        private boolean verified;
        private String notes;
        
        public boolean isVerified() { return verified; }
        public void setVerified(boolean verified) { this.verified = verified; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
    
    public static class DashboardSummary {
        private int totalReports;
        private int activeThreats;
        private int criticalThreats;
        private int highThreats;
        private int mediumThreats;
        private int lowThreats;
        private List<ThreatCategoryCount> categoryCounts;
        private List<RecentThreat> recentThreats;
        
        // Getters and setters
        public int getTotalReports() { return totalReports; }
        public void setTotalReports(int totalReports) { this.totalReports = totalReports; }
        public int getActiveThreats() { return activeThreats; }
        public void setActiveThreats(int activeThreats) { this.activeThreats = activeThreats; }
        public int getCriticalThreats() { return criticalThreats; }
        public void setCriticalThreats(int criticalThreats) { this.criticalThreats = criticalThreats; }
        public int getHighThreats() { return highThreats; }
        public void setHighThreats(int highThreats) { this.highThreats = highThreats; }
        public int getMediumThreats() { return mediumThreats; }
        public void setMediumThreats(int mediumThreats) { this.mediumThreats = mediumThreats; }
        public int getLowThreats() { return lowThreats; }
        public void setLowThreats(int lowThreats) { this.lowThreats = lowThreats; }
        public List<ThreatCategoryCount> getCategoryCounts() { return categoryCounts; }
        public void setCategoryCounts(List<ThreatCategoryCount> categoryCounts) { this.categoryCounts = categoryCounts; }
        public List<RecentThreat> getRecentThreats() { return recentThreats; }
        public void setRecentThreats(List<RecentThreat> recentThreats) { this.recentThreats = recentThreats; }
    }
    
    public static class ThreatCategoryCount {
        private String category;
        private int count;
        
        public ThreatCategoryCount(String category, int count) {
            this.category = category;
            this.count = count;
        }
        
        public String getCategory() { return category; }
        public int getCount() { return count; }
    }
    
    public static class RecentThreat {
        private String id;
        private String title;
        private String threatLevel;
        private LocalDateTime timestamp;
        private String location;
        
        public RecentThreat(String id, String title, String threatLevel, LocalDateTime timestamp, String location) {
            this.id = id;
            this.title = title;
            this.threatLevel = threatLevel;
            this.timestamp = timestamp;
            this.location = location;
        }
        
        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getThreatLevel() { return threatLevel; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getLocation() { return location; }
    }
    
    public static class ThreatTrend {
        private String date;
        private int count;
        private double avgThreatScore;
        
        public ThreatTrend(String date, int count, double avgThreatScore) {
            this.date = date;
            this.count = count;
            this.avgThreatScore = avgThreatScore;
        }
        
        public String getDate() { return date; }
        public int getCount() { return count; }
        public double getAvgThreatScore() { return avgThreatScore; }
    }
    
    public static class ThreatLocation {
        private String id;
        private String latitude;
        private String longitude;
        private String location;
        private String threatLevel;
        private double threatScore;
        private LocalDateTime timestamp;
        
        public ThreatLocation(String id, String latitude, String longitude, String location, 
                             String threatLevel, double threatScore, LocalDateTime timestamp) {
            this.id = id;
            this.latitude = latitude;
            this.longitude = longitude;
            this.location = location;
            this.threatLevel = threatLevel;
            this.threatScore = threatScore;
            this.timestamp = timestamp;
        }
        
        public String getId() { return id; }
        public String getLatitude() { return latitude; }
        public String getLongitude() { return longitude; }
        public String getLocation() { return location; }
        public String getThreatLevel() { return threatLevel; }
        public double getThreatScore() { return threatScore; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}

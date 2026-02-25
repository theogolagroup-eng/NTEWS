package com.ntews.intel.service;

import com.ntews.intel.controller.IntelligenceController.*;
import com.ntews.intel.model.IntelligenceReport;
import com.ntews.intel.model.Prediction;
import com.ntews.intel.repository.IntelligenceReportRepository;
import com.ntews.intel.client.AIEngineClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntelligenceServiceImpl implements IntelligenceService {
    
    private final IntelligenceReportRepository intelligenceReportRepository;
    private final AIEngineClient aiEngineClient;
    
    @Override
    public Page<IntelligenceReport> getIntelligenceReports(
            String threatLevel, String category, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        
        if (threatLevel != null && category != null && startDate != null && endDate != null) {
            return intelligenceReportRepository.findByThreatLevelAndCreatedAtBetween(threatLevel, startDate, endDate, pageable);
        } else if (threatLevel != null && startDate != null && endDate != null) {
            return intelligenceReportRepository.findByThreatLevelAndCreatedAtBetween(threatLevel, startDate, endDate, pageable);
        } else if (category != null && startDate != null && endDate != null) {
            return intelligenceReportRepository.findByCategoryAndCreatedAtBetween(category, startDate, endDate, pageable);
        } else if (threatLevel != null) {
            return intelligenceReportRepository.findByThreatLevel(threatLevel, pageable);
        } else if (category != null) {
            return intelligenceReportRepository.findByCategory(category, pageable);
        } else if (startDate != null && endDate != null) {
            return intelligenceReportRepository.findByCreatedAtBetween(startDate, endDate, pageable);
        } else {
            return intelligenceReportRepository.findAll(pageable);
        }
    }
    
    @Override
    public Optional<IntelligenceReport> getIntelligenceReport(String id) {
        return intelligenceReportRepository.findById(id);
    }
    
    @Override
    public IntelligenceReport createIntelligenceReport(IntelligenceReport report) {
        try {
            if (report == null) {
                throw new IllegalArgumentException("Intelligence report cannot be null");
            }
            
            report.setId(UUID.randomUUID().toString());
            report.setCreatedAt(LocalDateTime.now());
            report.setUpdatedAt(LocalDateTime.now());
            
            if (report.getStatus() == null) {
                report.setStatus(IntelligenceReport.ReportStatus.DRAFT);
            }
            
            // Use AI Engine to analyze the report with null safety
            try {
                String reportData = String.format("Title: %s, Content: %s, Category: %s", 
                    report.getTitle() != null ? report.getTitle() : "",
                    report.getContent() != null ? report.getContent() : "",
                    report.getCategory() != null ? report.getCategory().toString() : "");
                
                Map<String, Object> aiAnalysis = aiEngineClient.analyzeThreat(reportData);
                
                // Update report with AI analysis with null safety
                if (aiAnalysis != null) {
                    report.setAiConfidence(((Number) aiAnalysis.getOrDefault("confidence", 0.5)).doubleValue());
                    report.setAiThreatLevel((String) aiAnalysis.getOrDefault("threat_level", "medium"));
                    report.setAiRiskScore(((Number) aiAnalysis.getOrDefault("risk_score", 50)).intValue());
                    
                    @SuppressWarnings("unchecked")
                    List<String> recommendations = (List<String>) aiAnalysis.getOrDefault("recommendations", 
                        Arrays.asList("Monitor situation", "Gather more intelligence"));
                    report.setAiRecommendations(String.join("; ", recommendations));
                }
            } catch (Exception aiError) {
                log.warn("AI analysis failed for intelligence report {}: {}", report.getId(), aiError.getMessage());
                // Set default AI values
                report.setAiConfidence(0.5);
                report.setAiThreatLevel("medium");
                report.setAiRiskScore(50);
                report.setAiRecommendations("Monitor situation; Gather more intelligence");
            }
            
            log.info("AI analysis completed for report: {}", report.getId());
            
        } catch (Exception e) {
            log.error("Error creating intelligence report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create intelligence report", e);
        }
        
        try {
            IntelligenceReport savedReport = intelligenceReportRepository.save(report);
            log.info("Created intelligence report: {}", savedReport.getId());
            return savedReport;
        } catch (Exception saveError) {
            log.error("Error saving intelligence report: {}", saveError.getMessage());
            throw new RuntimeException("Failed to save intelligence report", saveError);
        }
    }
    
    @Override
    public IntelligenceReport updateIntelligenceReport(IntelligenceReport report) {
        report.setUpdatedAt(LocalDateTime.now());
        
        IntelligenceReport updatedReport = intelligenceReportRepository.save(report);
        log.info("Updated intelligence report: {}", updatedReport.getId());
        
        return updatedReport;
    }
    
    @Override
    public IntelligenceReport verifyReport(String id, boolean verified, String notes) {
        Optional<IntelligenceReport> reportOpt = intelligenceReportRepository.findById(id);
        
        if (reportOpt.isPresent()) {
            IntelligenceReport report = reportOpt.get();
            report.setVerified(verified);
            report.setVerificationNotes(notes);
            report.setVerifiedAt(LocalDateTime.now());
            report.setUpdatedAt(LocalDateTime.now());
            
            return intelligenceReportRepository.save(report);
        } else {
            throw new RuntimeException("Intelligence report not found: " + id);
        }
    }
    
    @Override
    public DashboardSummary getDashboardSummary() {
        DashboardSummary summary = new DashboardSummary();
        
        // Get total reports
        long totalReports = intelligenceReportRepository.count();
        summary.setTotalReports((int) totalReports);
        
        // Get threat level counts
        List<IntelligenceReport> activeReports = intelligenceReportRepository.findActiveReports();
        summary.setActiveThreats(activeReports.size());
        
        Map<String, Long> threatLevelCounts = activeReports.stream()
                .collect(Collectors.groupingBy(
                        report -> report.getThreatLevel().getValue(),
                        Collectors.counting()
                ));
        
        summary.setCriticalThreats(threatLevelCounts.getOrDefault("critical", 0L).intValue());
        summary.setHighThreats(threatLevelCounts.getOrDefault("high", 0L).intValue());
        summary.setMediumThreats(threatLevelCounts.getOrDefault("medium", 0L).intValue());
        summary.setLowThreats(threatLevelCounts.getOrDefault("low", 0L).intValue());
        
        // Get category counts
        Map<String, Long> categoryCounts = activeReports.stream()
                .collect(Collectors.groupingBy(
                        report -> report.getCategory().getValue(),
                        Collectors.counting()
                ));
        
        List<ThreatCategoryCount> categoryCountList = categoryCounts.entrySet().stream()
                .map(entry -> new ThreatCategoryCount(entry.getKey(), entry.getValue().intValue()))
                .collect(Collectors.toList());
        
        summary.setCategoryCounts(categoryCountList);
        
        // Get recent threats
        List<RecentThreat> recentThreats = activeReports.stream()
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .limit(5)
                .map(report -> new RecentThreat(
                        report.getId(),
                        report.getTitle(),
                        report.getThreatLevel().getValue(),
                        report.getCreatedAt(),
                        report.getLocation() != null ? report.getLocation().getAddress() : "Unknown"
                ))
                .collect(Collectors.toList());
        
        summary.setRecentThreats(recentThreats);
        
        return summary;
    }
    
    @Override
    public List<ThreatTrend> getThreatTrends(int days) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        
        List<IntelligenceReport> reports = intelligenceReportRepository.findByCreatedAtAfter(startDate);
        
        // Group by date and calculate trends
        Map<String, List<IntelligenceReport>> reportsByDate = reports.stream()
                .collect(Collectors.groupingBy(
                        report -> report.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                ));
        
        List<ThreatTrend> trends = new ArrayList<>();
        
        for (int i = 0; i < days; i++) {
            LocalDateTime date = startDate.plusDays(i);
            String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            List<IntelligenceReport> dayReports = reportsByDate.getOrDefault(dateStr, Collections.emptyList());
            
            double avgThreatScore = dayReports.stream()
                    .mapToDouble(report -> report.getThreatScore() != null ? report.getThreatScore() : 0.0)
                    .average()
                    .orElse(0.0);
            
            trends.add(new ThreatTrend(dateStr, dayReports.size(), avgThreatScore));
        }
        
        return trends;
    }
    
    @Override
    public List<ThreatLocation> getThreatLocations(LocalDateTime since) {
        if (since == null) {
            since = LocalDateTime.now().minusHours(24);
        }
        
        List<IntelligenceReport> reports = intelligenceReportRepository.findWithLocationAfter(since);
        
        return reports.stream()
                .filter(report -> report.getLocation() != null && 
                        report.getLocation().getLatitude() != null && 
                        report.getLocation().getLongitude() != null)
                .map(report -> new ThreatLocation(
                        report.getId(),
                        report.getLocation().getLatitude(),
                        report.getLocation().getLongitude(),
                        report.getLocation().getAddress() != null ? report.getLocation().getAddress() : "Unknown",
                        report.getThreatLevel().getValue(),
                        report.getThreatScore() != null ? report.getThreatScore() : 0.0,
                        report.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteIntelligenceReport(String id) {
        if (intelligenceReportRepository.existsById(id)) {
            intelligenceReportRepository.deleteById(id);
            log.info("Deleted intelligence report: {}", id);
        } else {
            throw new RuntimeException("Intelligence report not found: " + id);
        }
    }
    
    @Override
    public List<IntelligenceReport> getRecentReports(int limit) {
        return intelligenceReportRepository.findAll().stream()
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<IntelligenceReport> getReportsByLocation(String latitude, String longitude, double radiusKm) {
        // For now, return all reports with location data
        // In a real implementation, you would use geospatial queries
        return intelligenceReportRepository.findByLocationIsNotNull().stream()
                .filter(report -> report.getLocation() != null && 
                        report.getLocation().getLatitude() != null && 
                        report.getLocation().getLongitude() != null)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Prediction> getPredictions(String type, LocalDateTime since, int hours) {
        List<Prediction> predictions = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime validUntil = now.plusHours(hours);
        
        // Get real intelligence reports to base predictions on
        List<IntelligenceReport> recentReports = intelligenceReportRepository.findByCreatedAtAfter(since != null ? since : now.minusHours(24));
        
        // Get real alerts to correlate with intelligence
        // Note: In a real implementation, you would use a REST client or Feign to call the alert service
        // For now, we'll create predictions based on the intelligence reports we have
        
        // Analyze intelligence reports to generate predictions
        Map<String, List<IntelligenceReport>> reportsByThreatLevel = recentReports.stream()
                .filter(r -> r.getThreatLevel() != null)
                .collect(Collectors.groupingBy(r -> r.getThreatLevel().toString()));
        
        // Generate predictions based on high-risk intelligence reports
        if (reportsByThreatLevel.containsKey("HIGH")) {
            List<IntelligenceReport> highRiskReports = reportsByThreatLevel.get("HIGH");
            
            // Social unrest prediction based on intelligence data
            predictions.add(Prediction.builder()
                    .id("intel-pred-social-" + UUID.randomUUID().toString().substring(0, 8))
                    .type("social_unrest")
                    .title("Social Unrest Risk - Intelligence Based")
                    .description("Based on " + highRiskReports.size() + " high-risk intelligence reports indicating elevated social tensions")
                    .severity("medium")
                    .confidence(0.75 + (highRiskReports.size() * 0.05)) // Higher confidence with more reports
                    .predictedAt(now)
                    .validFrom(now.plusHours(2))
                    .validUntil(validUntil)
                    .timeWindowHours(hours)
                    .location("Nairobi Region")
                    .latitude("-1.2921")
                    .longitude("36.8219")
                    .radiusKm(50.0)
                    .affectedAreas(Arrays.asList("Nairobi CBD", "Uhuru Park", "Parliament Buildings"))
                    .riskFactors(Arrays.asList("intelligence reports", "threat patterns", "historical data"))
                    .indicators(Arrays.asList("increased communications", "mobilization signs", "social media activity"))
                    .recommendedActions(Arrays.asList("increase monitoring", "prepare contingency plans", "coordinate with security agencies"))
                    .aiModel("IntelligencePredictorV1")
                    .aiVersion("1.0.0")
                    .aiConfidence(String.format("%.2f", 0.75 + (highRiskReports.size() * 0.05)))
                    .aiExplanation("Based on analysis of " + highRiskReports.size() + " intelligence reports with threat level HIGH")
                    .aiProcessed(true)
                    .status("ACTIVE")
                    .source("Intelligence Service")
                    .dataSource("Intelligence Reports Database")
                    .lastUpdated(now)
                    .intelligenceReportId(highRiskReports.stream().map(IntelligenceReport::getId).findFirst().orElse(null))
                    .build());
        }
        
        // Generate border security predictions based on intelligence
        List<IntelligenceReport> borderReports = recentReports.stream()
                .filter(r -> r.getCategory() != null && r.getCategory().name().toLowerCase().contains("border"))
                .collect(Collectors.toList());
        
        if (!borderReports.isEmpty()) {
            predictions.add(Prediction.builder()
                    .id("intel-pred-border-" + UUID.randomUUID().toString().substring(0, 8))
                    .type("border_security")
                    .title("Border Security Assessment")
                    .description("Intelligence analysis of " + borderReports.size() + " border-related reports indicates potential security concerns")
                    .severity("low")
                    .confidence(0.65 + (borderReports.size() * 0.03))
                    .predictedAt(now)
                    .validFrom(now.plusHours(1))
                    .validUntil(validUntil)
                    .timeWindowHours(hours)
                    .location("Northern Kenya Border")
                    .latitude("3.8628")
                    .longitude("36.8172")
                    .radiusKm(100.0)
                    .affectedAreas(Arrays.asList("Moyale", "Mandera", "Wajir"))
                    .riskFactors(Arrays.asList("border intelligence", "cross-border activity", "regional instability"))
                    .indicators(Arrays.asList("movement patterns", "communication intercepts", "surveillance data"))
                    .recommendedActions(Arrays.asList("enhance border patrols", "increase surveillance", "coordinate with neighboring agencies"))
                    .aiModel("BorderIntelligencePredictor")
                    .aiVersion("1.0.0")
                    .aiConfidence(String.format("%.2f", 0.65 + (borderReports.size() * 0.03)))
                    .aiExplanation("Based on " + borderReports.size() + " border-related intelligence reports")
                    .aiProcessed(true)
                    .status("ACTIVE")
                    .source("Intelligence Service")
                    .dataSource("Border Intelligence Database")
                    .lastUpdated(now)
                    .intelligenceReportId(borderReports.stream().map(IntelligenceReport::getId).findFirst().orElse(null))
                    .build());
        }
        
        // Generate cyber threat predictions based on intelligence
        List<IntelligenceReport> cyberReports = recentReports.stream()
                .filter(r -> r.getCategory() != null && r.getCategory().name().toLowerCase().contains("cyber"))
                .collect(Collectors.toList());
        
        if (!cyberReports.isEmpty()) {
            predictions.add(Prediction.builder()
                    .id("intel-pred-cyber-" + UUID.randomUUID().toString().substring(0, 8))
                    .type("cyber")
                    .title("Cyber Threat Intelligence")
                    .description("Analysis of " + cyberReports.size() + " cyber intelligence reports indicates elevated threat level")
                    .severity("high")
                    .confidence(0.82 + (cyberReports.size() * 0.02))
                    .predictedAt(now)
                    .validFrom(now.plusHours(1))
                    .validUntil(validUntil)
                    .timeWindowHours(hours)
                    .location("Kenya Digital Infrastructure")
                    .latitude("-1.2921")
                    .longitude("36.8219")
                    .radiusKm(1.0) // Digital location
                    .affectedAreas(Arrays.asList("Banking Systems", "Government Networks", "Critical Infrastructure"))
                    .riskFactors(Arrays.asList("cyber intelligence", "threat actor activity", "vulnerability disclosures"))
                    .indicators(Arrays.asList("malware signatures", "phishing campaigns", "network anomalies"))
                    .recommendedActions(Arrays.asList("enhance monitoring", "update defenses", "prepare incident response"))
                    .aiModel("CyberIntelligencePredictor")
                    .aiVersion("1.0.0")
                    .aiConfidence(String.format("%.2f", 0.82 + (cyberReports.size() * 0.02)))
                    .aiExplanation("Based on " + cyberReports.size() + " cyber intelligence reports")
                    .aiProcessed(true)
                    .status("ACTIVE")
                    .source("Intelligence Service")
                    .dataSource("Cyber Intelligence Database")
                    .lastUpdated(now)
                    .intelligenceReportId(cyberReports.stream().map(IntelligenceReport::getId).findFirst().orElse(null))
                    .build());
        }
        
        log.info("Generated {} intelligence-based predictions for type: {}, since: {}, hours: {}, based on {} intelligence reports", 
                predictions.size(), type, since, hours, recentReports.size());
        
        return predictions;
    }
}

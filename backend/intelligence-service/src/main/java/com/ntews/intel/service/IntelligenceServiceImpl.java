package com.ntews.intel.service;

import com.ntews.intel.controller.IntelligenceController.*;
import com.ntews.intel.model.IntelligenceReport;
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
        report.setId(UUID.randomUUID().toString());
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        
        if (report.getStatus() == null) {
            report.setStatus(IntelligenceReport.ReportStatus.DRAFT);
        }
        
        // Use AI Engine to analyze the report
        try {
            String reportData = String.format("Title: %s, Content: %s, Category: %s", 
                report.getTitle(), report.getContent(), report.getCategory());
            
            Map<String, Object> aiAnalysis = aiEngineClient.analyzeThreat(reportData);
            
            // Update report with AI analysis
            if (aiAnalysis != null) {
                report.setAiConfidence(((Number) aiAnalysis.getOrDefault("confidence", 0.5)).doubleValue());
                report.setAiThreatLevel((String) aiAnalysis.getOrDefault("threat_level", "medium"));
                report.setAiRiskScore(((Number) aiAnalysis.getOrDefault("risk_score", 50)).intValue());
                
                @SuppressWarnings("unchecked")
                List<String> recommendations = (List<String>) aiAnalysis.getOrDefault("recommendations", 
                    Arrays.asList("Monitor situation", "Gather more intelligence"));
                report.setAiRecommendations(String.join("; ", recommendations));
                
                log.info("AI analysis completed for report: {}", report.getId());
            }
        } catch (Exception e) {
            log.warn("AI analysis failed for report {}, using defaults: {}", report.getId(), e.getMessage());
            // Set default values if AI analysis fails
            report.setAiConfidence(0.5);
            report.setAiThreatLevel("medium");
            report.setAiRiskScore(50);
            report.setAiRecommendations("Monitor situation; Gather more intelligence");
        }
        
        IntelligenceReport savedReport = intelligenceReportRepository.save(report);
        log.info("Created intelligence report: {}", savedReport.getId());
        
        return savedReport;
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
}

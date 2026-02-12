package com.ntews.intel.service;

import com.ntews.intel.controller.IntelligenceController.*;
import com.ntews.intel.model.IntelligenceReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IntelligenceService {
    
    Page<IntelligenceReport> getIntelligenceReports(
            String threatLevel, String category, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    Optional<IntelligenceReport> getIntelligenceReport(String id);
    
    IntelligenceReport createIntelligenceReport(IntelligenceReport report);
    
    IntelligenceReport updateIntelligenceReport(IntelligenceReport report);
    
    IntelligenceReport verifyReport(String id, boolean verified, String notes);
    
    DashboardSummary getDashboardSummary();
    
    List<ThreatTrend> getThreatTrends(int days);
    
    List<ThreatLocation> getThreatLocations(LocalDateTime since);
    
    void deleteIntelligenceReport(String id);
    
    List<IntelligenceReport> getRecentReports(int limit);
    
    List<IntelligenceReport> getReportsByLocation(String latitude, String longitude, double radiusKm);
}

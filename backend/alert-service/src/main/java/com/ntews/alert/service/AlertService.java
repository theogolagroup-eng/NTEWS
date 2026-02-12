package com.ntews.alert.service;

import com.ntews.alert.controller.AlertController.*;
import com.ntews.alert.model.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AlertService {
    
    Page<Alert> getAlerts(
            String severity, String status, String category, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    Optional<Alert> getAlert(String id);
    
    Alert createAlert(Alert alert);
    
    Alert updateAlert(Alert alert);
    
    Alert acknowledgeAlert(String id);
    
    Alert resolveAlert(String id, String resolutionNotes);
    
    Alert assignAlert(String id, String assignedTo);
    
    AlertDashboardSummary getDashboardSummary();
    
    List<Alert> getActiveAlerts();
    
    List<Alert> getUnacknowledgedAlerts();
    
    AlertStatistics getAlertStatistics(int days);
    
    void deleteAlert(String id);
    
    List<Alert> getAlertsByLocation(String latitude, String longitude, double radiusKm);
    
    List<Alert> getAlertsByAssignee(String assignedTo);
    
    void escalateAlert(String id, String reason);
    
    // AI-powered alert methods
    Alert createAIEnhancedAlert(Map<String, Object> alertData);
    Alert generateProactiveAlert(Map<String, Object> threatData, Map<String, Object> aiAnalysis, Map<String, Object> prediction);
    void updateAlertWithAIAnalysis(String alertId, Map<String, Object> aiAnalysis);
    List<Alert> getAIEnhancedAlerts();
    AlertDashboardSummary getAIDashboardSummary();
}

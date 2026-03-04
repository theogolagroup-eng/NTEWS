package com.ntews.alert.service;



import com.ntews.alert.controller.AlertController.*;

import com.ntews.alert.model.Alert;

import com.ntews.alert.repository.AlertRepository;

import com.ntews.alert.controller.WebSocketController;

import org.springframework.context.annotation.Lazy;

import org.springframework.context.ApplicationContext;

import com.ntews.alert.client.AIEngineClient;

import com.ntews.alert.client.AIEngineClient.AlertPriorityAnalysis;

import com.ntews.alert.client.AIEngineClient.ThreatEvolutionPrediction;

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

public class AlertServiceImpl implements AlertService {

    

    private final AlertRepository alertRepository;

    private final AIEngineClient aiEngineClient;

    private final NLPAnalysisService nlpAnalysisService;

    private final ApplicationContext applicationContext;

    

    @Override

    public Page<Alert> getAlerts(

            String severity, String status, String category, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {

        

        if (severity != null && status != null && startDate != null && endDate != null) {

            return alertRepository.findBySeverityAndStatusAndCreatedAtBetween(severity, status, startDate, endDate, pageable);

        } else if (severity != null) {

            return alertRepository.findBySeverity(severity, pageable);

        } else if (status != null) {

            return alertRepository.findByStatus(status, pageable);

        } else if (category != null) {

            return alertRepository.findByCategory(category, pageable);

        } else if (startDate != null && endDate != null) {

            return alertRepository.findByCreatedAtBetween(startDate, endDate, pageable);

        } else {

            return alertRepository.findAll(pageable);

        }

    }

    

    @Override

    public Optional<Alert> getAlert(String id) {

        return alertRepository.findById(id);

    }

    

    @Override

    public Alert getAlertById(String alertId) {

        return alertRepository.findById(alertId).orElse(null);

    }

    

    @Override

    public Alert createAlert(Alert alert) {

        alert.setId(UUID.randomUUID().toString());

        alert.setCreatedAt(LocalDateTime.now());

        alert.setUpdatedAt(LocalDateTime.now());

        

        if (alert.getStatus() == null) {

            alert.setStatus(Alert.AlertStatus.ACTIVE);

        }

        

        // Alert Analysis

        try {

            log.info("Performing AI analysis for alert: {}", alert.getTitle());

            

            // Analyze alert content with AI Engine

            Map<String, Object> alertData = prepareAlertData(alert);

            AlertPriorityAnalysis aiAnalysis = aiEngineClient.predictAlertPriority(alertData);

            

            // Enrich alert with AI insights

            enrichAlertWithAI(alert, aiAnalysis);

            

            // Predict threat evolution

            ThreatEvolutionPrediction evolution = aiEngineClient.predictThreatEvolution(alert.getId(), alertData);

            enrichAlertWithEvolution(alert, evolution);

            

            // AI-based prioritization and routing

            prioritizeAndRouteAlert(alert, aiAnalysis, evolution);

            

            log.info("AI analysis completed for alert {}: severity={}, urgency={}, time_to_critical={}min", 

                alert.getId(), alert.getSeverity(), alert.getAiUrgencyScore(), alert.getTimeToCritical());

            

        } catch (Exception e) {

            log.warn("AI analysis failed for alert {}, using defaults: {}", alert.getId(), e.getMessage());

            // Set default AI values if analysis fails

            setDefaultAIValues(alert);

        }

        

        // Perform NLP analysis

        try {

            alert = nlpAnalysisService.analyzeAlertWithNLP(alert);

            log.info("NLP analysis completed for alert {}: classification={}, confidence={}, risk_score={}", 

                alert.getId(), alert.getNlpClassification(), alert.getNlpConfidence(), alert.getNlpRiskScore());

            

        } catch (Exception e) {

            log.warn("NLP analysis failed for alert {}, using defaults: {}", alert.getId(), e.getMessage());

            // NLP service sets default values automatically

        }

        

        Alert savedAlert = alertRepository.save(alert);

        log.info("Created alert: {} - {} (Priority: {}, AI Risk: {}, NLP Classification: {})", 

            savedAlert.getId(), savedAlert.getTitle(), savedAlert.getPriority(), 

            savedAlert.getAiRiskLevel(), savedAlert.getNlpClassification());

        

        // Broadcast new alert to WebSocket clients

        try {

            WebSocketController webSocketController = applicationContext.getBean(WebSocketController.class);

            if (webSocketController != null) {

                webSocketController.sendRealTimeAlert(savedAlert);

            }

        } catch (Exception e) {

            log.warn("Failed to broadcast alert to WebSocket clients: {}", e.getMessage());

        }

        

        return savedAlert;

    }

    

    @Override

    public Alert updateAlert(Alert alert) {

        alert.setUpdatedAt(LocalDateTime.now());

        

        Alert updatedAlert = alertRepository.save(alert);

        log.info("Updated alert: {}", updatedAlert.getId());

        

        return updatedAlert;

    }

    

    @Override

    public Alert acknowledgeAlert(String id) {

        Optional<Alert> alertOpt = alertRepository.findById(id);

        

        if (alertOpt.isPresent()) {

            Alert alert = alertOpt.get();

            alert.setStatus(Alert.AlertStatus.ACKNOWLEDGED);

            alert.setUpdatedAt(LocalDateTime.now());

            

            return alertRepository.save(alert);

        } else {

            throw new RuntimeException("Alert not found: " + id);

        }

    }

    

    @Override

    public Alert resolveAlert(String id, String resolutionNotes) {

        Optional<Alert> alertOpt = alertRepository.findById(id);

        

        if (alertOpt.isPresent()) {

            Alert alert = alertOpt.get();

            alert.setStatus(Alert.AlertStatus.RESOLVED);

            alert.setResolutionNotes(resolutionNotes);

            alert.setResolved(true);

            alert.setResolvedAt(LocalDateTime.now());

            alert.setUpdatedAt(LocalDateTime.now());

            

            return alertRepository.save(alert);

        } else {

            throw new RuntimeException("Alert not found: " + id);

        }

    }

    

    @Override

    public Alert assignAlert(String id, String assignedTo) {

        Optional<Alert> alertOpt = alertRepository.findById(id);

        

        if (alertOpt.isPresent()) {

            Alert alert = alertOpt.get();

            alert.setAssignedTo(assignedTo);

            alert.setUpdatedAt(LocalDateTime.now());

            

            return alertRepository.save(alert);

        } else {

            throw new RuntimeException("Alert not found: " + id);

        }

    }

    

    @Override

    public AlertDashboardSummary getDashboardSummary() {

        AlertDashboardSummary summary = new AlertDashboardSummary();

        

        // Get total alerts

        long totalAlerts = alertRepository.count();

        summary.setTotalAlerts((int) totalAlerts);

        

        try {

            // Get all alerts and filter in memory (like working frontend approach)

            List<Alert> allAlerts = alertRepository.findAll();

            if (allAlerts == null) {

                allAlerts = new ArrayList<>();

            }

            

            // Filter active alerts in memory

            List<Alert> activeAlerts = allAlerts.stream()

                    .filter(alert -> alert != null && alert.getStatus() != null && 

                                   alert.getStatus().getValue().equals("active"))

                    .collect(Collectors.toList());

            

            summary.setActiveAlerts(activeAlerts.size());

            

            // Get unacknowledged alerts with proper filtering

            long unacknowledgedCount = activeAlerts.stream()

                    .filter(alert -> alert != null && alert.getStatus() == Alert.AlertStatus.ACTIVE)

                    .count();

            summary.setUnacknowledgedAlerts((int) unacknowledgedCount);

            

            // Get severity counts with null safety

            Map<String, Long> severityCounts = activeAlerts.stream()

                    .filter(alert -> alert != null && alert.getSeverity() != null)

                    .collect(Collectors.groupingBy(

                            alert -> alert.getSeverity().getValue(),

                            Collectors.counting()

                    ));

            

            summary.setCriticalAlerts(severityCounts.getOrDefault("critical", 0L).intValue());

            summary.setHighAlerts(severityCounts.getOrDefault("high", 0L).intValue());

            summary.setMediumAlerts(severityCounts.getOrDefault("medium", 0L).intValue());

            summary.setLowAlerts(severityCounts.getOrDefault("low", 0L).intValue());

            

            // Get severity counts list

            List<AlertSeverityCount> severityCountList = severityCounts.entrySet().stream()

                    .map(entry -> new AlertSeverityCount(entry.getKey(), entry.getValue().intValue()))

                    .collect(Collectors.toList());

            

            summary.setSeverityCounts(severityCountList);

        

        // Get recent alerts with null safety - use active alerts from memory

            List<RecentAlert> recentAlerts = activeAlerts.stream()

                    .filter(alert -> alert != null && alert.getCreatedAt() != null)

                    .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))

                    .limit(5)

                    .map(alert -> new RecentAlert(

                            alert.getId(),

                            alert.getTitle() != null ? alert.getTitle() : "Untitled Alert",

                            alert.getSeverity() != null ? alert.getSeverity().getValue() : "unknown",

                            alert.getStatus() != null ? alert.getStatus().getValue() : "unknown",

                            alert.getCreatedAt(),

                            alert.getLocation() != null ? alert.getLocation().getAddress() : "Unknown"

                    ))

                    .collect(Collectors.toList());

            

            summary.setRecentAlerts(recentAlerts);

            

        } catch (Exception e) {

            log.error("Error generating alert dashboard summary: {}", e.getMessage());

            // Set default values on error

            summary.setActiveAlerts(0);

            summary.setUnacknowledgedAlerts(0);

            summary.setCriticalAlerts(0);

            summary.setHighAlerts(0);

            summary.setMediumAlerts(0);

            summary.setLowAlerts(0);

            summary.setSeverityCounts(new ArrayList<>());

            summary.setRecentAlerts(new ArrayList<>());

        }

        

        return summary;

    }

    

    @Override

    public List<Alert> getActiveAlerts() {

        return alertRepository.findByStatus(Alert.AlertStatus.ACTIVE.getValue());

    }

    

    @Override

    public List<Alert> getUnacknowledgedAlerts() {

        return alertRepository.findByStatus(Alert.AlertStatus.ACTIVE.getValue()).stream()

                .filter(alert -> alert.getStatus() == Alert.AlertStatus.ACTIVE)

                .collect(Collectors.toList());

    }

    

    @Override

    public AlertStatistics getAlertStatistics(int days) {

        AlertStatistics statistics = new AlertStatistics();

        

        LocalDateTime endDate = LocalDateTime.now();

        LocalDateTime startDate = endDate.minusDays(days);

        

        // Get trends

        List<Alert> alerts = alertRepository.findByCreatedAtBetween(startDate, endDate, null).getContent();

        

        Map<String, List<Alert>> alertsByDate = alerts.stream()

                .collect(Collectors.groupingBy(

                        alert -> alert.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

                ));

        

        List<AlertTrend> trends = new ArrayList<>();

        for (int i = 0; i < days; i++) {

            LocalDateTime date = startDate.plusDays(i);

            String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            

            List<Alert> dayAlerts = alertsByDate.getOrDefault(dateStr, Collections.emptyList());

            trends.add(new AlertTrend(dateStr, dayAlerts.size()));

        }

        

        statistics.setTrends(trends);

        

        // Get hourly counts

        Map<Integer, Long> hourlyCounts = alerts.stream()

                .collect(Collectors.groupingBy(

                        alert -> alert.getCreatedAt().getHour(),

                        Collectors.counting()

                ));

        

        List<AlertHourlyCount> hourlyCountList = new ArrayList<>();

        for (int hour = 0; hour < 24; hour++) {

            hourlyCountList.add(new AlertHourlyCount(hour, hourlyCounts.getOrDefault(hour, 0L).intValue()));

        }

        

        statistics.setHourlyCounts(hourlyCountList);

        

        // Get category counts

        Map<String, Long> categoryCounts = alerts.stream()

                .collect(Collectors.groupingBy(

                        alert -> alert.getCategory() != null ? alert.getCategory() : "unknown",

                        Collectors.counting()

                ));

        

        List<AlertCategoryCount> categoryCountList = categoryCounts.entrySet().stream()

                .map(entry -> new AlertCategoryCount(entry.getKey(), entry.getValue().intValue()))

                .collect(Collectors.toList());

        

        statistics.setCategoryCounts(categoryCountList);

        

        // Calculate resolution rate

        long totalAlertsInPeriod = alerts.size();

        long resolvedAlerts = alertRepository.countResolvedAlertsBetween(startDate, endDate);

        

        double resolutionRate = totalAlertsInPeriod > 0 ? 

                (double) resolvedAlerts / totalAlertsInPeriod * 100 : 0.0;

        

        statistics.setResolutionRate(resolutionRate);

        

        // Set average response time (placeholder - would need actual response time data)

        statistics.setAverageResponseTime(15.5); // minutes

        

        return statistics;

    }

    

    @Override

    public void deleteAlert(String id) {

        if (alertRepository.existsById(id)) {

            alertRepository.deleteById(id);

            log.info("Deleted alert: {}", id);

        } else {

            throw new RuntimeException("Alert not found: " + id);

        }

    }

    

    @Override

    public List<Alert> getAlertsByLocation(String latitude, String longitude, double radiusKm) {

        // For now, return all alerts with location data

        // In a real implementation, you would use geospatial queries

        return alertRepository.findWithLocationAfter(LocalDateTime.now().minusHours(24));

    }

    

    @Override

    public List<Alert> getAlertsByAssignee(String assignedTo) {

        return alertRepository.findActiveAlertsByAssignee(assignedTo);

    }

    

    @Override

    public void escalateAlert(String id, String reason) {

        Optional<Alert> alertOpt = alertRepository.findById(id);

        

        if (alertOpt.isPresent()) {

            Alert alert = alertOpt.get();

            

            // Create escalation info

            Alert.EscalationInfo escalationInfo = Alert.EscalationInfo.builder()

                    .escalationLevel(1)

                    .escalatedAt(LocalDateTime.now())

                    .escalationReason(reason)

                    .nextEscalation(LocalDateTime.now().plusHours(1))

                    .escalationHistory(new ArrayList<>())

                    .build();

            

            alert.setEscalation(escalationInfo);

            alert.setUpdatedAt(LocalDateTime.now());

            

            alertRepository.save(alert);

            log.info("Escalated alert: {} - Reason: {}", id, reason);

        } else {

            throw new RuntimeException("Alert not found: " + id);

        }

    }

    

    // Alert enhancement methods implementation

    

    @Override

    public Alert createAIEnhancedAlert(Map<String, Object> alertData) {

        try {

            log.info("Creating AI-enhanced alert from data: {}", alertData.get("id"));

            

            // Get AI priority analysis

            AlertPriorityAnalysis priorityAnalysis = aiEngineClient.predictAlertPriority(alertData);

            

            // Create alert with AI insights

            Alert alert = Alert.builder()

                .id((String) alertData.get("id"))

                .title((String) alertData.get("title"))

                .description((String) alertData.get("description"))

                .severity(mapStringToSeverity((String) alertData.getOrDefault("severity", priorityAnalysis.getRecommendedPriority())))

                .status(Alert.AlertStatus.ACTIVE)

                .priority(mapStringToPriority(priorityAnalysis.getRecommendedPriority()))

                .category((String) alertData.getOrDefault("category", "unknown"))

                .location(Alert.LocationInfo.builder()

                    .latitude((String) alertData.get("latitude"))

                    .longitude((String) alertData.get("longitude"))

                    .address((String) alertData.get("address"))

                    .build())

                .createdAt(LocalDateTime.now())

                .updatedAt(LocalDateTime.now())

                .build();

            

            // Add AI analysis data

            alert.setAiConfidence(priorityAnalysis.getConfidence());

            alert.setAiUrgencyScore(priorityAnalysis.getUrgencyScore());

            alert.setAiImpactScore(priorityAnalysis.getImpactScore());

            alert.setAiRiskFactors(String.join(", ", priorityAnalysis.getRiskFactors()));

            alert.setAiRecommendations(String.join("; ", priorityAnalysis.getRecommendedActions()));

            alert.setAiAnalysis(priorityAnalysis.getAnalysis());

            alert.setTimeToEscalate(priorityAnalysis.getTimeToEscalate());

            

            Alert savedAlert = alertRepository.save(alert);

            log.info("Created AI-enhanced alert: {} with priority: {}", savedAlert.getId(), savedAlert.getPriority());

            

            return savedAlert;

            

        } catch (Exception e) {

            log.error("Error creating AI-enhanced alert", e);

            throw new RuntimeException("Failed to create AI-enhanced alert", e);

        }

    }

    

    @Override

    public Alert generateProactiveAlert(Map<String, Object> threatData, Map<String, Object> aiAnalysis, Map<String, Object> prediction) {

        try {

            log.info("Generating proactive alert for threat: {}", threatData.get("threatId"));

            

            // Create proactive alert

            Alert alert = Alert.builder()

                .id(UUID.randomUUID().toString())

                .title("PROACTIVE: " + threatData.getOrDefault("sourceType", "Unknown").toString().toUpperCase() + " Threat Detected")

                .description(String.format("AI analysis detected high-risk threat with score %.2f. Predicted to become critical in %d minutes.", 

                    (Double) aiAnalysis.getOrDefault("threatScore", 0.5), (Integer) prediction.getOrDefault("timeToCritical", 60)))

                .severity(mapStringToSeverity((String) prediction.getOrDefault("predictedSeverity", "medium")))

                .status(Alert.AlertStatus.ACTIVE)

                .priority((Integer) prediction.getOrDefault("timeToCritical", 60) < 60 ? Alert.Priority.HIGH : Alert.Priority.NORMAL)

                .category((String) threatData.getOrDefault("sourceType", "unknown"))

                .location(Alert.LocationInfo.builder()

                    .latitude((String) threatData.get("latitude"))

                    .longitude((String) threatData.get("longitude"))

                    .address((String) threatData.get("address"))

                    .build())

                .createdAt(LocalDateTime.now())

                .updatedAt(LocalDateTime.now())

                .source("AI Engine - Ingestion Service")

                .build();

            

            // Add AI analysis data

            alert.setAiConfidence((Double) aiAnalysis.getOrDefault("confidence", 0.5));

            alert.setAiThreatScore((Double) aiAnalysis.getOrDefault("threatScore", 0.5));

            alert.setAiRiskLevel((String) aiAnalysis.getOrDefault("riskLevel", "medium"));

            alert.setAiAnalysis((String) aiAnalysis.getOrDefault("analysis", "AI analysis completed"));

            alert.setAiKeywords((String) aiAnalysis.getOrDefault("threatKeywords", ""));

            

            // Add prediction data

            alert.setPredictedSeverity((String) prediction.getOrDefault("predictedSeverity", "medium"));

            alert.setTimeToCritical((Integer) prediction.getOrDefault("timeToCritical", 60));

            alert.setEvolutionTrend((String) prediction.getOrDefault("evolutionTrend", "stable"));

            

            // Add preventive actions

            @SuppressWarnings("unchecked")

            List<String> recommendedActions = (List<String>) prediction.getOrDefault("recommendedActions", new ArrayList<>());

            alert.setAiRecommendations(String.join("; ", recommendedActions));

            

            Alert savedAlert = alertRepository.save(alert);

            log.info("Generated proactive alert: {} with priority: {}", savedAlert.getId(), savedAlert.getPriority());

            

            return savedAlert;

            

        } catch (Exception e) {

            log.error("Error generating proactive alert", e);

            throw new RuntimeException("Failed to generate proactive alert", e);

        }

    }

    

    @Override

    public void updateAlertWithAIAnalysis(String alertId, Map<String, Object> aiAnalysis) {

        try {

            Optional<Alert> alertOpt = alertRepository.findById(alertId);

            

            if (alertOpt.isPresent()) {

                Alert alert = alertOpt.get();

                

                // Update alert with AI analysis

                alert.setAiConfidence(((Number) aiAnalysis.getOrDefault("confidence", 0.5)).doubleValue());

                alert.setAiThreatScore(((Number) aiAnalysis.getOrDefault("threatScore", 0.5)).doubleValue());

                alert.setAiRiskLevel((String) aiAnalysis.getOrDefault("riskLevel", "medium"));

                alert.setAiAnalysis((String) aiAnalysis.getOrDefault("analysis", "AI analysis completed"));

                alert.setAiKeywords((String) aiAnalysis.getOrDefault("threatKeywords", ""));

                alert.setUpdatedAt(LocalDateTime.now());

                

                alertRepository.save(alert);

                log.info("Updated alert {} with AI analysis", alertId);

            } else {

                throw new RuntimeException("Alert not found: " + alertId);

            }

        } catch (Exception e) {

            log.error("Error updating alert with AI analysis: {}", alertId, e);

            throw new RuntimeException("Failed to update alert with AI analysis", e);

        }

    }

    

    @Override

    public List<Alert> getAIEnhancedAlerts() {

        try {

            // Get alerts that have AI analysis data

            List<Alert> allAlerts = alertRepository.findAll();

            return allAlerts.stream()

                .filter(alert -> alert.getAiConfidence() > 0 || alert.getAiAnalysis() != null)

                .collect(Collectors.toList());

        } catch (Exception e) {

            log.error("Error fetching AI-enhanced alerts", e);

            return new ArrayList<>();

        }

    }

    

    @Override

    public List<Alert> getRecentAlerts(int limit) {

        try {

            return alertRepository.findAll(

                org.springframework.data.domain.PageRequest.of(0, limit, 

                    org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")

                )

            ).getContent();

        } catch (Exception e) {

            log.error("Error fetching recent alerts with limit {}: {}", limit, e.getMessage());

            return new ArrayList<>();

        }

    }

    

    @Override

    public AlertDashboardSummary getAIDashboardSummary() {

        try {

            AlertDashboardSummary summary = new AlertDashboardSummary();

            

            // Get AI-enhanced alerts

            List<Alert> aiAlerts = getAIEnhancedAlerts();

            

            // Calculate AI metrics

            long totalAIAlerts = aiAlerts.size();

            long highConfidenceAlerts = aiAlerts.stream()

                .filter(alert -> alert.getAiConfidence() >= 0.8)

                .count();

            long criticalPredictions = aiAlerts.stream()

                .filter(alert -> "critical".equals(alert.getPredictedSeverity()))

                .count();

            

            // Average AI confidence

            double avgConfidence = aiAlerts.stream()

                .mapToDouble(alert -> alert.getAiConfidence())

                .average()

                .orElse(0.0);

            

            // AI risk level distribution

            Map<String, Long> aiRiskLevels = aiAlerts.stream()

                .filter(alert -> alert.getAiRiskLevel() != null)

                .collect(Collectors.groupingBy(

                    Alert::getAiRiskLevel,

                    Collectors.counting()

                ));

            

            summary.setTotalAlerts((int) totalAIAlerts);

            summary.setActiveAlerts((int) totalAIAlerts); // All AI alerts are considered active

            summary.setCriticalAlerts((int) criticalPredictions);

            summary.setHighConfidenceAlerts((int) highConfidenceAlerts);

            summary.setAiRiskLevels(aiRiskLevels);

            summary.setAverageAiConfidence(avgConfidence);

            summary.setAiEngineHealthy(aiEngineClient.isHealthy());

            

            return summary;

            

        } catch (Exception e) {

            log.error("Error generating AI dashboard summary", e);

            return new AlertDashboardSummary(); // Return empty summary on error

        }

    }

    

    // ========== AI INTEGRATION HELPER METHODS ==========

    

    /**

     * Prepare alert data for AI analysis

     */

    private Map<String, Object> prepareAlertData(Alert alert) {

        Map<String, Object> alertData = new HashMap<>();

        alertData.put("title", alert.getTitle());

        alertData.put("description", alert.getDescription());

        alertData.put("type", alert.getType());

        alertData.put("severity", alert.getSeverity());

        alertData.put("category", alert.getCategory());

        alertData.put("source_type", alert.getSourceType());

        alertData.put("location", alert.getLocation());

        alertData.put("content", alert.getContent());

        alertData.put("tags", alert.getTags());

        alertData.put("keywords", alert.getKeywords());

        alertData.put("timestamp", alert.getTimestamp());

        return alertData;

    }

    

    /**

     * Enrich alert with AI analysis results

     */

    private void enrichAlertWithAI(Alert alert, AlertPriorityAnalysis analysis) {

        alert.setAiConfidence(analysis.getConfidence());

        alert.setAiThreatScore(analysis.getConfidence()); // Using confidence as threat score

        alert.setAiRiskLevel(analysis.getRecommendedPriority());

        alert.setAiAnalysis(analysis.getAnalysis());

        alert.setAiKeywords("priority,alert,analysis"); // Default keywords

        alert.setAiRecommendations(String.join("; ", analysis.getRecommendedActions()));

        alert.setAiUrgencyScore(analysis.getUrgencyScore());

        alert.setAiImpactScore(analysis.getImpactScore());

        alert.setAiRiskFactors(String.join(", ", analysis.getRiskFactors()));

        alert.setTimeToEscalate(analysis.getTimeToEscalate());

        

        // Update alert severity based on AI analysis if more severe

        if (shouldUpgradeSeverity(alert.getSeverity(), analysis.getRecommendedPriority())) {

            alert.setSeverity(mapRiskLevelToSeverity(analysis.getRecommendedPriority()));

            log.info("Upgraded alert {} severity to {} based on AI analysis", alert.getId(), alert.getSeverity());

        }

    }

    

    /**

     * Enrich alert with threat evolution prediction

     */

    private void enrichAlertWithEvolution(Alert alert, ThreatEvolutionPrediction evolution) {

        alert.setPredictedSeverity(evolution.getPredictedSeverity());

        alert.setTimeToCritical(evolution.getTimeToCritical());

        alert.setEvolutionTrend(evolution.getEvolutionTrend());

        

        // Adjust urgency based on time to critical

        if (evolution.getTimeToCritical() <= 30) { // Less than 30 minutes

            alert.setAiUrgencyScore(Math.min(alert.getAiUrgencyScore() + 0.3, 1.0));

        } else if (evolution.getTimeToCritical() <= 60) { // Less than 1 hour

            alert.setAiUrgencyScore(Math.min(alert.getAiUrgencyScore() + 0.2, 1.0));

        }

    }

    

    /**

     * AI-based prioritization and routing

     */

    private void prioritizeAndRouteAlert(Alert alert, AlertPriorityAnalysis analysis, ThreatEvolutionPrediction evolution) {

        // Calculate AI priority score

        double priorityScore = calculateAIPriorityScore(alert, analysis, evolution);

        

        // Set priority based on AI score

        if (priorityScore >= 0.8) {

            alert.setPriority(Alert.Priority.HIGH);

        } else if (priorityScore >= 0.6) {

            alert.setPriority(Alert.Priority.NORMAL);

        } else if (priorityScore >= 0.4) {

            alert.setPriority(Alert.Priority.NORMAL);

        } else {

            alert.setPriority(Alert.Priority.LOW);

        }

        

        // Smart routing based on AI analysis

        String assignedTo = determineAssignment(alert, analysis, evolution);

        alert.setAssignedTo(assignedTo);

        

        // Set escalation info if high priority

        if (alert.getPriority() == Alert.Priority.HIGH) {

            setupEscalation(alert, evolution);

        }

    }

    

    /**

     * Calculate AI priority score based on multiple factors

     */

    private double calculateAIPriorityScore(Alert alert, AlertPriorityAnalysis analysis, ThreatEvolutionPrediction evolution) {

        double score = 0.0;

        

        // Base threat score (40% weight) - using confidence as proxy

        score += analysis.getConfidence() * 0.4;

        

        // Urgency score (25% weight)

        score += analysis.getUrgencyScore() * 0.25;

        

        // Impact score (20% weight)

        score += analysis.getImpactScore() * 0.2;

        

        // Time to critical factor (15% weight)

        double timeCriticalFactor = Math.max(0, 1.0 - (evolution.getTimeToCritical() / 240.0)); // 4 hours as baseline

        score += timeCriticalFactor * 0.15;

        

        return Math.min(score, 1.0);

    }

    

    /**

     * Determine optimal assignment based on AI analysis

     */

    private String determineAssignment(Alert alert, AlertPriorityAnalysis analysis, ThreatEvolutionPrediction evolution) {

        // Smart routing logic based on alert type, severity, and AI insights

        if (alert.getType() == Alert.AlertType.THREAT_DETECTION) {

            return "threat-analysis-team";

        } else if (alert.getType() == Alert.AlertType.RISK_FORECAST) {

            return "risk-assessment-team";

        } else if (alert.getType() == Alert.AlertType.SYSTEM_ALERT) {

            return "system-administrators";

        } else if ("critical".equals(evolution.getPredictedSeverity())) {

            return "incident-commander";

        } else if (analysis.getConfidence() >= 0.7) {

            return "senior-analyst";

        } else {

            return "junior-analyst";

        }

    }

    

    /**

     * Setup escalation information

     */

    private void setupEscalation(Alert alert, ThreatEvolutionPrediction evolution) {

        Alert.EscalationInfo escalation = Alert.EscalationInfo.builder()

            .escalationLevel(1)

            .escalatedAt(LocalDateTime.now())

            .escalatedBy("AI-System")

            .escalationReason("AI-predicted high threat evolution")

            .nextEscalation(LocalDateTime.now().plusMinutes(alert.getTimeToEscalate()))

            .escalationHistory(List.of("Initial AI escalation at " + LocalDateTime.now()))

            .build();

        

        alert.setEscalation(escalation);

    }

    

    /**

     * Set default AI values when AI analysis fails

     */

    private void setDefaultAIValues(Alert alert) {

        alert.setAiConfidence(0.5);

        alert.setAiThreatScore(0.5);

        alert.setAiRiskLevel("medium");

        alert.setAiAnalysis("AI analysis unavailable - using default assessment");

        alert.setAiKeywords("threat,alert,monitor");

        alert.setAiRecommendations("Monitor situation; Investigate further");

        alert.setAiUrgencyScore(0.5);

        alert.setAiImpactScore(0.5);

        alert.setAiRiskFactors("Limited data available");

        alert.setTimeToEscalate(120); // 2 hours

        alert.setPredictedSeverity(alert.getSeverity().toString());

        alert.setTimeToCritical(180); // 3 hours

        alert.setEvolutionTrend("stable");

        

        // Set default priority if not already set

        if (alert.getPriority() == null) {

            alert.setPriority(Alert.Priority.NORMAL);

        }

    }

    

    /**

     * Check if severity should be upgraded based on AI risk level

     */

    private boolean shouldUpgradeSeverity(Alert.Severity currentSeverity, String aiRiskLevel) {

        if (currentSeverity == Alert.Severity.LOW && !"low".equals(aiRiskLevel)) return true;

        if (currentSeverity == Alert.Severity.MEDIUM && ("high".equals(aiRiskLevel) || "critical".equals(aiRiskLevel))) return true;

        if (currentSeverity == Alert.Severity.HIGH && "critical".equals(aiRiskLevel)) return true;

        return false;

    }

    

    /**

     * Map AI risk level to alert severity

     */

    private Alert.Severity mapRiskLevelToSeverity(String aiRiskLevel) {

        switch (aiRiskLevel.toLowerCase()) {

            case "critical": return Alert.Severity.HIGH; // Use HIGH as highest available

            case "high": return Alert.Severity.HIGH;

            case "medium": return Alert.Severity.MEDIUM;

            case "low": return Alert.Severity.LOW;

            default: return Alert.Severity.MEDIUM;

        }

    }

    

    /**

     * Map string to Alert.Severity enum

     */

    private Alert.Severity mapStringToSeverity(String severityStr) {

        if (severityStr == null) return Alert.Severity.MEDIUM;

        

        switch (severityStr.toLowerCase()) {

            case "critical": return Alert.Severity.HIGH;

            case "high": return Alert.Severity.HIGH;

            case "medium": return Alert.Severity.MEDIUM;

            case "low": return Alert.Severity.LOW;

            default: return Alert.Severity.MEDIUM;

        }

    }

    

    /**

     * Map string to Alert.Priority enum

     */

    private Alert.Priority mapStringToPriority(String priorityStr) {

        if (priorityStr == null) return Alert.Priority.NORMAL;

        

        switch (priorityStr.toLowerCase()) {

            case "critical": return Alert.Priority.HIGH;

            case "high": return Alert.Priority.HIGH;

            case "normal": return Alert.Priority.NORMAL;

            case "medium": return Alert.Priority.NORMAL;

            case "low": return Alert.Priority.LOW;

            default: return Alert.Priority.NORMAL;

        }

    }

    

    @Override

    public void deleteAllAlerts() {

        alertRepository.deleteAll();

    }

    

    @Override

    public List<Alert> getAllAlerts() {

        return alertRepository.findAll();

    }

}


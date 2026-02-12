package com.ntews.alert.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@Component
@Slf4j
public class AIEngineClient {
    
    private final RestTemplate restTemplate;
    private final String aiEngineUrl;
    
    public AIEngineClient(
            RestTemplate restTemplate,
            @Value("${ai-engine.base-url:http://localhost:8000}") String aiEngineUrl) {
        this.restTemplate = restTemplate;
        this.aiEngineUrl = aiEngineUrl;
    }
    
    public AlertPriorityAnalysis predictAlertPriority(Map<String, Object> alertData) {
        try {
            String url = aiEngineUrl + "/predict";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            Map<String, Object> request = new HashMap<>();
            request.put("alert_data", alertData);
            request.put("type", "alert_priority_prediction");
            request.put("context", Map.of(
                "current_alerts", getActiveAlertsCount(),
                "time_of_day", LocalDateTime.now().getHour(),
                "location", alertData.get("location")
            ));
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            log.info("Predicting alert priority with AI Engine");
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> aiResponse = response.getBody();
                
                return AlertPriorityAnalysis.builder()
                    .recommendedPriority((String) aiResponse.getOrDefault("recommended_priority", "medium"))
                    .confidence(((Number) aiResponse.getOrDefault("confidence", 0.5)).doubleValue())
                    .urgencyScore(((Number) aiResponse.getOrDefault("urgency_score", 0.5)).doubleValue())
                    .impactScore(((Number) aiResponse.getOrDefault("impact_score", 0.5)).doubleValue())
                    .timeToEscalate(((Number) aiResponse.getOrDefault("time_to_escalate", 60)).intValue())
                    .riskFactors((List<String>) aiResponse.getOrDefault("risk_factors", new ArrayList<>()))
                    .recommendedActions((List<String>) aiResponse.getOrDefault("recommended_actions", new ArrayList<>()))
                    .analysis((String) aiResponse.getOrDefault("analysis", "AI priority analysis completed"))
                    .analysisTimestamp(LocalDateTime.now())
                    .build();
            } else {
                log.warn("AI Engine returned non-successful status for priority prediction: {}", response.getStatusCode());
                return getDefaultPriorityAnalysis();
            }
            
        } catch (Exception e) {
            log.error("Error predicting alert priority with AI Engine", e);
            return getDefaultPriorityAnalysis();
        }
    }
    
    public ThreatEvolutionPrediction predictThreatEvolution(String alertId, Map<String, Object> currentThreatData) {
        try {
            String url = aiEngineUrl + "/predict";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            Map<String, Object> request = new HashMap<>();
            request.put("current_threat", currentThreatData);
            request.put("alert_id", alertId);
            request.put("forecast_hours", 24);
            request.put("type", "threat_evolution_alert");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            log.info("Predicting threat evolution for alert: {}", alertId);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> aiResponse = response.getBody();
                
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> evolutionSteps = (List<Map<String, Object>>) aiResponse.getOrDefault("evolution_steps", new ArrayList<>());
                List<String> escalationFactors = (List<String>) aiResponse.getOrDefault("escalation_factors", new ArrayList<>());
                List<String> mitigationOpportunities = (List<String>) aiResponse.getOrDefault("mitigation_opportunities", new ArrayList<>());
                
                return ThreatEvolutionPrediction.builder()
                    .alertId(alertId)
                    .predictedSeverity((String) aiResponse.getOrDefault("predicted_severity", "medium"))
                    .evolutionTrend((String) aiResponse.getOrDefault("evolution_trend", "stable"))
                    .timeToCritical(((Number) aiResponse.getOrDefault("time_to_critical", 180)).intValue())
                    .confidence(((Number) aiResponse.getOrDefault("confidence", 0.5)).doubleValue())
                    .evolutionSteps(evolutionSteps)
                    .riskEscalationFactors(escalationFactors)
                    .mitigationOpportunities(mitigationOpportunities)
                    .predictionTimestamp(LocalDateTime.now())
                    .build();
            } else {
                log.warn("AI Engine returned non-successful status for threat evolution: {}", response.getStatusCode());
                return getDefaultThreatEvolution(alertId);
            }
            
        } catch (Exception e) {
            log.error("Error predicting threat evolution for alert: {}", alertId, e);
            return getDefaultThreatEvolution(alertId);
        }
    }
    
    public List<String> generatePreventiveActions(Map<String, Object> alertData, ThreatEvolutionPrediction evolution) {
        try {
            String url = aiEngineUrl + "/analyze";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            Map<String, Object> request = new HashMap<>();
            request.put("id", alertData.get("id"));
            request.put("type", "preventive_actions");
            request.put("content", String.format("%s: %s", alertData.get("title"), alertData.get("description")));
            request.put("severity", alertData.get("severity"));
            request.put("location", alertData.get("location"));
            request.put("evolution_prediction", Map.of(
                "trend", evolution.getEvolutionTrend(),
                "time_to_critical", evolution.getTimeToCritical(),
                "risk_factors", evolution.getRiskEscalationFactors()
            ));
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            log.info("Generating preventive actions for alert: {}", alertData.get("id"));
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> aiResponse = response.getBody();
                @SuppressWarnings("unchecked")
                List<String> actions = (List<String>) aiResponse.getOrDefault("preventive_actions", getDefaultPreventiveActions(alertData, evolution));
                return actions;
            } else {
                log.warn("AI Engine returned non-successful status for preventive actions: {}", response.getStatusCode());
                return getDefaultPreventiveActions(alertData, evolution);
            }
            
        } catch (Exception e) {
            log.error("Error generating preventive actions for alert: {}", alertData.get("id"), e);
            return getDefaultPreventiveActions(alertData, evolution);
        }
    }
    
    public boolean isHealthy() {
        try {
            String url = aiEngineUrl + "/health";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("AI Engine health check failed", e);
            return false;
        }
    }
    
    private int getActiveAlertsCount() {
        // This would typically call the alert repository
        // For now, return a mock value
        return 5;
    }
    
    private AlertPriorityAnalysis getDefaultPriorityAnalysis() {
        return AlertPriorityAnalysis.builder()
            .recommendedPriority("medium")
            .confidence(0.5)
            .urgencyScore(0.5)
            .impactScore(0.5)
            .timeToEscalate(60)
            .riskFactors(new ArrayList<>())
            .recommendedActions(new ArrayList<>())
            .analysis("AI Engine unavailable - using default priority analysis")
            .analysisTimestamp(LocalDateTime.now())
            .build();
    }
    
    private ThreatEvolutionPrediction getDefaultThreatEvolution(String alertId) {
        return ThreatEvolutionPrediction.builder()
            .alertId(alertId)
            .predictedSeverity("medium")
            .evolutionTrend("stable")
            .timeToCritical(180) // 3 hours
            .confidence(0.5)
            .evolutionSteps(new ArrayList<>())
            .riskEscalationFactors(new ArrayList<>())
            .mitigationOpportunities(new ArrayList<>())
            .predictionTimestamp(LocalDateTime.now())
            .build();
    }
    
    private List<String> getDefaultPreventiveActions(Map<String, Object> alertData, ThreatEvolutionPrediction evolution) {
        List<String> actions = new ArrayList<>();
        
        // Base actions
        actions.add("Monitor situation closely");
        actions.add("Prepare response team");
        
        // Time-based actions
        if (evolution.getTimeToCritical() < 60) {
            actions.add("IMMEDIATE: Deploy emergency response");
            actions.add("IMMEDIATE: Consider evacuation if necessary");
        } else if (evolution.getTimeToCritical() < 180) {
            actions.add("Stage resources nearby");
            actions.add("Prepare contingency plans");
        }
        
        // Severity-based actions
        String severity = (String) alertData.getOrDefault("severity", "medium");
        if ("critical".equals(severity)) {
            actions.add("Maximum alert level - all hands on deck");
            actions.add("Activate emergency protocols");
        } else if ("high".equals(severity)) {
            actions.add("Elevate alert to senior management");
            actions.add("Prepare for escalation");
        }
        
        return actions;
    }
    
    // DTOs for AI responses
    public static class AlertPriorityAnalysis {
        private String recommendedPriority;
        private double confidence;
        private double urgencyScore;
        private double impactScore;
        private int timeToEscalate; // minutes
        private List<String> riskFactors;
        private List<String> recommendedActions;
        private String analysis;
        private LocalDateTime analysisTimestamp;
        
        public static Builder builder() { return new Builder(); }
        
        public static class Builder {
            private AlertPriorityAnalysis analysis = new AlertPriorityAnalysis();
            
            public Builder recommendedPriority(String priority) { analysis.recommendedPriority = priority; return this; }
            public Builder confidence(double confidence) { analysis.confidence = confidence; return this; }
            public Builder urgencyScore(double score) { analysis.urgencyScore = score; return this; }
            public Builder impactScore(double score) { analysis.impactScore = score; return this; }
            public Builder timeToEscalate(int minutes) { analysis.timeToEscalate = minutes; return this; }
            public Builder riskFactors(List<String> factors) { analysis.riskFactors = factors; return this; }
            public Builder recommendedActions(List<String> actions) { analysis.recommendedActions = actions; return this; }
            public Builder analysis(String analysisText) { this.analysis.analysis = analysisText; return this; }
            public Builder analysisTimestamp(LocalDateTime timestamp) { analysis.analysisTimestamp = timestamp; return this; }
            
            public AlertPriorityAnalysis build() { return analysis; }
        }
        
        // Getters
        public String getRecommendedPriority() { return recommendedPriority; }
        public double getConfidence() { return confidence; }
        public double getUrgencyScore() { return urgencyScore; }
        public double getImpactScore() { return impactScore; }
        public int getTimeToEscalate() { return timeToEscalate; }
        public List<String> getRiskFactors() { return riskFactors; }
        public List<String> getRecommendedActions() { return recommendedActions; }
        public String getAnalysis() { return analysis; }
        public LocalDateTime getAnalysisTimestamp() { return analysisTimestamp; }
    }
    
    public static class ThreatEvolutionPrediction {
        private String alertId;
        private String predictedSeverity;
        private String evolutionTrend;
        private int timeToCritical; // minutes
        private double confidence;
        private List<Map<String, Object>> evolutionSteps;
        private List<String> riskEscalationFactors;
        private List<String> mitigationOpportunities;
        private LocalDateTime predictionTimestamp;
        
        public static Builder builder() { return new Builder(); }
        
        public static class Builder {
            private ThreatEvolutionPrediction prediction = new ThreatEvolutionPrediction();
            
            public Builder alertId(String id) { prediction.alertId = id; return this; }
            public Builder predictedSeverity(String severity) { prediction.predictedSeverity = severity; return this; }
            public Builder evolutionTrend(String trend) { prediction.evolutionTrend = trend; return this; }
            public Builder timeToCritical(int minutes) { prediction.timeToCritical = minutes; return this; }
            public Builder confidence(double confidence) { prediction.confidence = confidence; return this; }
            public Builder evolutionSteps(List<Map<String, Object>> steps) { prediction.evolutionSteps = steps; return this; }
            public Builder riskEscalationFactors(List<String> factors) { prediction.riskEscalationFactors = factors; return this; }
            public Builder mitigationOpportunities(List<String> opportunities) { prediction.mitigationOpportunities = opportunities; return this; }
            public Builder predictionTimestamp(LocalDateTime timestamp) { prediction.predictionTimestamp = timestamp; return this; }
            
            public ThreatEvolutionPrediction build() { return prediction; }
        }
        
        // Getters
        public String getAlertId() { return alertId; }
        public String getPredictedSeverity() { return predictedSeverity; }
        public String getEvolutionTrend() { return evolutionTrend; }
        public int getTimeToCritical() { return timeToCritical; }
        public double getConfidence() { return confidence; }
        public List<Map<String, Object>> getEvolutionSteps() { return evolutionSteps; }
        public List<String> getRiskEscalationFactors() { return riskEscalationFactors; }
        public List<String> getMitigationOpportunities() { return mitigationOpportunities; }
        public LocalDateTime getPredictionTimestamp() { return predictionTimestamp; }
    }
}

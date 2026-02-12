package com.ntews.ingestion.client;

import com.ntews.ingestion.model.ThreatData;
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
    
    public ThreatAnalysis analyzeInRealTime(ThreatData threatData) {
        try {
            String url = aiEngineUrl + "/analyze";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            // Convert ThreatData to AI Engine format
            Map<String, Object> request = new HashMap<>();
            request.put("id", threatData.getId());
            request.put("type", threatData.getType());
            request.put("source", threatData.getSource());
            request.put("sourceType", threatData.getSourceType());
            request.put("content", threatData.getContent());
            request.put("timestamp", threatData.getTimestamp());
            request.put("location", threatData.getLocation());
            request.put("confidence", threatData.getConfidence());
            request.put("severity", threatData.getSeverity());
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            log.info("Real-time AI analysis for threat: {}", threatData.getId());
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> aiResponse = response.getBody();
                
                return ThreatAnalysis.builder()
                    .threatId(threatData.getId())
                    .threatScore(((Number) aiResponse.getOrDefault("threat_score", 0.5)).doubleValue())
                    .riskLevel((String) aiResponse.getOrDefault("risk_level", "medium"))
                    .confidence(((Number) aiResponse.getOrDefault("confidence", 0.5)).doubleValue())
                    .keyEntities((List<String>) aiResponse.getOrDefault("key_entities", new ArrayList<>()))
                    .threatKeywords((List<String>) aiResponse.getOrDefault("threat_keywords", new ArrayList<>()))
                    .analysisText((String) aiResponse.getOrDefault("analysis", "Real-time threat analysis completed"))
                    .analysisTimestamp(LocalDateTime.now())
                    .build();
            } else {
                log.warn("AI Engine returned non-successful status: {}", response.getStatusCode());
                return getDefaultAnalysis(threatData.getId());
            }
            
        } catch (Exception e) {
            log.error("Error in real-time AI analysis for threat: {}", threatData.getId(), e);
            return getDefaultAnalysis(threatData.getId());
        }
    }
    
    public boolean isHighRiskThreat(ThreatData threatData) {
        try {
            ThreatAnalysis analysis = analyzeInRealTime(threatData);
            return analysis.getThreatScore() > 0.7 || "critical".equals(analysis.getRiskLevel());
        } catch (Exception e) {
            log.error("Error determining high-risk status for threat: {}", threatData.getId(), e);
            return false;
        }
    }
    
    public PredictionResult predictThreatEvolution(ThreatData threatData) {
        try {
            String url = aiEngineUrl + "/predict";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            Map<String, Object> request = new HashMap<>();
            request.put("historical_data", Map.of(
                "current_threat", threatData.getContent(),
                "location", threatData.getLocation(),
                "source_type", threatData.getSourceType(),
                "severity", threatData.getSeverity()
            ));
            request.put("forecast_hours", 24);
            request.put("type", "threat_evolution");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            log.info("Predicting threat evolution for: {}", threatData.getId());
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> prediction = response.getBody();
                
                return PredictionResult.builder()
                    .threatId(threatData.getId())
                    .predictedSeverity((String) prediction.getOrDefault("predicted_severity", threatData.getSeverity()))
                    .timeToCritical(((Number) prediction.getOrDefault("time_to_critical", 1440)).intValue()) // minutes
                    .evolutionTrend((String) prediction.getOrDefault("trend", "stable"))
                    .confidence(((Number) prediction.getOrDefault("confidence", 0.5)).doubleValue())
                    .riskFactors((List<String>) prediction.getOrDefault("risk_factors", new ArrayList<>()))
                    .predictedImpact((String) prediction.getOrDefault("predicted_impact", "medium"))
                    .predictionTimestamp(LocalDateTime.now())
                    .build();
            } else {
                log.warn("AI Engine prediction failed for: {}", threatData.getId());
                return getDefaultPrediction(threatData.getId());
            }
            
        } catch (Exception e) {
            log.error("Error predicting threat evolution for: {}", threatData.getId(), e);
            return getDefaultPrediction(threatData.getId());
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
    
    private ThreatAnalysis getDefaultAnalysis(String threatId) {
        return ThreatAnalysis.builder()
            .threatId(threatId)
            .threatScore(0.5)
            .riskLevel("medium")
            .confidence(0.5)
            .keyEntities(new ArrayList<>())
            .threatKeywords(new ArrayList<>())
            .analysisText("AI Engine unavailable - using default analysis")
            .analysisTimestamp(LocalDateTime.now())
            .build();
    }
    
    private PredictionResult getDefaultPrediction(String threatId) {
        return PredictionResult.builder()
            .threatId(threatId)
            .predictedSeverity("medium")
            .timeToCritical(1440) // 24 hours
            .evolutionTrend("stable")
            .confidence(0.5)
            .riskFactors(new ArrayList<>())
            .predictedImpact("medium")
            .predictionTimestamp(LocalDateTime.now())
            .build();
    }
    
    // DTOs for AI responses
    public static class ThreatAnalysis {
        private String threatId;
        private double threatScore;
        private String riskLevel;
        private double confidence;
        private List<String> keyEntities;
        private List<String> threatKeywords;
        private String analysis;
        private LocalDateTime analysisTimestamp;
        
        // Builder pattern implementation
        public static Builder builder() { return new Builder(); }
        
        public static class Builder {
            private ThreatAnalysis analysis = new ThreatAnalysis();
            
            public Builder threatId(String threatId) { analysis.threatId = threatId; return this; }
            public Builder threatScore(double threatScore) { analysis.threatScore = threatScore; return this; }
            public Builder riskLevel(String riskLevel) { analysis.riskLevel = riskLevel; return this; }
            public Builder confidence(double confidence) { analysis.confidence = confidence; return this; }
            public Builder keyEntities(List<String> keyEntities) { analysis.keyEntities = keyEntities; return this; }
            public Builder threatKeywords(List<String> threatKeywords) { analysis.threatKeywords = threatKeywords; return this; }
            public Builder analysisText(String analysis) { this.analysis.analysis = analysis; return this; }
            public Builder analysisTimestamp(LocalDateTime timestamp) { analysis.analysisTimestamp = timestamp; return this; }
            
            public ThreatAnalysis build() { return analysis; }
        }
        
        // Getters
        public String getThreatId() { return threatId; }
        public double getThreatScore() { return threatScore; }
        public String getRiskLevel() { return riskLevel; }
        public double getConfidence() { return confidence; }
        public List<String> getKeyEntities() { return keyEntities; }
        public List<String> getThreatKeywords() { return threatKeywords; }
        public String getAnalysis() { return analysis; }
        public LocalDateTime getAnalysisTimestamp() { return analysisTimestamp; }
    }
    
    public static class PredictionResult {
        private String threatId;
        private String predictedSeverity;
        private int timeToCritical; // minutes
        private String evolutionTrend;
        private double confidence;
        private List<String> riskFactors;
        private String predictedImpact;
        private LocalDateTime predictionTimestamp;
        
        // Builder pattern implementation
        public static Builder builder() { return new Builder(); }
        
        public static class Builder {
            private PredictionResult prediction = new PredictionResult();
            
            public Builder threatId(String threatId) { prediction.threatId = threatId; return this; }
            public Builder predictedSeverity(String severity) { prediction.predictedSeverity = severity; return this; }
            public Builder timeToCritical(int minutes) { prediction.timeToCritical = minutes; return this; }
            public Builder evolutionTrend(String trend) { prediction.evolutionTrend = trend; return this; }
            public Builder confidence(double confidence) { prediction.confidence = confidence; return this; }
            public Builder riskFactors(List<String> factors) { prediction.riskFactors = factors; return this; }
            public Builder predictedImpact(String impact) { prediction.predictedImpact = impact; return this; }
            public Builder predictionTimestamp(LocalDateTime timestamp) { prediction.predictionTimestamp = timestamp; return this; }
            
            public PredictionResult build() { return prediction; }
        }
        
        // Getters
        public String getThreatId() { return threatId; }
        public String getPredictedSeverity() { return predictedSeverity; }
        public int getTimeToCritical() { return timeToCritical; }
        public String getEvolutionTrend() { return evolutionTrend; }
        public double getConfidence() { return confidence; }
        public List<String> getRiskFactors() { return riskFactors; }
        public String getPredictedImpact() { return predictedImpact; }
        public LocalDateTime getPredictionTimestamp() { return predictionTimestamp; }
    }
}

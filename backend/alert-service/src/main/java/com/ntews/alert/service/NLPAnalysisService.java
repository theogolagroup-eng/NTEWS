package com.ntews.alert.service;

import com.ntews.alert.model.Alert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

/**
 * NLP Analysis Service for Alert Service
 * Integrates with AI Engine for text analysis
 */
@Service
@Slf4j
public class NLPAnalysisService {
    
    private final RestTemplate restTemplate;
    private final String aiEngineUrl;
    
    public NLPAnalysisService(RestTemplate restTemplate, 
                           @Value("${ai.engine.url:http://localhost:8000}") String aiEngineUrl) {
        this.restTemplate = restTemplate;
        this.aiEngineUrl = aiEngineUrl;
    }
    
    /**
     * Analyze alert using NLP from AI Engine
     */
    public Alert analyzeAlertWithNLP(Alert alert) {
        try {
            // Prepare request for AI Engine
            Map<String, Object> request = new HashMap<>();
            request.put("alert_id", alert.getId());
            request.put("title", alert.getTitle());
            request.put("description", alert.getDescription());
            request.put("category", alert.getCategory());
            request.put("source", alert.getSource());
            
            // Call AI Engine NLP endpoint
            String url = aiEngineUrl + "/nlp/analyze-alert";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> nlpResult = response.getBody();
                
                // Update alert with NLP analysis
                updateAlertWithNLPAnalysis(alert, nlpResult);
                
                log.info("NLP analysis completed for alert: {}", alert.getId());
            } else {
                log.warn("NLP analysis failed for alert: {}", alert.getId());
                setDefaultNLPValues(alert);
            }
            
        } catch (Exception e) {
            log.error("Error during NLP analysis for alert {}: {}", alert.getId(), e.getMessage());
            setDefaultNLPValues(alert);
        }
        
        return alert;
    }
    
    /**
     * Analyze text content for threat detection
     */
    public Map<String, Object> analyzeTextThreat(String text, String context) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("text", text);
            if (context != null) {
                request.put("context", context);
            }
            
            String url = aiEngineUrl + "/nlp/analyze-text";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            
        } catch (Exception e) {
            log.error("Error during text threat analysis: {}", e.getMessage());
        }
        
        return getDefaultNLPResponse();
    }
    
    /**
     * Batch analyze multiple texts
     */
    public Map<String, Object> batchAnalyzeTexts(List<Map<String, String>> texts) {
        try {
            String url = aiEngineUrl + "/nlp/batch-analyze";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<List<Map<String, String>>> entity = new HttpEntity<>(texts, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            
        } catch (Exception e) {
            log.error("Error during batch text analysis: {}", e.getMessage());
        }
        
        return Map.of(
            "batch_id", "failed_batch",
            "processed_count", 0,
            "results", List.of(),
            "timestamp", new Date()
        );
    }
    
    /**
     * Get NLP capabilities from AI Engine
     */
    public Map<String, Object> getNLPCapabilities() {
        try {
            String url = aiEngineUrl + "/nlp/capabilities";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            
        } catch (Exception e) {
            log.error("Error getting NLP capabilities: {}", e.getMessage());
        }
        
        return Map.of("error", "Unable to fetch NLP capabilities");
    }
    
    /**
     * Update alert with NLP analysis results
     */
    private void updateAlertWithNLPAnalysis(Alert alert, Map<String, Object> nlpResult) {
        // Set NLP risk score
        if (nlpResult.containsKey("nlp_risk_score")) {
            alert.setNlpRiskScore(((Number) nlpResult.get("nlp_risk_score")).doubleValue());
        }
        
        // Set combined risk score
        if (nlpResult.containsKey("combined_risk_score")) {
            alert.setCombinedRiskScore(((Number) nlpResult.get("combined_risk_score")).doubleValue());
        }
        
        // Set NLP analysis
        if (nlpResult.containsKey("nlp_analysis")) {
            Map<String, Object> nlpAnalysis = (Map<String, Object>) nlpResult.get("nlp_analysis");
            
            alert.setNlpClassification((String) nlpAnalysis.get("classification"));
            alert.setNlpConfidence(((Number) nlpAnalysis.get("confidence")).doubleValue());
            
            // Set threat keywords
            if (nlpAnalysis.containsKey("threat_keywords")) {
                alert.setThreatKeywords((List<String>) nlpAnalysis.get("threat_keywords"));
            }
            
            // Set sentiment scores
            if (nlpAnalysis.containsKey("sentiment_scores")) {
                alert.setSentimentScores((Map<String, Object>) nlpAnalysis.get("sentiment_scores"));
            }
            
            // Set recommendations
            if (nlpAnalysis.containsKey("recommendations")) {
                alert.setNlpRecommendations((List<String>) nlpAnalysis.get("recommendations"));
            }
        }
        
        // Set priority recommendation
        if (nlpResult.containsKey("priority_recommendation")) {
            alert.setPriorityRecommendation((String) nlpResult.get("priority_recommendation"));
        }
        
        // Set analysis timestamp
        alert.setNlpAnalyzedAt(java.time.LocalDateTime.now());
    }
    
    /**
     * Set default NLP values when analysis fails
     */
    private void setDefaultNLPValues(Alert alert) {
        alert.setNlpRiskScore(0.1);
        alert.setNlpClassification("benign");
        alert.setNlpConfidence(0.5);
        alert.setThreatKeywords(List.of());
        alert.setSentimentScores(Map.of(
            "positive", 0.3,
            "negative", 0.3,
            "neutral", 0.4
        ));
        alert.setNlpRecommendations(List.of("Continue monitoring"));
        alert.setCombinedRiskScore(0.2);
        alert.setPriorityRecommendation("LOW - Routine Monitoring");
        alert.setNlpAnalyzedAt(java.time.LocalDateTime.now());
    }
    
    /**
     * Get default NLP response for fallback
     */
    private Map<String, Object> getDefaultNLPResponse() {
        return Map.of(
            "text", "",
            "classification", "benign",
            "confidence", 0.5,
            "threat_probabilities", Map.of(
                "benign", 0.8,
                "suspicious", 0.15,
                "threat", 0.05
            ),
            "sentiment_scores", Map.of(
                "positive", 0.3,
                "negative", 0.3,
                "neutral", 0.4
            ),
            "threat_keywords", List.of(),
            "risk_score", 0.1,
            "recommendations", List.of("Continue monitoring")
        );
    }
}

package com.ntews.alert.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * AI Engine Integration Service for Enhanced Threat Analysis
 * Integrates with the rule-based AI Engine for multilingual threat detection
 */
@Service
@Slf4j
public class AIEngineIntegrationService {

    private final WebClient aiEngineWebClient;
    private final ObjectMapper objectMapper;
    private final String aiEngineUrl;

    public AIEngineIntegrationService(WebClient.Builder webClientBuilder,
                                     ObjectMapper objectMapper,
                                     @Value("${ai.engine.base.url:http://localhost:8000}") String aiEngineUrl) {
        this.aiEngineWebClient = webClientBuilder
                .baseUrl(aiEngineUrl)
                .build();
        this.objectMapper = objectMapper;
        this.aiEngineUrl = aiEngineUrl;
    }

    /**
     * Analyze text content using AI Engine for threat detection
     */
    public Mono<ThreatAnalysisResult> analyzeTextThreat(String text, String context) {
        log.info("🧠 Analyzing text with AI Engine: {}", text.substring(0, Math.min(50, text.length())));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("text", text);
        requestBody.put("context", context != null ? context : "security_alert");
        requestBody.put("source", "alert_service");
        requestBody.put("tweet_id", "alert_" + System.currentTimeMillis());
        requestBody.put("author_id", "system");

        return aiEngineWebClient.post()
                .uri("/nlp/analyze-sheng")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseAIResponse)
                .doOnSuccess(result -> log.info("✅ AI Engine analysis complete: {} (confidence: {})", 
                    result.getClassification(), result.getConfidence()))
                .doOnError(error -> log.error("❌ AI Engine analysis failed: {}", error.getMessage()))
                .onErrorReturn(ThreatAnalysisResult.getDefault());
    }

    /**
     * Parse AI Engine response into ThreatAnalysisResult
     */
    private ThreatAnalysisResult parseAIResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            
            String classification = root.path("classification").asText("benign");
            double confidence = root.path("confidence").asDouble(0.5);
            double riskScore = root.path("risk_score").asDouble(0.2);
            
            // Parse threat keywords
            JsonNode keywordsNode = root.path("threat_keywords");
            StringBuilder keywords = new StringBuilder();
            if (keywordsNode.isArray()) {
                for (JsonNode keyword : keywordsNode) {
                    if (keywords.length() > 0) keywords.append(", ");
                    keywords.append(keyword.asText());
                }
            }
            
            // Parse threat probabilities
            JsonNode probabilitiesNode = root.path("threat_probabilities");
            Map<String, Double> probabilities = new HashMap<>();
            probabilitiesNode.fields().forEachRemaining(entry -> 
                probabilities.put(entry.getKey(), entry.getValue().asDouble()));
            
            // Parse Sheng words
            JsonNode shengNode = root.path("sheng_words_detected");
            StringBuilder shengWords = new StringBuilder();
            if (shengNode.isArray()) {
                for (JsonNode sheng : shengNode) {
                    if (shengWords.length() > 0) shengWords.append(", ");
                    shengWords.append(sheng.asText());
                }
            }
            
            // Parse context enhancement
            JsonNode contextNode = root.path("context_enhancement");
            double eastAfricanRelevance = contextNode.path("east_african_relevance").asDouble(0.0);
            boolean politicalContext = contextNode.path("political_context").asBoolean(false);
            
            return ThreatAnalysisResult.builder()
                .classification(classification)
                .confidence(confidence)
                .riskScore(riskScore)
                .threatKeywords(keywords.toString())
                .shengWordsDetected(shengWords.toString())
                .eastAfricanRelevance(eastAfricanRelevance)
                .politicalContext(politicalContext)
                .threatProbabilities(probabilities)
                .analyzedAt(LocalDateTime.now())
                .processingMethod(root.path("processing_metadata").path("method").asText("rule_based"))
                .detectedLanguage(root.path("detected_language").asText("unknown"))
                .build();
                
        } catch (Exception e) {
            log.error("Failed to parse AI Engine response: {}", e.getMessage());
            return ThreatAnalysisResult.getDefault();
        }
    }

    /**
     * Check AI Engine health
     */
    public Mono<Boolean> isAIEngineHealthy() {
        return aiEngineWebClient.get()
                .uri("/health")
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> response.contains("healthy"))
                .onErrorReturn(false);
    }

    /**
     * Result container for AI Engine threat analysis
     */
    public static class ThreatAnalysisResult {
        private String classification;
        private double confidence;
        private double riskScore;
        private String threatKeywords;
        private String shengWordsDetected;
        private double eastAfricanRelevance;
        private boolean politicalContext;
        private Map<String, Double> threatProbabilities;
        private LocalDateTime analyzedAt;
        private String processingMethod;
        private String detectedLanguage;

        // Builder pattern
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private ThreatAnalysisResult result = new ThreatAnalysisResult();

            public Builder classification(String classification) {
                result.classification = classification;
                return this;
            }

            public Builder confidence(double confidence) {
                result.confidence = confidence;
                return this;
            }

            public Builder riskScore(double riskScore) {
                result.riskScore = riskScore;
                return this;
            }

            public Builder threatKeywords(String threatKeywords) {
                result.threatKeywords = threatKeywords;
                return this;
            }

            public Builder shengWordsDetected(String shengWordsDetected) {
                result.shengWordsDetected = shengWordsDetected;
                return this;
            }

            public Builder eastAfricanRelevance(double eastAfricanRelevance) {
                result.eastAfricanRelevance = eastAfricanRelevance;
                return this;
            }

            public Builder politicalContext(boolean politicalContext) {
                result.politicalContext = politicalContext;
                return this;
            }

            public Builder threatProbabilities(Map<String, Double> threatProbabilities) {
                result.threatProbabilities = threatProbabilities;
                return this;
            }

            public Builder analyzedAt(LocalDateTime analyzedAt) {
                result.analyzedAt = analyzedAt;
                return this;
            }

            public Builder processingMethod(String processingMethod) {
                result.processingMethod = processingMethod;
                return this;
            }

            public Builder detectedLanguage(String detectedLanguage) {
                result.detectedLanguage = detectedLanguage;
                return this;
            }

            public ThreatAnalysisResult build() {
                return result;
            }
        }

        // Getters
        public String getClassification() { return classification; }
        public double getConfidence() { return confidence; }
        public double getRiskScore() { return riskScore; }
        public String getThreatKeywords() { return threatKeywords; }
        public String getShengWordsDetected() { return shengWordsDetected; }
        public double getEastAfricanRelevance() { return eastAfricanRelevance; }
        public boolean isPoliticalContext() { return politicalContext; }
        public Map<String, Double> getThreatProbabilities() { return threatProbabilities; }
        public LocalDateTime getAnalyzedAt() { return analyzedAt; }
        public String getProcessingMethod() { return processingMethod; }
        public String getDetectedLanguage() { return detectedLanguage; }

        public static ThreatAnalysisResult getDefault() {
            return builder()
                .classification("benign")
                .confidence(0.5)
                .riskScore(0.2)
                .threatKeywords("")
                .shengWordsDetected("")
                .eastAfricanRelevance(0.0)
                .politicalContext(false)
                .threatProbabilities(Map.of("benign", 0.7, "suspicious", 0.2, "threat", 0.1))
                .analyzedAt(LocalDateTime.now())
                .processingMethod("fallback")
                .detectedLanguage("unknown")
                .build();
        }

        public boolean isThreat() {
            return "threat".equals(classification) || "suspicious".equals(classification);
        }

        public boolean isHighConfidence() {
            return confidence >= 0.7;
        }

        public boolean hasEastAfricanContext() {
            return eastAfricanRelevance > 0.5;
        }
    }
}

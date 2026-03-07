package com.ntews.ingestion.service;

import com.ntews.ingestion.document.TwitterStreamDocument;
import com.ntews.ingestion.repository.TwitterStreamRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for processing Twitter data and saving to MongoDB
 * Handles AI Engine integration and data persistence
 */
@Service
@Slf4j
public class TwitterProcessingService {

    private final TwitterStreamRepository twitterStreamRepository;
    private final WebClient aiEngineWebClient;
    private final ObjectMapper objectMapper;

    public TwitterProcessingService(TwitterStreamRepository twitterStreamRepository,
                                   WebClient.Builder webClientBuilder) {
        this.twitterStreamRepository = twitterStreamRepository;
        this.aiEngineWebClient = webClientBuilder
                .baseUrl("http://localhost:8000")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Process tweet with AI Engine and save to MongoDB
     */
    public void processTweet(TwitterStreamDocument document) {
        try {
            // Process with AI Engine
            processWithAIEngine(document)
                .subscribe(
                    processedDoc -> {
                        // Save to MongoDB
                        twitterStreamRepository.save(processedDoc);
                        log.info("Saved processed tweet {} to MongoDB", processedDoc.getTweetId());
                        
                        // Log security findings
                        if (processedDoc.getIsThreat() != null && processedDoc.getIsThreat()) {
                            log.warn("THREAT DETECTED: {} (Risk: {}, Confidence: {})", 
                                processedDoc.getText(), 
                                processedDoc.getRiskScore(), 
                                processedDoc.getConfidence());
                        }
                    },
                    error -> {
                        log.error("Failed to process tweet {}: {}", document.getTweetId(), error.getMessage());
                        // Save even if AI processing fails
                        twitterStreamRepository.save(document);
                    }
                );
                
        } catch (Exception e) {
            log.error("Error processing tweet {}: {}", document.getTweetId(), e.getMessage());
        }
    }

    /**
     * Process document with AI Engine
     */
    private Mono<TwitterStreamDocument> processWithAIEngine(TwitterStreamDocument document) {
        // Prepare request for AI Engine
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("text", document.getText());
        requestData.put("context", document.getContext());
        requestData.put("source", document.getSource());
        requestData.put("tweet_id", document.getTweetId());
        requestData.put("author_id", document.getAuthorId());
        
        if (document.getPublicMetrics() != null) {
            requestData.put("public_metrics", document.getPublicMetrics());
        }
        
        requestData.put("language", document.getLanguage());
        requestData.put("created_at", document.getCreatedAt());

        return aiEngineWebClient.post()
                .uri("/nlp/analyze-sheng")
                .bodyValue(requestData)
                .retrieve()
                .bodyToMono(String.class)
                .map(aiResponse -> {
                    // Parse AI response and update document
                    updateDocumentWithAIResults(document, aiResponse);
                    return document;
                })
                .doOnSuccess(doc -> log.debug("AI Engine processed tweet {}: {}", document.getTweetId(), "AI analysis complete"))
                .onErrorReturn(document); // Return original document if AI processing fails
    }

    /**
     * Update document with AI Engine analysis results
     */
    @SuppressWarnings("unchecked")
    private void updateDocumentWithAIResults(TwitterStreamDocument document, String aiResponse) {
        try {
            // Parse AI response
            Map<String, Object> aiResults = objectMapper.readValue(aiResponse, Map.class);
            
            // Update document with AI results
            document.setShengDetected((Boolean) aiResults.getOrDefault("sheng_detected", false));
            document.setOriginalLanguage((String) aiResults.getOrDefault("original_language", "unknown"));
            document.setClassification((String) aiResults.getOrDefault("classification", "benign"));
            document.setConfidence((Double) aiResults.getOrDefault("confidence", 0.0));
            document.setShengWordsDetected((java.util.List<String>) aiResults.getOrDefault("sheng_words_detected", java.util.Collections.emptyList()));
            document.setRiskScore((Double) aiResults.getOrDefault("risk_score", 0.0));
            document.setProcessorUsed((String) aiResults.getOrDefault("processor_used", "standard"));
            document.setThreatProbabilities((Map<String, Double>) aiResults.getOrDefault("threat_probabilities", new HashMap<>()));
            document.setSentimentScores((Map<String, Double>) aiResults.getOrDefault("sentiment_scores", new HashMap<>()));
            document.setThreatKeywords((java.util.List<String>) aiResults.getOrDefault("threat_keywords", java.util.Collections.emptyList()));
            document.setRecommendations((java.util.List<String>) aiResults.getOrDefault("recommendations", java.util.Collections.emptyList()));
            document.setNormalizedText((String) aiResults.getOrDefault("normalized_text", document.getText()));
            
            // Store raw AI response for debugging
            document.setAiEngineResponse(aiResponse);
            
            // Set security classification
            updateSecurityClassification(document);
            
        } catch (Exception e) {
            log.error("Error parsing AI response for tweet {}: {}", document.getTweetId(), e.getMessage());
        }
    }

    /**
     * Update security classification based on AI results
     */
    private void updateSecurityClassification(TwitterStreamDocument document) {
        // Determine if it's a threat
        String classification = document.getClassification();
        double riskScore = document.getRiskScore();
        double confidence = document.getConfidence();
        
        document.setIsThreat(false);
        document.setRiskCategory("low");
        document.setSecurityRelevance("monitoring");
        
        if ("threat".equals(classification) || "civil_unrest".equals(classification)) {
            document.setIsThreat(true);
            document.setRiskCategory("high");
            document.setSecurityRelevance("immediate");
        } else if ("suspicious".equals(classification)) {
            document.setIsThreat(riskScore > 0.7 && confidence > 0.6);
            document.setRiskCategory("medium");
            document.setSecurityRelevance("investigate");
        } else if (riskScore > 0.8 && confidence > 0.7) {
            document.setIsThreat(true);
            document.setRiskCategory("medium");
            document.setSecurityRelevance("investigate");
        }
    }
}

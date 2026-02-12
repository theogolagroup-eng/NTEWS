package com.ntews.intel.service;

import com.ntews.intel.model.ThreatData;
import com.ntews.intel.model.SocialMediaData;
import com.ntews.intel.model.CCTVData;
import com.ntews.intel.model.IntelligenceReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
// Kafka imports disabled for local development
// import org.springframework.kafka.annotation.KafkaListener;
// import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.ParameterizedTypeReference;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class IntelligenceProcessingService {
    
    @Autowired
    private WebClient.Builder webClientBuilder;
    
    // Kafka disabled for local development
    // @Autowired
    // private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    private ThreatFusionService fusionService;
    
    @Autowired
    private ThreatScoringService scoringService;
    
    @Value("${ai-engine.base-url:http://localhost:8000}")
    private String aiEngineUrl;
    
    @Value("${intelligence.processing.batch-size:50}")
    private Integer batchSize;
    
    @Value("${intelligence.processing.timeout-seconds:60}")
    private Integer timeoutSeconds;
    
    // Kafka listeners disabled for local development
    // These methods can be called via REST endpoints for testing
    
    public void processSocialMediaData(SocialMediaData data) {
        try {
            log.debug("Processing social media data: {}", data.getId());
            
            // Convert to ThreatData
            ThreatData threatData = convertToThreatData(data);
            
            // Process threat data
            processThreatData(threatData);
            
        } catch (Exception e) {
            log.error("Error processing social media data: {}", data.getId(), e);
        }
    }
    
    public void processCCTVData(CCTVData data) {
        try {
            log.debug("Processing CCTV data: {}", data.getId());
            
            // Convert to ThreatData
            ThreatData threatData = convertToThreatData(data);
            
            // Process threat data
            processThreatData(threatData);
            
        } catch (Exception e) {
            log.error("Error processing CCTV data: {}", data.getId(), e);
        }
    }
    
    public void processCyberData(ThreatData data) {
        try {
            log.debug("Processing cyber threat data: {}", data.getId());
            
            // Process threat data
            processThreatData(data);
            
        } catch (Exception e) {
            log.error("Error processing cyber threat data: {}", data.getId(), e);
        }
    }
    
    private void processThreatData(ThreatData threatData) {
        try {
            // Send to AI Engine for analysis
            CompletableFuture<Map<String, Object>> aiAnalysis = analyzeWithAI(threatData);
            
            // Generate intelligence report
            IntelligenceReport report = generateReport(threatData, aiAnalysis.get());
            
            // Send report to alerts (Kafka disabled for local development)
            // kafkaTemplate.send("intelligence-reports", report);
            
            log.info("Generated intelligence report: {}", report.getId());
            log.info("Report details: {}", report.getSummary());
            
        } catch (Exception e) {
            log.error("Error processing threat data: {}", threatData.getId(), e);
        }
    }
    
    private CompletableFuture<Map<String, Object>> analyzeWithAI(ThreatData threatData) {
        WebClient webClient = webClientBuilder.build();
        
        return webClient.post()
                .uri(aiEngineUrl + "/analyze")
                .bodyValue(threatData)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .toFuture();
    }
    
    private IntelligenceReport generateReport(ThreatData threatData, Map<String, Object> aiAnalysis) {
        return IntelligenceReport.builder()
                .id(UUID.randomUUID().toString())
                .title("Threat Analysis Report")
                .summary(generateSummary(threatData, aiAnalysis))
                .threatLevel(determineThreatLevel(aiAnalysis))
                .category(determineThreatCategory(threatData))
                .threatScore(calculateThreatScore(aiAnalysis))
                .confidence((Double) aiAnalysis.getOrDefault("confidence", 0.5))
                .startTime(threatData.getTimestamp())
                .endTime(LocalDateTime.now())
                .location(buildLocationInfo(threatData))
                .status(IntelligenceReport.ReportStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .createdBy("intelligence-service")
                .verified(false)
                .build();
    }
    
    private String generateSummary(ThreatData threatData, Map<String, Object> aiAnalysis) {
        StringBuilder summary = new StringBuilder();
        summary.append("Threat detected from ");
        summary.append(threatData.getSourceType() != null ? threatData.getSourceType() : "unknown");
        summary.append(". Analysis indicates ");
        summary.append(aiAnalysis.getOrDefault("analysis", "potential threat"));
        return summary.toString();
    }
    
    private Double calculateThreatScore(Map<String, Object> aiAnalysis) {
        return (Double) aiAnalysis.getOrDefault("threat_score", 0.5);
    }
    
    private IntelligenceReport.ThreatLevel determineThreatLevel(Map<String, Object> aiAnalysis) {
        Double score = (Double) aiAnalysis.getOrDefault("threat_score", 0.5);
        if (score >= 0.8) return IntelligenceReport.ThreatLevel.CRITICAL;
        if (score >= 0.6) return IntelligenceReport.ThreatLevel.HIGH;
        if (score >= 0.4) return IntelligenceReport.ThreatLevel.MEDIUM;
        return IntelligenceReport.ThreatLevel.LOW;
    }
    
    private IntelligenceReport.ThreatCategory determineThreatCategory(ThreatData threatData) {
        if (threatData.getSourceType() != null) {
            switch (threatData.getSourceType().toLowerCase()) {
                case "social_media":
                    return IntelligenceReport.ThreatCategory.SOCIAL_UNREST;
                case "cctv":
                    return IntelligenceReport.ThreatCategory.PHYSICAL_SECURITY;
                case "cyber":
                    return IntelligenceReport.ThreatCategory.CYBER;
                default:
                    return IntelligenceReport.ThreatCategory.OTHER;
            }
        }
        return IntelligenceReport.ThreatCategory.OTHER;
    }
    
    private IntelligenceReport.LocationInfo buildLocationInfo(ThreatData threatData) {
        if (threatData.getLocation() != null) {
            IntelligenceReport.LocationInfo reportLocation = IntelligenceReport.LocationInfo.builder()
                    .latitude("0.0")  // Default values since location is String
                    .longitude("0.0")
                    .build();
            return reportLocation;
        }
        return IntelligenceReport.LocationInfo.builder()
                .latitude("0.0")
                .longitude("0.0")
                .build();
    }
    
    private ThreatData convertToThreatData(SocialMediaData socialMediaData) {
        return ThreatData.builder()
                .id(socialMediaData.getId())
                .sourceType("social_media")
                .content(socialMediaData.getContent())
                .timestamp(socialMediaData.getTimestamp() != null ? socialMediaData.getTimestamp() : LocalDateTime.now())
                .confidence(0.7)
                .build();
    }
    
    private ThreatData convertToThreatData(CCTVData cctvData) {
        return ThreatData.builder()
                .id(cctvData.getId())
                .sourceType("cctv")
                .content(cctvData.getVideoUrl() != null ? cctvData.getVideoUrl() : cctvData.getImageUrl())
                .timestamp(cctvData.getTimestamp() != null ? cctvData.getTimestamp() : LocalDateTime.now())
                .confidence(cctvData.getConfidence() != null ? cctvData.getConfidence() : 0.7)
                .build();
    }
}

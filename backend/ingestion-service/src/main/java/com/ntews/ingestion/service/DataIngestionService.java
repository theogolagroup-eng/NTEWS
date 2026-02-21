package com.ntews.ingestion.service;

import com.ntews.ingestion.model.ThreatData;
import com.ntews.ingestion.model.SocialMediaData;
import com.ntews.ingestion.model.CCTVData;
import com.ntews.ingestion.client.AIEngineClient;
import com.ntews.ingestion.client.AIEngineClient.ThreatAnalysis;
import com.ntews.ingestion.client.AIEngineClient.PredictionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@Service
@Slf4j
public class DataIngestionService {
    
    @Autowired
    private SocialMediaIngestionService socialMediaService;
    
    @Autowired
    private CCTVIngestionService cctvService;
    
    @Autowired
    private CyberFeedIngestionService cyberFeedService;
    
    @Autowired
    private AIEngineClient aiEngineClient;
    
    @Value("${ingestion.batch-size:100}")
    private Integer batchSize;
    
    @Value("${ingestion.processing-interval:5000}")
    private Long processingInterval;
    
    public void ingestSocialMediaData(SocialMediaData socialMediaData) {
        try {
            log.debug("Ingesting social media data from platform: {}", socialMediaData.getPlatform());
            
            // Convert to ThreatData format
            ThreatData threatData = convertToThreatData(socialMediaData);
            
            // REAL-TIME AI ANALYSIS
            try {
                // Analyze threat immediately
                ThreatAnalysis analysis = aiEngineClient.analyzeInRealTime(threatData);
                
                // Predict threat evolution
                PredictionResult prediction = aiEngineClient.predictThreatEvolution(threatData);
                
                // Enrich threat data with AI insights
                threatData.setAiThreatScore(analysis.getThreatScore());
                threatData.setAiRiskLevel(analysis.getRiskLevel());
                threatData.setAiConfidence(analysis.getConfidence());
                threatData.setAiKeywords(String.join(", ", analysis.getThreatKeywords()));
                threatData.setAiAnalysis(analysis.getAnalysis());
                
                // Add prediction data
                threatData.setPredictedSeverity(prediction.getPredictedSeverity());
                threatData.setTimeToCritical(prediction.getTimeToCritical());
                threatData.setEvolutionTrend(prediction.getEvolutionTrend());
                
                log.info("AI analysis completed for threat {}: score={}, risk_level={}, time_to_critical={}min", 
                    threatData.getId(), analysis.getThreatScore(), analysis.getRiskLevel(), prediction.getTimeToCritical());
                
                // If high-risk threat, trigger immediate alert
                if (aiEngineClient.isHighRiskThreat(threatData)) {
                    triggerProactiveAlert(threatData, analysis, prediction);
                }
                
            } catch (Exception aiError) {
                log.warn("AI analysis failed for threat {}, proceeding without AI enrichment: {}", 
                    threatData.getId(), aiError.getMessage());
                // Continue processing without AI data
            }
            
            // Log successful processing (replacing Kafka)
            log.info("Successfully processed social media data: {}", socialMediaData.getId());
            threatData.setStatus(ThreatData.ProcessingStatus.PROCESSED);
            
            // Log data for processing (replacing database storage)
            log.info("Threat data processed: ID={}, Type={}, RiskLevel={}", 
                threatData.getId(), threatData.getSourceType(), threatData.getAiRiskLevel());
            
        } catch (Exception e) {
            log.error("Error ingesting social media data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to ingest social media data", e);
        }
    }
    
    public void ingestCCTVData(CCTVData cctvData) {
        try {
            log.debug("Ingesting CCTV data from camera: {}", cctvData.getCameraId());
            
            // Convert to ThreatData format
            ThreatData threatData = convertToThreatData(cctvData);
            
            // Log successful processing (replacing Kafka)
            log.info("Successfully processed CCTV data: {}", cctvData.getId());
            threatData.setStatus(ThreatData.ProcessingStatus.PROCESSED);
            
            // Log data for processing (replacing database storage)
            log.info("CCTV threat data processed: ID={}, Camera={}, Location={}", 
                threatData.getId(), cctvData.getCameraId(), cctvData.getLocation());
            
        } catch (Exception e) {
            log.error("Error ingesting CCTV data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to ingest CCTV data", e);
        }
    }
    
    public void ingestCyberFeedData(ThreatData cyberData) {
        try {
            log.debug("Ingesting cyber feed data from source: {}", cyberData.getSource());
            
            // Set metadata for cyber data
            cyberData.setSourceType("cyber");
            cyberData.setContentType("log");
            cyberData.setIngestedAt(LocalDateTime.now());
            
            if (cyberData.getId() == null) {
                cyberData.setId(UUID.randomUUID().toString());
            }
            
            if (cyberData.getStatus() == null) {
                cyberData.setStatus(ThreatData.ProcessingStatus.PENDING);
            }
            
            // Log successful processing (replacing Kafka)
            log.info("Successfully processed cyber feed data: {}", cyberData.getId());
            cyberData.setStatus(ThreatData.ProcessingStatus.PROCESSED);
            
            // Log data for processing (replacing database storage)
            log.info("Cyber threat data processed: ID={}, Source={}, Type={}", 
                cyberData.getId(), cyberData.getSource(), cyberData.getContentType());
            
        } catch (Exception e) {
            log.error("Error ingesting cyber feed data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to ingest cyber feed data", e);
        }
    }
    
    private ThreatData convertToThreatData(SocialMediaData socialMediaData) {
        return ThreatData.builder()
            .id(socialMediaData.getId())
            .source(socialMediaData.getPlatform())
            .sourceType("social_media")
            .contentType("text")
            .rawContent(socialMediaData.getContent())
            .processedContent(socialMediaData.getContent())
            .location(ThreatData.LocationInfo.builder()
                .latitude(socialMediaData.getGeoTagLatitude())
                .longitude(socialMediaData.getGeoTagLongitude())
                .address(socialMediaData.getLocation())
                .confidence(0.8)
                .build())
            .timestamp(socialMediaData.getPostedAt())
            .ingestedAt(LocalDateTime.now())
            .status(ThreatData.ProcessingStatus.PENDING)
            .build();
    }
    
    private ThreatData convertToThreatData(CCTVData cctvData) {
        return ThreatData.builder()
            .id(cctvData.getId())
            .source(cctvData.getCameraId())
            .sourceType("cctv")
            .contentType("video")
            .rawContent(cctvData.getStreamUrl())
            .processedContent(cctvData.getStreamUrl())
            .location(ThreatData.LocationInfo.builder()
                .latitude(cctvData.getLatitude())
                .longitude(cctvData.getLongitude())
                .address(cctvData.getLocation())
                .confidence(0.9)
                .build())
            .timestamp(cctvData.getRecordedAt())
            .ingestedAt(LocalDateTime.now())
            .status(ThreatData.ProcessingStatus.PENDING)
            .build();
    }
    
    public CompletableFuture<Void> startBatchIngestion() {
        return CompletableFuture.runAsync(() -> {
            log.info("Starting batch data ingestion process");
            
            while (true) {
                try {
                    // Ingest social media data
                    socialMediaService.ingestFromAllPlatforms();
                    
                    // Ingest CCTV data
                    cctvService.ingestFromAllCameras();
                    
                    // Ingest cyber feed data
                    cyberFeedService.ingestFromAllSources();
                    
                    // Wait for next processing interval
                    Thread.sleep(processingInterval);
                    
                } catch (InterruptedException e) {
                    log.info("Batch ingestion interrupted");
                    break;
                } catch (Exception e) {
                    log.error("Error in batch ingestion: {}", e.getMessage(), e);
                    try {
                        Thread.sleep(10000); // Wait 10 seconds before retry
                    } catch (InterruptedException ie) {
                        break;
                    }
                }
            }
        });
    }
    
    private void triggerProactiveAlert(ThreatData threatData, ThreatAnalysis analysis, PredictionResult prediction) {
        try {
            log.warn("TRIGGERING PROACTIVE ALERT for high-risk threat: {}", threatData.getId());
            
            // Create proactive alert data
            Map<String, Object> alertData = new HashMap<>();
            alertData.put("id", UUID.randomUUID().toString());
            alertData.put("threatId", threatData.getId());
            alertData.put("title", "PROACTIVE: " + threatData.getSourceType().toUpperCase() + " Threat Detected");
            alertData.put("description", String.format("AI analysis detected high-risk threat with score %.2f. Predicted to become critical in %d minutes.", 
                analysis.getThreatScore(), prediction.getTimeToCritical()));
            alertData.put("severity", prediction.getPredictedSeverity());
            alertData.put("status", "active");
            alertData.put("priority", prediction.getTimeToCritical() < 60 ? "critical" : "high");
            alertData.put("category", threatData.getSourceType());
            alertData.put("location", threatData.getLocation());
            alertData.put("timestamp", LocalDateTime.now());
            alertData.put("confidence", analysis.getConfidence());
            alertData.put("threatLevel", analysis.getRiskLevel());
            alertData.put("aiAnalysis", analysis.getAnalysis());
            alertData.put("timeToCritical", prediction.getTimeToCritical());
            alertData.put("evolutionTrend", prediction.getEvolutionTrend());
            alertData.put("riskFactors", prediction.getRiskFactors());
            alertData.put("recommendedActions", generatePreventiveActions(threatData, analysis, prediction));
            alertData.put("source", "AI Engine - Ingestion Service");
            
            // Log proactive alert (replacing Kafka)
            log.info("Proactive alert generated for threat: {}", threatData.getId());
            log.info("Alert details: {}", alertData.toString());
                
        } catch (Exception e) {
            log.error("Error triggering proactive alert for threat: {}", threatData.getId(), e);
        }
    }
    
    private List<String> generatePreventiveActions(ThreatData threatData, ThreatAnalysis analysis, PredictionResult prediction) {
        List<String> actions = new ArrayList<>();
        
        // Base actions for all high-risk threats
        actions.add("Increase monitoring in the area");
        actions.add("Alert local authorities");
        
        // Specific actions based on threat type
        if ("social_media".equals(threatData.getSourceType())) {
            actions.add("Monitor social media trends for escalation");
            actions.add("Track related hashtags and accounts");
        } else if ("cctv".equals(threatData.getSourceType())) {
            actions.add("Review CCTV footage for suspicious activity");
            actions.add("Deploy additional cameras if needed");
        } else if ("cyber_feed".equals(threatData.getSourceType())) {
            actions.add("Enhance cybersecurity monitoring");
            actions.add("Check for related cyber threats");
        }
        
        // Time-critical actions
        if (prediction.getTimeToCritical() < 60) {
            actions.add("IMMEDIATE: Deploy response team");
            actions.add("IMMEDIATE: Evacuate if necessary");
        } else if (prediction.getTimeToCritical() < 360) {
            actions.add("Prepare contingency plans");
            actions.add("Stage resources nearby");
        }
        
        // Location-specific actions
        if (threatData.getLocation() != null && threatData.getLocation().getAddress() != null) {
            actions.add("Secure perimeter around: " + threatData.getLocation().getAddress());
        }
        
        return actions;
    }
}

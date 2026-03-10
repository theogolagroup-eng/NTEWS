package com.ntews.ingestion.controller;

import com.ntews.ingestion.model.ThreatData;
import com.ntews.ingestion.model.SocialMediaData;
import com.ntews.ingestion.model.CCTVData;
import com.ntews.ingestion.service.DataIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/ingestion")
@RequiredArgsConstructor
public class IngestionController {
    
    private static final Logger log = LoggerFactory.getLogger(IngestionController.class);
    
    private final DataIngestionService dataIngestionService;
    
    @PostMapping("/social-media")
    public ResponseEntity<Map<String, Object>> ingestSocialMedia(@RequestBody SocialMediaData data) {
        try {
            dataIngestionService.ingestSocialMediaData(data);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Social media data ingested successfully");
            response.put("dataId", data.getId());
            response.put("platform", data.getPlatform());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error ingesting social media data", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PostMapping("/cctv")
    public ResponseEntity<Map<String, Object>> ingestCCTV(@RequestBody CCTVData data) {
        try {
            dataIngestionService.ingestCCTVData(data);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "CCTV data ingested successfully");
            response.put("dataId", data.getId());
            response.put("cameraId", data.getCameraId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error ingesting CCTV data", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PostMapping("/cyber-feed")
    public ResponseEntity<Map<String, Object>> ingestCyberFeed(@RequestBody ThreatData data) {
        try {
            dataIngestionService.ingestCyberFeedData(data);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Cyber feed data ingested successfully");
            response.put("dataId", data.getId());
            response.put("source", data.getSource());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error ingesting cyber feed data", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PostMapping("/start-batch")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> startBatchIngestion() {
        return dataIngestionService.startBatchIngestion()
            .thenApply(v -> {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "Batch ingestion started");
                return ResponseEntity.ok(response);
            })
            .exceptionally(e -> {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "error");
                errorResponse.put("message", e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            });
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getIngestionStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "Data Ingestion Service");
        status.put("status", "running");
        status.put("timestamp", java.time.LocalDateTime.now());
        status.put("capabilities", new String[]{"social_media", "cctv", "cyber_feed", "batch_processing"});
        
        return ResponseEntity.ok(status);
    }
}

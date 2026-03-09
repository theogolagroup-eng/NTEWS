package com.ntews.intel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for receiving data from Bluesky ingestion service
 * Provides HTTP endpoint for WebSocket integration
 */
@RestController
@RequestMapping("/api/bluesky")
@RequiredArgsConstructor
@Slf4j
public class BlueskyRestController {

    private final ObjectMapper objectMapper;
    
    /**
     * Receive post data from ingestion service via REST
     */
    @PostMapping("/intelligence")
    public ResponseEntity<String> receiveBlueskyPost(@RequestBody Map<String, Object> postData) {
        try {
            log.info("📨 Received post from ingestion service: {}", postData.get("id"));
            
            // Process the post for intelligence analysis
            log.info("🧠 Processing post for intelligence analysis: {}", postData.get("id"));
            log.info("📝 Content: {}", postData.get("text"));
            log.info("🌍 Source: {}", postData.get("source"));
            log.info("📍 Location: {}", postData.get("location"));
            
            // Here you would integrate with your intelligence models
            // For now, just log the data that would be processed
            
            // Forward to WebSocket clients (dashboard)
            // This will be handled by existing WebSocket controller
            
            return ResponseEntity.ok("Post processed for intelligence analysis");
            
        } catch (Exception e) {
            log.error("❌ Error processing Bluesky post: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Intelligence Service is healthy and ready to receive Bluesky data");
    }
}

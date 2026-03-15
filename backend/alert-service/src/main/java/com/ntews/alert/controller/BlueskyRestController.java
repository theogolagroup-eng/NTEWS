package com.ntews.alert.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntews.alert.model.Alert;
import com.ntews.alert.model.UnifiedPost;
import com.ntews.alert.repository.ProcessedPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

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
    private final ProcessedPostRepository processedPostRepository;
    
    /**
     * Receive post data from ingestion service via REST
     */
    @PostMapping("/alert")
    public ResponseEntity<String> receiveBlueskyPost(@RequestBody Map<String, Object> postData) {
        try {
            log.info("📨 Received post from ingestion service: {}", postData.get("id"));
            
            // Convert post data to UnifiedPost with AI analysis
            UnifiedPost processedPost = UnifiedPost.builder()
                .id(String.valueOf(postData.get("id")))
                .source(String.valueOf(postData.get("source")))
                .author((String) postData.get("author"))
                .authorHandle((String) postData.get("author_handle"))
                .text((String) postData.get("text"))
                .cleanedText((String) postData.get("cleaned_text"))
                .timestamp(java.time.LocalDateTime.now())
                .url((String) postData.get("url"))
                .language((String) postData.get("language"))
                .hashtags(new ArrayList<>((java.util.List<String>) postData.getOrDefault("hashtags", new ArrayList<>())))
                .processedAt(java.time.LocalDateTime.now())
                .metadata(postData)
                .threatLevel((String) postData.get("threat_level"))
                .build();
            
            // Store processed post with AI analysis
            processedPostRepository.savePost(processedPost);
            
            // Create alert from post data
            Alert alert = Alert.builder()
                .id(String.valueOf(postData.get("id")))
                .title("Threat detected in Bluesky post")
                .description("Threat content: " + postData.get("text"))
                .category("SECURITY_THREAT")
                .source(String.valueOf(postData.get("source")))
                .severity(com.ntews.alert.model.Alert.Severity.MEDIUM)
                .location(null) // Simplified for now
                .timestamp(java.time.LocalDateTime.now())
                .metadata(postData)
                .build();
            
            // Process the alert (this would trigger existing alert logic)
            log.info("🚨 Created alert from Bluesky post: {}", alert.getId());
            
            // Forward to WebSocket clients (dashboard)
            // This will be handled by existing WebSocket controller
            
            return ResponseEntity.ok("Alert processed successfully");
            
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
        return ResponseEntity.ok("Alert Service is healthy and ready to receive Bluesky data");
    }
}

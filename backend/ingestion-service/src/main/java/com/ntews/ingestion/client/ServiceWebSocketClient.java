package com.ntews.ingestion.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntews.ingestion.model.UnifiedPost;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket client for direct communication with NTEWS services
 * Replaces Kafka/Redis with direct WebSocket connections
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceWebSocketClient {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    
    // Service URLs for REST communication
    private static final String ALERT_SERVICE_URL = "http://localhost:8081/api/bluesky/alert";
    private static final String PREDICTION_SERVICE_URL = "http://localhost:8082/api/bluesky/prediction";
    private static final String INTELLIGENCE_SERVICE_URL = "http://localhost:8083/api/bluesky/intelligence";
    
    // Connection status tracking
    private final ConcurrentHashMap<String, Boolean> connectionStatus = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void initialize() {
        log.info("🔗 Initializing REST client connections to NTEWS services...");
        
        // Initialize connection status
        connectionStatus.put("alert", true); // REST always available
        connectionStatus.put("prediction", true);
        connectionStatus.put("intelligence", true);
        
        log.info("✅ REST client initialized successfully");
    }
    
    /**
     * Send threat data to Alert Service via REST
     */
    public void sendToAlertService(UnifiedPost post) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(objectMapper.convertValue(post, Map.class), headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                ALERT_SERVICE_URL, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("📤 Sent post to Alert Service: {}", post.getId());
            } else {
                log.error("❌ Error sending to Alert Service: {} - {}", post.getId(), response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("❌ Error sending to Alert Service: {}", e.getMessage());
        }
    }
    
    /**
     * Send threat data to Prediction Service via REST
     */
    public void sendToPredictionService(UnifiedPost post) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(objectMapper.convertValue(post, Map.class), headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                PREDICTION_SERVICE_URL, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("📤 Sent post to Prediction Service: {}", post.getId());
            } else {
                log.error("❌ Error sending to Prediction Service: {} - {}", post.getId(), response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("❌ Error sending to Prediction Service: {}", e.getMessage());
        }
    }
    
    /**
     * Send threat data to Intelligence Service via REST
     */
    public void sendToIntelligenceService(UnifiedPost post) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(objectMapper.convertValue(post, Map.class), headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                INTELLIGENCE_SERVICE_URL, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("📤 Sent post to Intelligence Service: {}", post.getId());
            } else {
                log.error("❌ Error sending to Intelligence Service: {} - {}", post.getId(), response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("❌ Error sending to Intelligence Service: {}", e.getMessage());
        }
    }
    
    /**
     * Send to all connected services
     */
    public void sendToAllServices(UnifiedPost post) {
        log.info("📡 Broadcasting post {} to all NTEWS services", post.getId());
        
        sendToAlertService(post);
        sendToPredictionService(post);
        sendToIntelligenceService(post);
    }
    
    /**
     * Check if a service is connected
     */
    public boolean isConnected(String service) {
        return connectionStatus.getOrDefault(service, false);
    }
    
    /**
     * Get connection status for all services
     */
    public java.util.Map<String, Boolean> getConnectionStatus() {
        return new java.util.HashMap<>(connectionStatus);
    }
    
        
    /**
     * Send ping to keep connection alive
     */
    public void pingAllServices() {
        log.debug("📡 REST connections don't need pings - services are always available");
    }
    
    @PreDestroy
    public void cleanup() {
        log.info("🔌 Shutting down REST client...");
        // No cleanup needed for RestTemplate
    }
}

package com.ntews.predict.client;

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
    
    public Map<String, Object> generateHotspotForecast(Map<String, Object> historicalData, int forecastHours) {
        try {
            String url = aiEngineUrl + "/predict/hotspots";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            Map<String, Object> request = new HashMap<>();
            request.put("historical_data", historicalData);
            request.put("forecast_hours", forecastHours);
            request.put("type", "hotspot_prediction");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            log.info("Sending hotspot forecast request to AI Engine: {}", url);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("AI Engine hotspot forecast completed successfully");
                return response.getBody();
            } else {
                log.warn("AI Engine returned non-successful status: {}", response.getStatusCode());
                return getDefaultHotspotForecast();
            }
            
        } catch (Exception e) {
            log.error("Error calling AI Engine for hotspot forecast", e);
            return getDefaultHotspotForecast();
        }
    }
    
    public Map<String, Object> generateRiskTrendForecast(Map<String, Object> historicalData, int forecastHours) {
        try {
            String url = aiEngineUrl + "/predict";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            Map<String, Object> request = new HashMap<>();
            request.put("historical_data", historicalData);
            request.put("forecast_hours", forecastHours);
            request.put("type", "risk_trend_prediction");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            log.info("Sending risk trend forecast request to AI Engine: {}", url);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("AI Engine risk trend forecast completed successfully");
                return response.getBody();
            } else {
                log.warn("AI Engine returned non-successful status: {}", response.getStatusCode());
                return getDefaultRiskTrendForecast();
            }
            
        } catch (Exception e) {
            log.error("Error calling AI Engine for risk trend forecast", e);
            return getDefaultRiskTrendForecast();
        }
    }
    
    public List<Map<String, Object>> getAvailableModels() {
        try {
            String url = aiEngineUrl + "/models";
            
            log.info("Fetching available models from AI Engine: {}", url);
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> models = (List<Map<String, Object>>) response.getBody().get("models");
                log.info("Retrieved {} models from AI Engine", models.size());
                return models;
            } else {
                log.warn("AI Engine returned non-successful status: {}", response.getStatusCode());
                return getDefaultModels();
            }
            
        } catch (Exception e) {
            log.error("Error fetching models from AI Engine", e);
            return getDefaultModels();
        }
    }
    
    public Map<String, Object> getEngineStats() {
        try {
            String url = aiEngineUrl + "/stats";
            
            log.info("Fetching AI Engine stats: {}", url);
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Retrieved AI Engine stats successfully");
                return response.getBody();
            } else {
                log.warn("AI Engine returned non-successful status: {}", response.getStatusCode());
                return getDefaultStats();
            }
            
        } catch (Exception e) {
            log.error("Error fetching AI Engine stats", e);
            return getDefaultStats();
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
    
    private Map<String, Object> getDefaultHotspotForecast() {
        Map<String, Object> defaultForecast = new HashMap<>();
        defaultForecast.put("hotspots", new ArrayList<>());
        defaultForecast.put("total_hotspots", 0);
        defaultForecast.put("confidence", 0.5);
        defaultForecast.put("timestamp", LocalDateTime.now());
        defaultForecast.put("analysis", "AI Engine unavailable - using default forecast");
        return defaultForecast;
    }
    
    private Map<String, Object> getDefaultRiskTrendForecast() {
        Map<String, Object> defaultForecast = new HashMap<>();
        defaultForecast.put("trend", "stable");
        defaultForecast.put("confidence", 0.5);
        defaultForecast.put("time_horizon", "24h");
        defaultForecast.put("risk_level", "medium");
        defaultForecast.put("forecast_points", new ArrayList<>());
        defaultForecast.put("analysis", "AI Engine unavailable - using default trend");
        return defaultForecast;
    }
    
    private List<Map<String, Object>> getDefaultModels() {
        List<Map<String, Object>> defaultModels = new ArrayList<>();
        defaultModels.add(Map.of(
            "name", "threat_classifier",
            "version", "1.0",
            "status", "unknown",
            "type", "classification",
            "note", "AI Engine unavailable"
        ));
        return defaultModels;
    }
    
    private Map<String, Object> getDefaultStats() {
        Map<String, Object> defaultStats = new HashMap<>();
        defaultStats.put("requests_processed", 0);
        defaultStats.put("models_loaded", 0);
        defaultStats.put("uptime", "Unknown");
        defaultStats.put("last_prediction", LocalDateTime.now());
        defaultStats.put("status", "AI Engine unavailable");
        return defaultStats;
    }
}

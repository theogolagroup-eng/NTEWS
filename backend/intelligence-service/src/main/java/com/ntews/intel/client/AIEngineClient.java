package com.ntews.intel.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.Map;
import java.util.HashMap;

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
    
    public Map<String, Object> analyzeThreat(String threatData) {
        try {
            String url = aiEngineUrl + "/api/analyze";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            Map<String, Object> request = new HashMap<>();
            request.put("data", threatData);
            request.put("type", "threat_analysis");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            log.info("Sending threat analysis request to AI Engine: {}", url);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("AI Engine analysis completed successfully");
                return response.getBody();
            } else {
                log.warn("AI Engine returned non-successful status: {}", response.getStatusCode());
                return getDefaultAnalysis();
            }
            
        } catch (Exception e) {
            log.error("Error calling AI Engine for threat analysis", e);
            return getDefaultAnalysis();
        }
    }
    
    public Map<String, Object> generateForecast(String historicalData) {
        try {
            String url = aiEngineUrl + "/api/forecast";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            Map<String, Object> request = new HashMap<>();
            request.put("data", historicalData);
            request.put("type", "threat_forecast");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            log.info("Sending forecast request to AI Engine: {}", url);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("AI Engine forecast completed successfully");
                return response.getBody();
            } else {
                log.warn("AI Engine returned non-successful status: {}", response.getStatusCode());
                return getDefaultForecast();
            }
            
        } catch (Exception e) {
            log.error("Error calling AI Engine for forecast", e);
            return getDefaultForecast();
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
    
    private Map<String, Object> getDefaultAnalysis() {
        Map<String, Object> defaultAnalysis = new HashMap<>();
        defaultAnalysis.put("threat_level", "medium");
        defaultAnalysis.put("confidence", 0.5);
        defaultAnalysis.put("risk_score", 50);
        defaultAnalysis.put("recommendations", new String[]{"Monitor situation", "Gather more intelligence"});
        defaultAnalysis.put("key_factors", new String[]{"Limited data available"});
        return defaultAnalysis;
    }
    
    private Map<String, Object> getDefaultForecast() {
        Map<String, Object> defaultForecast = new HashMap<>();
        defaultForecast.put("trend", "stable");
        defaultForecast.put("confidence", 0.5);
        defaultForecast.put("time_horizon", "24h");
        defaultForecast.put("risk_level", "medium");
        defaultForecast.put("hotspots", new Object[]{});
        return defaultForecast;
    }
}

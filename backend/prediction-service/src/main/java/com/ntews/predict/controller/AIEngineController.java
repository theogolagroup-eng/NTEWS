package com.ntews.predict.controller;

import com.ntews.predict.client.AIEngineClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/ai-engine")
@RequiredArgsConstructor
@Slf4j
public class AIEngineController {
    
    private final AIEngineClient aiEngineClient;
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        boolean isHealthy = aiEngineClient.isHealthy();
        
        response.put("status", isHealthy ? "healthy" : "unhealthy");
        response.put("timestamp", java.time.LocalDateTime.now());
        response.put("service", "AI Engine Health Check");
        
        if (isHealthy) {
            response.put("message", "AI Engine is responding normally");
        } else {
            response.put("message", "AI Engine is not responding");
            response.put("troubleshooting", "Check if AI Engine is running on port 8000");
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/models")
    public ResponseEntity<Map<String, Object>> getModels() {
        try {
            var models = aiEngineClient.getAvailableModels();
            
            Map<String, Object> response = new HashMap<>();
            response.put("models", models);
            response.put("total_models", models.size());
            response.put("timestamp", java.time.LocalDateTime.now());
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching AI models", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Failed to fetch AI models");
            response.put("message", e.getMessage());
            response.put("timestamp", java.time.LocalDateTime.now());
            response.put("status", "error");
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getEngineStats() {
        try {
            var stats = aiEngineClient.getEngineStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("engine_stats", stats);
            response.put("timestamp", java.time.LocalDateTime.now());
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching AI engine stats", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Failed to fetch AI engine stats");
            response.put("message", e.getMessage());
            response.put("timestamp", java.time.LocalDateTime.now());
            response.put("status", "error");
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/capabilities")
    public ResponseEntity<Map<String, Object>> getCapabilities() {
        Map<String, Object> response = new HashMap<>();
        
        // AI Engine capabilities
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("threat_classification", true);
        capabilities.put("risk_prediction", true);
        capabilities.put("hotspot_detection", true);
        capabilities.put("sentiment_analysis", true);
        capabilities.put("real_time_analysis", true);
        capabilities.put("batch_processing", false); // Not implemented yet
        
        // Model information
        Map<String, Object> modelInfo = new HashMap<>();
        modelInfo.put("total_models", 4);
        modelInfo.put("trained_models", 7);
        modelInfo.put("best_accuracy", 0.891); // Logistic Regression
        modelInfo.put("model_types", new String[]{"classification", "prediction", "geospatial", "nlp"});
        
        response.put("capabilities", capabilities);
        response.put("model_info", modelInfo);
        response.put("prediction_type", "mixed"); // Both reactive and limited predictive
        response.put("effectiveness", "medium"); // Current assessment
        response.put("timestamp", java.time.LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/prediction-analysis")
    public ResponseEntity<Map<String, Object>> getPredictionAnalysis() {
        Map<String, Object> response = new HashMap<>();
        
        // Current prediction capabilities analysis
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("current_state", "Mostly Reactive");
        Map<String, Object> threatClassification = new HashMap<>();
        threatClassification.put("status", "WORKING");
        threatClassification.put("accuracy", "89%");
        threatClassification.put("type", "Classification (not prediction)");
        threatClassification.put("effectiveness", "High for existing threats");
        analysis.put("threat_classification", threatClassification);
        Map<String, Object> riskTrends = new HashMap<>();
        riskTrends.put("status", "MOCK IMPLEMENTATION");
        riskTrends.put("accuracy", "N/A - Random values");
        riskTrends.put("type", "Should be time-series prediction");
        riskTrends.put("effectiveness", "Low - needs real implementation");
        analysis.put("risk_trends", riskTrends);
        Map<String, Object> hotspotPrediction = new HashMap<>();
        hotspotPrediction.put("status", "MOCK IMPLEMENTATION");
        hotspotPrediction.put("accuracy", "N/A - Random values");
        hotspotPrediction.put("type", "Should be geospatial prediction");
        hotspotPrediction.put("effectiveness", "Low - needs real implementation");
        analysis.put("hotspot_prediction", hotspotPrediction);
        
        response.put("analysis", analysis);
        response.put("overall_assessment", "System has good classification but lacks true predictive capabilities");
        response.put("recommendations", new String[]{
            "Implement real model inference instead of mock predictions",
            "Add time series analysis for trend prediction", 
            "Enable historical pattern learning",
            "Integrate AI Engine with ingestion service for real-time analysis",
            "Add continuous learning and model retraining"
        });
        response.put("timestamp", java.time.LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
}

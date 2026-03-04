package com.ntews.prediction.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {
    
    @GetMapping("/health")
    public String health() {
        return "Prediction Service is running!";
    }
    
    @GetMapping("/dashboard/summary")
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalPredictions", 150);
        summary.put("highRiskAreas", 5);
        summary.put("averageConfidence", 0.85);
        summary.put("lastUpdated", "2026-03-04T12:00:00Z");
        return summary;
    }
}

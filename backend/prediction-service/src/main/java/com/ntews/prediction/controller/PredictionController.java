package com.ntews.prediction.controller;

import com.ntews.prediction.model.Prediction;
import com.ntews.prediction.model.LocationRisk;
import com.ntews.prediction.model.RiskTrend;
import com.ntews.prediction.model.DashboardSummary;
import com.ntews.prediction.model.VerificationRequest;
import com.ntews.prediction.service.PredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
@Slf4j
public class PredictionController {
    
    private final PredictionService predictionService;
    
    @GetMapping
    public ResponseEntity<List<Prediction>> getPredictions(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "24") int hours,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        List<Prediction> predictions = predictionService.getPredictions(
            type, category, location, severity, from, to, hours, page, size);
        
        return ResponseEntity.ok(predictions);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Prediction> getPrediction(@PathVariable String id) {
        return predictionService.getPrediction(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/dashboard/summary")
    public ResponseEntity<DashboardSummary> getDashboardSummary() {
        DashboardSummary summary = predictionService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/forecasts")
    public ResponseEntity<List<Prediction>> getForecasts(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "72") int hours) {
        
        List<Prediction> forecasts = predictionService.getForecasts(type, hours);
        return ResponseEntity.ok(forecasts);
    }
    
    @GetMapping("/location-risk")
    public ResponseEntity<List<LocationRisk>> getLocationRisk(
            @RequestParam(required = false) String latitude,
            @RequestParam(required = false) String longitude,
            @RequestParam(defaultValue = "50") double radiusKm) {
        
        List<LocationRisk> risks = predictionService.getLocationRisk(latitude, longitude, radiusKm);
        return ResponseEntity.ok(risks);
    }
    
    @GetMapping("/risk-trends")
    public ResponseEntity<List<RiskTrend>> getRiskTrends(
            @RequestParam(defaultValue = "168") int hours) { // default 7 days
        
        List<RiskTrend> trends = predictionService.getRiskTrends(hours);
        return ResponseEntity.ok(trends);
    }
    
    @PostMapping
    public ResponseEntity<Prediction> createPrediction(@RequestBody Prediction prediction) {
        Prediction created = predictionService.createPrediction(prediction);
        return ResponseEntity.ok(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Prediction> updatePrediction(
            @PathVariable String id, @RequestBody Prediction prediction) {
        
        prediction.setId(id);
        Prediction updated = predictionService.updatePrediction(prediction);
        return ResponseEntity.ok(updated);
    }
    
    @PostMapping("/{id}/verify")
    public ResponseEntity<Prediction> verifyPrediction(
            @PathVariable String id, @RequestBody VerificationRequest request) {
        
        Prediction verified = predictionService.verifyPrediction(id, request.isVerified(), request.getNotes());
        return ResponseEntity.ok(verified);
    }
    
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Prediction> cancelPrediction(@PathVariable String id) {
        Prediction cancelled = predictionService.cancelPrediction(id);
        return ResponseEntity.ok(cancelled);
    }
}

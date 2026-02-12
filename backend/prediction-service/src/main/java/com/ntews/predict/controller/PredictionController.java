package com.ntews.predict.controller;

import com.ntews.predict.model.RiskForecast;
import com.ntews.predict.model.HotspotData;
import com.ntews.predict.service.PredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
@Slf4j
public class PredictionController {
    
    private final PredictionService predictionService;
    
    @GetMapping("/forecasts")
    public ResponseEntity<List<RiskForecast>> getRiskForecasts(
            @RequestParam(required = false) String forecastType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime validFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime validTo) {
        
        List<RiskForecast> forecasts = predictionService.getRiskForecasts(forecastType, validFrom, validTo);
        return ResponseEntity.ok(forecasts);
    }
    
    @GetMapping("/forecasts/{id}")
    public ResponseEntity<RiskForecast> getRiskForecast(@PathVariable String id) {
        RiskForecast forecast = predictionService.getRiskForecast(id);
        if (forecast != null) {
            return ResponseEntity.ok(forecast);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/forecasts/current")
    public ResponseEntity<RiskForecast> getCurrentForecast() {
        RiskForecast currentForecast = predictionService.getCurrentForecast();
        return ResponseEntity.ok(currentForecast);
    }
    
    @GetMapping("/hotspots")
    public ResponseEntity<List<HotspotData>> getCurrentHotspots() {
        List<HotspotData> hotspots = predictionService.getCurrentHotspots();
        return ResponseEntity.ok(hotspots);
    }
    
    @GetMapping("/hotspots/{id}")
    public ResponseEntity<HotspotDetail> getHotspotDetail(@PathVariable UUID id) {
        HotspotDetail detail = predictionService.getHotspotDetail(id.toString());
        return ResponseEntity.ok(detail);
    }
    
    @GetMapping("/risk-trends")
    public ResponseEntity<List<RiskTrendData>> getRiskTrends(
            @RequestParam(defaultValue = "24") int hours) {
        
        List<RiskTrendData> trends = predictionService.getRiskTrends(hours);
        return ResponseEntity.ok(trends);
    }
    
    @GetMapping("/dashboard/summary")
    public ResponseEntity<PredictionDashboardSummary> getDashboardSummary() {
        PredictionDashboardSummary summary = predictionService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/location-risk")
    public ResponseEntity<List<LocationRiskData>> getLocationRisks(
            @RequestParam(required = false) String latitude,
            @RequestParam(required = false) String longitude,
            @RequestParam(defaultValue = "10") double radiusKm) {
        
        List<LocationRiskData> locationRisks = predictionService.getLocationRisks(latitude, longitude, radiusKm);
        return ResponseEntity.ok(locationRisks);
    }
    
    @PostMapping("/generate-forecast")
    public ResponseEntity<RiskForecast> generateForecast(@RequestBody ForecastRequest request) {
        RiskForecast forecast = predictionService.generateForecast(request.getForecastType(), request.getParameters());
        return ResponseEntity.ok(forecast);
    }
    
    // DTOs
    public static class ForecastRequest {
        private String forecastType;
        private Map<String, Object> parameters;
        
        public String getForecastType() { return forecastType; }
        public void setForecastType(String forecastType) { this.forecastType = forecastType; }
        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
    }
    
    public static class HotspotData {
        private String id;
        private String locationName;
        private String latitude;
        private String longitude;
        private double probability;
        private String severity;
        private String threatType;
        private LocalDateTime peakTime;
        private double radius;
        
        public HotspotData(String id, String locationName, String latitude, String longitude,
                          double probability, String severity, String threatType, 
                          LocalDateTime peakTime, double radius) {
            this.id = id;
            this.locationName = locationName;
            this.latitude = latitude;
            this.longitude = longitude;
            this.probability = probability;
            this.severity = severity;
            this.threatType = threatType;
            this.peakTime = peakTime;
            this.radius = radius;
        }
        
        // Getters
        public String getId() { return id; }
        public String getLocationName() { return locationName; }
        public String getLatitude() { return latitude; }
        public String getLongitude() { return longitude; }
        public double getProbability() { return probability; }
        public String getSeverity() { return severity; }
        public String getThreatType() { return threatType; }
        public LocalDateTime getPeakTime() { return peakTime; }
        public double getRadius() { return radius; }
    }
    
    public static class HotspotDetail {
        private String id;
        private String locationName;
        private String latitude;
        private String longitude;
        private double probability;
        private String severity;
        private String threatType;
        private LocalDateTime peakTime;
        private double radius;
        private List<String> contributingFactors;
        private double confidence;
        private List<RiskForecast.ForecastPoint> forecastPoints;
        
        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getLocationName() { return locationName; }
        public void setLocationName(String locationName) { this.locationName = locationName; }
        public String getLatitude() { return latitude; }
        public void setLatitude(String latitude) { this.latitude = latitude; }
        public String getLongitude() { return longitude; }
        public void setLongitude(String longitude) { this.longitude = longitude; }
        public double getProbability() { return probability; }
        public void setProbability(double probability) { this.probability = probability; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public String getThreatType() { return threatType; }
        public void setThreatType(String threatType) { this.threatType = threatType; }
        public LocalDateTime getPeakTime() { return peakTime; }
        public void setPeakTime(LocalDateTime peakTime) { this.peakTime = peakTime; }
        public double getRadius() { return radius; }
        public void setRadius(double radius) { this.radius = radius; }
        public List<String> getContributingFactors() { return contributingFactors; }
        public void setContributingFactors(List<String> contributingFactors) { this.contributingFactors = contributingFactors; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        public List<RiskForecast.ForecastPoint> getForecastPoints() { return forecastPoints; }
        public void setForecastPoints(List<RiskForecast.ForecastPoint> forecastPoints) { this.forecastPoints = forecastPoints; }
    }
    
    public static class RiskTrendData {
        private String timestamp;
        private double riskScore;
        private String riskLevel;
        private double confidence;
        
        public RiskTrendData(String timestamp, double riskScore, String riskLevel, double confidence) {
            this.timestamp = timestamp;
            this.riskScore = riskScore;
            this.riskLevel = riskLevel;
            this.confidence = confidence;
        }
        
        // Getters
        public String getTimestamp() { return timestamp; }
        public double getRiskScore() { return riskScore; }
        public String getRiskLevel() { return riskLevel; }
        public double getConfidence() { return confidence; }
    }
    
    public static class PredictionDashboardSummary {
        private int activeHotspots;
        private int highRiskHotspots;
        private int mediumRiskHotspots;
        private int lowRiskHotspots;
        private double currentRiskTrend;
        private String trendDirection;
        private List<HotspotSummary> topHotspots;
        private List<RiskTrendData> recentTrends;
        
        // Getters and setters
        public int getActiveHotspots() { return activeHotspots; }
        public void setActiveHotspots(int activeHotspots) { this.activeHotspots = activeHotspots; }
        public int getHighRiskHotspots() { return highRiskHotspots; }
        public void setHighRiskHotspots(int highRiskHotspots) { this.highRiskHotspots = highRiskHotspots; }
        public int getMediumRiskHotspots() { return mediumRiskHotspots; }
        public void setMediumRiskHotspots(int mediumRiskHotspots) { this.mediumRiskHotspots = mediumRiskHotspots; }
        public int getLowRiskHotspots() { return lowRiskHotspots; }
        public void setLowRiskHotspots(int lowRiskHotspots) { this.lowRiskHotspots = lowRiskHotspots; }
        public double getCurrentRiskTrend() { return currentRiskTrend; }
        public void setCurrentRiskTrend(double currentRiskTrend) { this.currentRiskTrend = currentRiskTrend; }
        public String getTrendDirection() { return trendDirection; }
        public void setTrendDirection(String trendDirection) { this.trendDirection = trendDirection; }
        public List<HotspotSummary> getTopHotspots() { return topHotspots; }
        public void setTopHotspots(List<HotspotSummary> topHotspots) { this.topHotspots = topHotspots; }
        public List<RiskTrendData> getRecentTrends() { return recentTrends; }
        public void setRecentTrends(List<RiskTrendData> recentTrends) { this.recentTrends = recentTrends; }
    }
    
    public static class HotspotSummary {
        private String id;
        private String locationName;
        private double probability;
        private String severity;
        private String threatType;
        
        public HotspotSummary(String id, String locationName, double probability, String severity, String threatType) {
            this.id = id;
            this.locationName = locationName;
            this.probability = probability;
            this.severity = severity;
            this.threatType = threatType;
        }
        
        // Getters
        public String getId() { return id; }
        public String getLocationName() { return locationName; }
        public double getProbability() { return probability; }
        public String getSeverity() { return severity; }
        public String getThreatType() { return threatType; }
    }
    
    public static class LocationRiskData {
        private String locationId;
        private String locationName;
        private String latitude;
        private String longitude;
        private double currentRisk;
        private double predictedRisk;
        private double riskChange;
        private String trendDirection;
        private double confidence;
        
        public LocationRiskData(String locationId, String locationName, String latitude, String longitude,
                               double currentRisk, double predictedRisk, double riskChange,
                               String trendDirection, double confidence) {
            this.locationId = locationId;
            this.locationName = locationName;
            this.latitude = latitude;
            this.longitude = longitude;
            this.currentRisk = currentRisk;
            this.predictedRisk = predictedRisk;
            this.riskChange = riskChange;
            this.trendDirection = trendDirection;
            this.confidence = confidence;
        }
        
        // Getters
        public String getLocationId() { return locationId; }
        public String getLocationName() { return locationName; }
        public String getLatitude() { return latitude; }
        public String getLongitude() { return longitude; }
        public double getCurrentRisk() { return currentRisk; }
        public double getPredictedRisk() { return predictedRisk; }
        public double getRiskChange() { return riskChange; }
        public String getTrendDirection() { return trendDirection; }
        public double getConfidence() { return confidence; }
    }
}

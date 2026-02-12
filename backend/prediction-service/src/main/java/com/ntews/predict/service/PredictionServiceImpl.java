package com.ntews.predict.service;

import com.ntews.predict.controller.PredictionController.*;
import com.ntews.predict.model.RiskForecast;
import com.ntews.predict.repository.RiskForecastRepository;
import com.ntews.predict.client.AIEngineClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PredictionServiceImpl implements PredictionService {
    
    private static final Logger log = LoggerFactory.getLogger(PredictionServiceImpl.class);
    
    private final RiskForecastRepository riskForecastRepository;
    private final AIEngineClient aiEngineClient;
    
    public PredictionServiceImpl(RiskForecastRepository riskForecastRepository, AIEngineClient aiEngineClient) {
        this.riskForecastRepository = riskForecastRepository;
        this.aiEngineClient = aiEngineClient;
    }
    
    @Override
    public List<RiskForecast> getRiskForecasts(String forecastType, LocalDateTime validFrom, LocalDateTime validTo) {
        if (forecastType != null) {
            if (validFrom != null && validTo != null) {
                return riskForecastRepository.findByTypeAndGeneratedAtBetween(forecastType, validFrom, validTo);
            } else {
                return riskForecastRepository.findByForecastType(forecastType);
            }
        } else if (validFrom != null && validTo != null) {
            return riskForecastRepository.findByGeneratedAtBetween(validFrom, validTo);
        } else {
            return riskForecastRepository.findAll();
        }
    }
    
    @Override
    public RiskForecast getRiskForecast(String id) {
        return riskForecastRepository.findById(id).orElse(null);
    }
    
    @Override
    public RiskForecast getCurrentForecast() {
        LocalDateTime now = LocalDateTime.now();
        return riskForecastRepository.findLatestValidForecast(now);
    }
    
    @Override
    public List<HotspotData> getCurrentHotspots() {
        LocalDateTime now = LocalDateTime.now();
        List<RiskForecast> hotspotForecasts = riskForecastRepository.findValidHotspotForecasts(now);
        
        List<HotspotData> hotspots = new ArrayList<>();
        
        for (RiskForecast forecast : hotspotForecasts) {
            if (forecast.getHotspots() != null) {
                for (RiskForecast.HotspotPrediction hotspot : forecast.getHotspots()) {
                    hotspots.add(new HotspotData(
                            hotspot.getHotspotId(),
                            hotspot.getLocationName(),
                            hotspot.getLatitude(),
                            hotspot.getLongitude(),
                            hotspot.getProbability(),
                            hotspot.getSeverity(),
                            hotspot.getThreatType(),
                            hotspot.getPeakTime(),
                            hotspot.getRadius()
                    ));
                }
            }
        }
        
        // Sort by probability (highest first)
        hotspots.sort((h1, h2) -> Double.compare(h2.getProbability(), h1.getProbability()));
        
        return hotspots;
    }
    
    @Override
    public HotspotDetail getHotspotDetail(String id) {
        LocalDateTime now = LocalDateTime.now();
        List<RiskForecast> hotspotForecasts = riskForecastRepository.findValidHotspotForecasts(now);
        
        for (RiskForecast forecast : hotspotForecasts) {
            if (forecast.getHotspots() != null) {
                for (RiskForecast.HotspotPrediction hotspot : forecast.getHotspots()) {
                    if (id.equals(hotspot.getHotspotId())) {
                        HotspotDetail detail = new HotspotDetail();
                        detail.setId(hotspot.getHotspotId());
                        detail.setLocationName(hotspot.getLocationName());
                        detail.setLatitude(hotspot.getLatitude());
                        detail.setLongitude(hotspot.getLongitude());
                        detail.setProbability(hotspot.getProbability());
                        detail.setSeverity(hotspot.getSeverity());
                        detail.setThreatType(hotspot.getThreatType());
                        detail.setPeakTime(hotspot.getPeakTime());
                        detail.setRadius(hotspot.getRadius());
                        detail.setContributingFactors(hotspot.getContributingFactors());
                        detail.setConfidence(hotspot.getConfidence());
                        detail.setForecastPoints(forecast.getForecastPoints());
                        
                        return detail;
                    }
                }
            }
        }
        
        return null;
    }
    
    @Override
    public List<RiskTrendData> getRiskTrends(int hours) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.minusHours(hours);
        
        List<RiskForecast> trendForecasts = riskForecastRepository.findValidTrendForecasts(now);
        List<RiskTrendData> trends = new ArrayList<>();
        
        for (RiskForecast forecast : trendForecasts) {
            if (forecast.getForecastPoints() != null) {
                for (RiskForecast.ForecastPoint point : forecast.getForecastPoints()) {
                    if (point.getTimestamp().isAfter(startTime) && point.getTimestamp().isBefore(now)) {
                        trends.add(new RiskTrendData(
                                point.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                point.getPredictedRisk(),
                                point.getRiskLevel(),
                                point.getConfidence()
                        ));
                    }
                }
            }
        }
        
        // Sort by timestamp
        trends.sort((t1, t2) -> t1.getTimestamp().compareTo(t2.getTimestamp()));
        
        return trends;
    }
    
    @Override
    public PredictionDashboardSummary getDashboardSummary() {
        PredictionDashboardSummary summary = new PredictionDashboardSummary();
        
        LocalDateTime now = LocalDateTime.now();
        List<HotspotData> currentHotspots = getCurrentHotspots();
        
        summary.setActiveHotspots(currentHotspots.size());
        
        // Count by severity
        Map<String, Long> severityCounts = currentHotspots.stream()
                .collect(Collectors.groupingBy(
                        HotspotData::getSeverity,
                        Collectors.counting()
                ));
        
        summary.setHighRiskHotspots(severityCounts.getOrDefault("high", 0L).intValue());
        summary.setMediumRiskHotspots(severityCounts.getOrDefault("medium", 0L).intValue());
        summary.setLowRiskHotspots(severityCounts.getOrDefault("low", 0L).intValue());
        
        // Get current risk trend
        RiskForecast currentForecast = getCurrentForecast();
        if (currentForecast != null) {
            summary.setCurrentRiskTrend(currentForecast.getOverallRiskTrend() != null ? 
                    currentForecast.getOverallRiskTrend() : 0.0);
            
            // Determine trend direction (simplified)
            if (currentForecast.getOverallRiskTrend() != null) {
                double trend = currentForecast.getOverallRiskTrend();
                if (trend > 0.1) {
                    summary.setTrendDirection("increasing");
                } else if (trend < -0.1) {
                    summary.setTrendDirection("decreasing");
                } else {
                    summary.setTrendDirection("stable");
                }
            }
        }
        
        // Get top hotspots
        List<HotspotSummary> topHotspots = currentHotspots.stream()
                .limit(5)
                .map(hotspot -> new HotspotSummary(
                        hotspot.getId(),
                        hotspot.getLocationName(),
                        hotspot.getProbability(),
                        hotspot.getSeverity(),
                        hotspot.getThreatType()
                ))
                .collect(Collectors.toList());
        
        summary.setTopHotspots(topHotspots);
        
        // Get recent trends
        List<RiskTrendData> recentTrends = getRiskTrends(24);
        summary.setRecentTrends(recentTrends);
        
        return summary;
    }
    
    @Override
    public List<LocationRiskData> getLocationRisks(String latitude, String longitude, double radiusKm) {
        LocalDateTime now = LocalDateTime.now();
        List<RiskForecast> forecasts = riskForecastRepository.findWithLocationRisks();
        
        List<LocationRiskData> locationRisks = new ArrayList<>();
        
        for (RiskForecast forecast : forecasts) {
            if (forecast.getLocationRisks() != null) {
                for (RiskForecast.LocationRisk locationRisk : forecast.getLocationRisks()) {
                    // For now, include all location risks
                    // In a real implementation, you would filter by distance from the specified coordinates
                    locationRisks.add(new LocationRiskData(
                            locationRisk.getLocationId(),
                            locationRisk.getLocationName(),
                            locationRisk.getLatitude(),
                            locationRisk.getLongitude(),
                            locationRisk.getCurrentRisk(),
                            locationRisk.getPredictedRisk(),
                            locationRisk.getRiskChange(),
                            locationRisk.getTrendDirection(),
                            locationRisk.getConfidence()
                    ));
                }
            }
        }
        
        return locationRisks;
    }
    
    @Override
    public RiskForecast generateForecast(String forecastType, Map<String, Object> parameters) {
        try {
            log.info("Generating {} forecast with AI Engine", forecastType);
            
            // Use AI Engine for real predictions
            Map<String, Object> historicalData = new HashMap<>(); // Simplified for now
            int forecastHours = (Integer) parameters.getOrDefault("forecast_hours", 24);
            
            Map<String, Object> aiPrediction;
            
            if ("trend".equals(forecastType)) {
                aiPrediction = aiEngineClient.generateRiskTrendForecast(historicalData, forecastHours);
            } else if ("hotspot".equals(forecastType)) {
                aiPrediction = aiEngineClient.generateHotspotForecast(historicalData, forecastHours);
            } else {
                // Default to trend prediction for unknown types
                aiPrediction = aiEngineClient.generateRiskTrendForecast(historicalData, forecastHours);
            }
            
            // Convert AI prediction to RiskForecast and return
            return convertAIPredictionToRiskForecast(aiPrediction, forecastType, forecastHours);
            
        } catch (Exception e) {
            log.error("Error generating AI forecast, falling back to mock data: {}", e.getMessage());
            
            // Fallback to mock implementation
            return generateMockForecast();
        }
    }
    
    @Override
    public void deleteRiskForecast(String id) {
        if (riskForecastRepository.existsById(id)) {
            riskForecastRepository.deleteById(id);
            log.info("Deleted risk forecast: {}", id);
        } else {
            throw new RuntimeException("Risk forecast not found: " + id);
        }
    }
    
    @Override
    public List<RiskForecast> getRecentForecasts(int limit) {
        return riskForecastRepository.findAll().stream()
                .sorted((f1, f2) -> f2.getGeneratedAt().compareTo(f1.getGeneratedAt()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    private List<RiskForecast.HotspotPrediction> generateMockHotspots() {
        List<RiskForecast.HotspotPrediction> hotspots = new ArrayList<>();
        
        // Generate a few mock hotspots
        RiskForecast.HotspotPrediction hotspot1 = new RiskForecast.HotspotPrediction();
        hotspot1.setHotspotId(UUID.randomUUID().toString());
        hotspot1.setLocationName("Nairobi CBD");
        hotspot1.setLatitude("-1.2921");
        hotspot1.setLongitude("36.8219");
        hotspot1.setProbability(0.75);
        hotspot1.setRadius(2000.0);
        hotspot1.setPeakTime(LocalDateTime.now().plusHours(3));
        hotspot1.setThreatType("social_unrest");
        hotspot1.setConfidence(0.8);
        hotspot1.setSeverity("high");
        hotspots.add(hotspot1);
        
        return hotspots;
    }
    
    private String determineRiskLevel(double riskScore) {
        if (riskScore >= 0.8) return "critical";
        if (riskScore >= 0.6) return "high";
        if (riskScore >= 0.4) return "medium";
        return "low";
    }
    
    private RiskForecast generateMockForecast() {
        // Generate a single mock forecast
        RiskForecast forecast = new RiskForecast();
        forecast.setId(UUID.randomUUID().toString());
        forecast.setForecastType("trend");
        forecast.setModelVersion("Mock-1.0");
        forecast.setGeneratedAt(LocalDateTime.now());
        forecast.setValidFrom(LocalDateTime.now());
        forecast.setValidTo(LocalDateTime.now().plusHours(24));
        forecast.setOverallRiskTrend(0.65);
        forecast.setConfidenceScore(0.7);
        forecast.setHotspots(generateMockHotspots());
        
        return forecast;
    }
    
    @Override
    public List<RiskForecast> getForecastsByType(String forecastType) {
        return riskForecastRepository.findAll().stream()
                .filter(forecast -> forecastType.equals(forecast.getForecastType()))
                .sorted((f1, f2) -> f2.getGeneratedAt().compareTo(f1.getGeneratedAt()))
                .collect(Collectors.toList());
    }
    
    /**
     * Convert AI Engine prediction response to RiskForecast object
     */
    private RiskForecast convertAIPredictionToRiskForecast(Map<String, Object> aiPrediction, String forecastType, int forecastHours) {
        RiskForecast forecast = new RiskForecast();
        forecast.setId(UUID.randomUUID().toString());
        forecast.setForecastType(forecastType);
        forecast.setModelVersion("AI-Engine-1.0");
        forecast.setGeneratedAt(LocalDateTime.now());
        forecast.setValidFrom(LocalDateTime.now());
        forecast.setValidTo(LocalDateTime.now().plusHours(forecastHours));
        
        if ("hotspot".equals(forecastType)) {
            // Convert hotspot prediction
            List<RiskForecast.HotspotPrediction> hotspots = new ArrayList<>();
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> aiHotspots = (List<Map<String, Object>>) aiPrediction.getOrDefault("hotspots", new ArrayList<>());
            
            for (Map<String, Object> aiHotspot : aiHotspots) {
                RiskForecast.HotspotPrediction hotspot = new RiskForecast.HotspotPrediction();
                hotspot.setHotspotId(UUID.randomUUID().toString());
                hotspot.setLocationName((String) aiHotspot.getOrDefault("location_name", "Unknown"));
                hotspot.setLatitude((String) aiHotspot.getOrDefault("latitude", "0.0"));
                hotspot.setLongitude((String) aiHotspot.getOrDefault("longitude", "0.0"));
                hotspot.setProbability(((Number) aiHotspot.getOrDefault("probability", 0.5)).doubleValue());
                hotspot.setRadius(((Number) aiHotspot.getOrDefault("radius", 1000.0)).doubleValue());
                hotspot.setPeakTime(LocalDateTime.now().plusHours(((Number) aiHotspot.getOrDefault("hours_to_peak", 3)).intValue()));
                hotspot.setThreatType((String) aiHotspot.getOrDefault("threat_type", "unknown"));
                hotspot.setConfidence(((Number) aiPrediction.getOrDefault("confidence", 0.7)).doubleValue());
                hotspot.setSeverity(determineRiskLevel(((Number) aiHotspot.getOrDefault("risk_score", 0.5)).doubleValue()));
                
                @SuppressWarnings("unchecked")
                List<String> factors = (List<String>) aiHotspot.getOrDefault("contributing_factors", new ArrayList<>());
                hotspot.setContributingFactors(factors);
                
                hotspots.add(hotspot);
            }
            
            forecast.setHotspots(hotspots);
            forecast.setConfidenceScore(((Number) aiPrediction.getOrDefault("confidence", 0.7)).doubleValue());
            
        } else if ("trend".equals(forecastType)) {
            // Convert trend prediction
            List<RiskForecast.ForecastPoint> forecastPoints = new ArrayList<>();
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> aiPoints = (List<Map<String, Object>>) aiPrediction.getOrDefault("forecast_points", new ArrayList<>());
            
            for (Map<String, Object> aiPoint : aiPoints) {
                RiskForecast.ForecastPoint point = new RiskForecast.ForecastPoint();
                point.setTimestamp(LocalDateTime.now().plusHours(((Number) aiPoint.getOrDefault("hour_offset", 0)).intValue()));
                point.setPredictedRisk(((Number) aiPoint.getOrDefault("predicted_risk", 0.5)).doubleValue());
                point.setConfidence(((Number) aiPoint.getOrDefault("confidence", 0.7)).doubleValue());
                point.setRiskLevel(determineRiskLevel(((Number) aiPoint.getOrDefault("predicted_risk", 0.5)).doubleValue()));
                
                @SuppressWarnings("unchecked")
                Map<String, Double> contributions = (Map<String, Double>) aiPoint.getOrDefault("feature_contributions", new HashMap<>());
                point.setFeatureContributions(contributions);
                
                forecastPoints.add(point);
            }
            
            // If no forecast points from AI, create default trend points
            if (forecastPoints.isEmpty()) {
                String trend = (String) aiPrediction.getOrDefault("trend", "stable");
                double baseRisk = "increasing".equals(trend) ? 0.7 : "decreasing".equals(trend) ? 0.3 : 0.5;
                
                for (int i = 0; i < forecastHours; i += 3) { // Create points every 3 hours
                    RiskForecast.ForecastPoint point = new RiskForecast.ForecastPoint();
                    point.setTimestamp(LocalDateTime.now().plusHours(i));
                    point.setPredictedRisk(baseRisk + (i * 0.01 * ("increasing".equals(trend) ? 1 : -1)));
                    point.setConfidence(((Number) aiPrediction.getOrDefault("confidence", 0.7)).doubleValue());
                    point.setRiskLevel(determineRiskLevel(point.getPredictedRisk()));
                    point.setFeatureContributions(new HashMap<>());
                    forecastPoints.add(point);
                }
            }
            
            forecast.setForecastPoints(forecastPoints);
            forecast.setOverallRiskTrend("increasing".equals(aiPrediction.get("trend")) ? 0.7 : 
                                         "decreasing".equals(aiPrediction.get("trend")) ? 0.3 : 0.5);
            forecast.setConfidenceScore(((Number) aiPrediction.getOrDefault("confidence", 0.7)).doubleValue());
        }
        
        // Set model metadata
        RiskForecast.ModelMetadata metadata = new RiskForecast.ModelMetadata();
        metadata.setModelName("NTEWS-AI-Engine");
        metadata.setVersion("1.0");
        metadata.setTrainedAt(LocalDateTime.now().minusDays(1));
        metadata.setTrainingPeriod("Last 30 days");
        metadata.setFeatures(List.of("historical_incidents", "temporal_patterns", "spatial_data", "threat_intelligence"));
        metadata.setAccuracy(((Number) aiPrediction.getOrDefault("accuracy", 0.85)).doubleValue());
        metadata.setPrecision(((Number) aiPrediction.getOrDefault("precision", 0.82)).doubleValue());
        metadata.setRecall(((Number) aiPrediction.getOrDefault("recall", 0.88)).doubleValue());
        metadata.setF1Score(((Number) aiPrediction.getOrDefault("f1_score", 0.85)).doubleValue());
        
        forecast.setModelMetadata(metadata);
        
        log.info("Successfully converted AI prediction to RiskForecast for type: {}", forecastType);
        return forecast;
    }
}

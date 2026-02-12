package com.ntews.predict.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskForecast {
    private String id;
    private String forecastType; // trend, hotspot, seasonal
    private String modelVersion;
    private LocalDateTime generatedAt;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    
    // Forecast data
    private List<ForecastPoint> forecastPoints;
    private Double overallRiskTrend;
    private Double confidenceScore;
    
    // Location-based forecasts
    private List<LocationRisk> locationRisks;
    private List<HotspotPrediction> hotspots;
    
    // Model metadata
    private ModelMetadata modelMetadata;
    
    // Performance metrics
    private ForecastMetrics metrics;
    
    // Manual getters and setters to bypass Lombok issues
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getForecastType() { return forecastType; }
    public void setForecastType(String forecastType) { this.forecastType = forecastType; }
    
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    
    public LocalDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDateTime validFrom) { this.validFrom = validFrom; }
    
    public LocalDateTime getValidTo() { return validTo; }
    public void setValidTo(LocalDateTime validTo) { this.validTo = validTo; }
    
    public List<ForecastPoint> getForecastPoints() { return forecastPoints; }
    public void setForecastPoints(List<ForecastPoint> forecastPoints) { this.forecastPoints = forecastPoints; }
    
    public Double getOverallRiskTrend() { return overallRiskTrend; }
    public void setOverallRiskTrend(Double overallRiskTrend) { this.overallRiskTrend = overallRiskTrend; }
    
    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }
    
    public List<LocationRisk> getLocationRisks() { return locationRisks; }
    public void setLocationRisks(List<LocationRisk> locationRisks) { this.locationRisks = locationRisks; }
    
    public List<HotspotPrediction> getHotspots() { return hotspots; }
    public void setHotspots(List<HotspotPrediction> hotspots) { this.hotspots = hotspots; }
    
    public ModelMetadata getModelMetadata() { return modelMetadata; }
    public void setModelMetadata(ModelMetadata modelMetadata) { this.modelMetadata = modelMetadata; }
    
    public ForecastMetrics getMetrics() { return metrics; }
    public void setMetrics(ForecastMetrics metrics) { this.metrics = metrics; }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ForecastPoint {
        private LocalDateTime timestamp;
        private Double predictedRisk;
        private Double confidence;
        private Map<String, Double> featureContributions;
        private String riskLevel; // low, medium, high, critical
        
        // Manual getters and setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public Double getPredictedRisk() { return predictedRisk; }
        public void setPredictedRisk(Double predictedRisk) { this.predictedRisk = predictedRisk; }
        
        public Double getConfidence() { return confidence; }
        public void setConfidence(Double confidence) { this.confidence = confidence; }
        
        public Map<String, Double> getFeatureContributions() { return featureContributions; }
        public void setFeatureContributions(Map<String, Double> featureContributions) { this.featureContributions = featureContributions; }
        
        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LocationRisk {
        private String locationId;
        private String locationName;
        private String latitude;
        private String longitude;
        private Double currentRisk;
        private Double predictedRisk;
        private Double riskChange;
        private String trendDirection; // increasing, decreasing, stable
        private Double confidence;
        private List<String> riskFactors;
        
        // Manual getters and setters
        public String getLocationId() { return locationId; }
        public void setLocationId(String locationId) { this.locationId = locationId; }
        
        public String getLocationName() { return locationName; }
        public void setLocationName(String locationName) { this.locationName = locationName; }
        
        public String getLatitude() { return latitude; }
        public void setLatitude(String latitude) { this.latitude = latitude; }
        
        public String getLongitude() { return longitude; }
        public void setLongitude(String longitude) { this.longitude = longitude; }
        
        public Double getCurrentRisk() { return currentRisk; }
        public void setCurrentRisk(Double currentRisk) { this.currentRisk = currentRisk; }
        
        public Double getPredictedRisk() { return predictedRisk; }
        public void setPredictedRisk(Double predictedRisk) { this.predictedRisk = predictedRisk; }
        
        public Double getRiskChange() { return riskChange; }
        public void setRiskChange(Double riskChange) { this.riskChange = riskChange; }
        
        public String getTrendDirection() { return trendDirection; }
        public void setTrendDirection(String trendDirection) { this.trendDirection = trendDirection; }
        
        public Double getConfidence() { return confidence; }
        public void setConfidence(Double confidence) { this.confidence = confidence; }
        
        public List<String> getRiskFactors() { return riskFactors; }
        public void setRiskFactors(List<String> riskFactors) { this.riskFactors = riskFactors; }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HotspotPrediction {
        private String hotspotId;
        private String locationName;
        private String latitude;
        private String longitude;
        private Double probability;
        private Double radius;  // Predicted hotspot radius in meters
        private LocalDateTime peakTime;
        private String threatType;
        private List<String> contributingFactors;
        private Double confidence;
        private String severity; // low, medium, high, critical
        
        // Manual getters and setters
        public String getHotspotId() { return hotspotId; }
        public void setHotspotId(String hotspotId) { this.hotspotId = hotspotId; }
        
        public String getLocationName() { return locationName; }
        public void setLocationName(String locationName) { this.locationName = locationName; }
        
        public String getLatitude() { return latitude; }
        public void setLatitude(String latitude) { this.latitude = latitude; }
        
        public String getLongitude() { return longitude; }
        public void setLongitude(String longitude) { this.longitude = longitude; }
        
        public Double getProbability() { return probability; }
        public void setProbability(Double probability) { this.probability = probability; }
        
        public Double getRadius() { return radius; }
        public void setRadius(Double radius) { this.radius = radius; }
        
        public LocalDateTime getPeakTime() { return peakTime; }
        public void setPeakTime(LocalDateTime peakTime) { this.peakTime = peakTime; }
        
        public String getThreatType() { return threatType; }
        public void setThreatType(String threatType) { this.threatType = threatType; }
        
        public List<String> getContributingFactors() { return contributingFactors; }
        public void setContributingFactors(List<String> contributingFactors) { this.contributingFactors = contributingFactors; }
        
        public Double getConfidence() { return confidence; }
        public void setConfidence(Double confidence) { this.confidence = confidence; }
        
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ModelMetadata {
        private String modelName;
        private String version;
        private LocalDateTime trainedAt;
        private String trainingPeriod;
        private List<String> features;
        private Map<String, Double> featureImportance;
        private Double accuracy;
        private Double precision;
        private Double recall;
        private Double f1Score;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ForecastMetrics {
        private Double mae;  // Mean Absolute Error
        private Double rmse; // Root Mean Square Error
        private Double mape; // Mean Absolute Percentage Error
        private Double directionalAccuracy;
        private Integer totalPredictions;
        private Integer correctPredictions;
        private LocalDateTime lastUpdated;
    }
}

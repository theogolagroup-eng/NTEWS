package com.ntews.predict.service;

import com.ntews.predict.controller.PredictionController.*;
import com.ntews.predict.model.RiskForecast;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface PredictionService {
    
    List<RiskForecast> getRiskForecasts(String forecastType, LocalDateTime validFrom, LocalDateTime validTo);
    
    RiskForecast getRiskForecast(String id);
    
    RiskForecast getCurrentForecast();
    
    List<HotspotData> getCurrentHotspots();
    
    HotspotDetail getHotspotDetail(String id);
    
    List<RiskTrendData> getRiskTrends(int hours);
    
    PredictionDashboardSummary getDashboardSummary();
    
    List<LocationRiskData> getLocationRisks(String latitude, String longitude, double radiusKm);
    
    RiskForecast generateForecast(String forecastType, Map<String, Object> parameters);
    
    void deleteRiskForecast(String id);
    
    List<RiskForecast> getRecentForecasts(int limit);
    
    List<RiskForecast> getForecastsByType(String forecastType);
}

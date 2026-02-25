package com.ntews.prediction.service;

import com.ntews.prediction.model.Prediction;
import com.ntews.prediction.model.LocationRisk;
import com.ntews.prediction.model.RiskTrend;
import com.ntews.prediction.model.DashboardSummary;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PredictionService {
    
    List<Prediction> getPredictions(String type, String category, String location, String severity, 
                                     LocalDateTime from, LocalDateTime to, int hours, int page, int size);
    
    Optional<Prediction> getPrediction(String id);
    
    DashboardSummary getDashboardSummary();
    
    List<Prediction> getForecasts(String type, int hours);
    
    List<LocationRisk> getLocationRisk(String latitude, String longitude, double radiusKm);
    
    List<RiskTrend> getRiskTrends(int hours);
    
    Prediction createPrediction(Prediction prediction);
    
    Prediction updatePrediction(Prediction prediction);
    
    Prediction verifyPrediction(String id, boolean verified, String notes);
    
    Prediction cancelPrediction(String id);
}

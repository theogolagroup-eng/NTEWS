package com.ntews.prediction.service;

import com.ntews.prediction.controller.PredictionController.*;
import com.ntews.prediction.model.Prediction;
import com.ntews.prediction.model.LocationRisk;
import com.ntews.prediction.model.RiskTrend;
import com.ntews.prediction.model.DashboardSummary;
import com.ntews.prediction.repository.PredictionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionServiceImpl implements PredictionService {
    
    private final PredictionRepository predictionRepository;
    
    @Override
    public List<Prediction> getPredictions(String type, String category, String location, String severity, 
                                         LocalDateTime from, LocalDateTime to, int hours, int page, int size) {
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime validFrom = from != null ? from : now.minusHours(hours);
        LocalDateTime validUntil = to != null ? to : now.plusHours(hours);
        
        // Query real predictions from database
        List<Prediction> predictions = predictionRepository.findByValidFromBeforeAndValidUntilAfter(validFrom, validUntil, null)
                .getContent();
        
        // Apply filters
        if (type != null) {
            predictions = predictions.stream()
                    .filter(p -> type.equals(p.getType()))
                    .collect(Collectors.toList());
        }
        
        if (category != null) {
            predictions = predictions.stream()
                    .filter(p -> category.equals(p.getCategory()))
                    .collect(Collectors.toList());
        }
        
        if (location != null) {
            predictions = predictions.stream()
                    .filter(p -> location.equals(p.getLocation()))
                    .collect(Collectors.toList());
        }
        
        if (severity != null) {
            predictions = predictions.stream()
                    .filter(p -> severity.equals(p.getSeverity()))
                    .collect(Collectors.toList());
        }
        
        return predictions;
    }
    
    @Override
    public Optional<Prediction> getPrediction(String id) {
        return predictionRepository.findById(id);
    }
    
    @Override
    public DashboardSummary getDashboardSummary() {
        LocalDateTime now = LocalDateTime.now();
        
        // Get real counts from database
        long totalPredictions = predictionRepository.count();
        List<Prediction> activePredictions = predictionRepository.findActivePredictions(now);
        List<Prediction> highRiskPredictions = predictionRepository.findActivePredictionsBySeverity(now, "high");
        List<Prediction> criticalPredictions = predictionRepository.findActivePredictionsBySeverity(now, "critical");
        List<Prediction> expiredPredictions = predictionRepository.findExpiredPredictions(now);
        
        DashboardSummary summary = DashboardSummary.builder()
                .totalPredictions((int) totalPredictions)
                .activePredictions(activePredictions.size())
                .highRiskPredictions(highRiskPredictions.size())
                .criticalPredictions(criticalPredictions.size())
                .expiredPredictions(expiredPredictions.size())
                .build();
        
        return summary;
    }
    
    @Override
    public List<Prediction> getForecasts(String type, int hours) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusHours(hours);
        
        if (type != null) {
            return predictionRepository.findActivePredictionsByType(type, now);
        } else {
            return predictionRepository.findActivePredictions(now);
        }
    }
    
    @Override
    public List<LocationRisk> getLocationRisk(String latitude, String longitude, double radiusKm) {
        LocalDateTime now = LocalDateTime.now();
        
        // Get real predictions with location data
        List<Prediction> locationPredictions = predictionRepository.findActivePredictionsWithLocation(now);
        
        // Convert to location risk scores based on real predictions
        Map<String, List<Prediction>> predictionsByLocation = locationPredictions.stream()
                .collect(Collectors.groupingBy(Prediction::getLocation));
        
        List<LocationRisk> risks = new ArrayList<>();
        
        for (Map.Entry<String, List<Prediction>> entry : predictionsByLocation.entrySet()) {
            String location = entry.getKey();
            List<Prediction> predictions = entry.getValue();
            
            if (!predictions.isEmpty()) {
                // Calculate risk score based on real predictions
                double avgRiskScore = predictions.stream()
                        .mapToDouble(p -> p.getConfidence() != null ? p.getConfidence() : 0.0)
                        .average()
                        .orElse(0.0);
                
                // Determine risk level based on severity distribution
                String riskLevel = determineRiskLevel(predictions);
                
                // Collect all risk factors
                List<String> allRiskFactors = predictions.stream()
                        .flatMap(p -> p.getRiskFactors() != null ? p.getRiskFactors().stream() : new ArrayList<String>().stream())
                        .distinct()
                        .collect(Collectors.toList());
                
                // Get location coordinates from first prediction
                Prediction firstPred = predictions.get(0);
                
                LocationRisk risk = LocationRisk.builder()
                        .location(location)
                        .latitude(firstPred.getLatitude())
                        .longitude(firstPred.getLongitude())
                        .riskScore(avgRiskScore)
                        .riskLevel(riskLevel)
                        .riskFactors(allRiskFactors)
                        .build();
                
                risks.add(risk);
            }
        }
        
        return risks;
    }
    
    @Override
    public List<RiskTrend> getRiskTrends(int hours) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusHours(hours);
        
        // Get real predictions from the specified time range
        List<Prediction> predictions = predictionRepository.findByPredictedAtBetween(start, now, null)
                .getContent();
        
        // Group by hour and calculate risk trends
        Map<String, List<Prediction>> predictionsByHour = predictions.stream()
                .collect(Collectors.groupingBy(p -> 
                    p.getPredictedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00"))));
        
        List<RiskTrend> trends = new ArrayList<>();
        
        for (Map.Entry<String, List<Prediction>> entry : predictionsByHour.entrySet()) {
            String timestamp = entry.getKey();
            List<Prediction> hourPredictions = entry.getValue();
            
            if (!hourPredictions.isEmpty()) {
                // Calculate average risk score for the hour
                double avgRiskScore = hourPredictions.stream()
                        .mapToDouble(p -> p.getConfidence() != null ? p.getConfidence() : 0.0)
                        .average()
                        .orElse(0.0);
                
                // Determine risk level
                String riskLevel = determineRiskLevel(hourPredictions);
                
                RiskTrend trend = RiskTrend.builder()
                        .timestamp(timestamp)
                        .type("overall")
                        .riskScore(avgRiskScore)
                        .riskLevel(riskLevel)
                        .count(hourPredictions.size())
                        .build();
                
                trends.add(trend);
            }
        }
        
        // Sort by timestamp
        trends.sort(Comparator.comparing(RiskTrend::getTimestamp));
        
        return trends;
    }
    
    @Override
    public Prediction createPrediction(Prediction prediction) {
        prediction.setId(UUID.randomUUID().toString());
        prediction.setPredictedAt(LocalDateTime.now());
        prediction.setStatus("ACTIVE");
        prediction.setLastUpdated(LocalDateTime.now());
        
        // Set default values if not provided
        if (prediction.getValidFrom() == null) {
            prediction.setValidFrom(LocalDateTime.now());
        }
        if (prediction.getValidUntil() == null) {
            prediction.setValidUntil(LocalDateTime.now().plusHours(24));
        }
        if (prediction.getConfidence() == null) {
            prediction.setConfidence(0.5);
        }
        
        return predictionRepository.save(prediction);
    }
    
    @Override
    public Prediction updatePrediction(Prediction prediction) {
        prediction.setLastUpdated(LocalDateTime.now());
        return predictionRepository.save(prediction);
    }
    
    @Override
    public Prediction verifyPrediction(String id, boolean verified, String notes) {
        Optional<Prediction> opt = predictionRepository.findById(id);
        if (opt.isPresent()) {
            Prediction prediction = opt.get();
            prediction.setVerifiedBy(verified ? "system" : "manual");
            prediction.setNotes(notes);
            prediction.setLastUpdated(LocalDateTime.now());
            return predictionRepository.save(prediction);
        }
        throw new RuntimeException("Prediction not found: " + id);
    }
    
    @Override
    public Prediction cancelPrediction(String id) {
        Optional<Prediction> opt = predictionRepository.findById(id);
        if (opt.isPresent()) {
            Prediction prediction = opt.get();
            prediction.setStatus("CANCELLED");
            prediction.setLastUpdated(LocalDateTime.now());
            return predictionRepository.save(prediction);
        }
        throw new RuntimeException("Prediction not found: " + id);
    }
    
    private String determineRiskLevel(List<Prediction> predictions) {
        long criticalCount = predictions.stream()
                .filter(p -> "critical".equals(p.getSeverity()))
                .count();
        long highCount = predictions.stream()
                .filter(p -> "high".equals(p.getSeverity()))
                .count();
        long mediumCount = predictions.stream()
                .filter(p -> "medium".equals(p.getSeverity()))
                .count();
        
        if (criticalCount > 0) return "critical";
        if (highCount > mediumCount) return "high";
        if (mediumCount > 0) return "medium";
        return "low";
    }
}

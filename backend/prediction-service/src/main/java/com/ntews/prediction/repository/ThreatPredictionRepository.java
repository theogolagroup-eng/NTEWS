package com.ntews.prediction.repository;

import com.ntews.prediction.model.ThreatPrediction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Threat Prediction documents
 */
@Repository
public interface ThreatPredictionRepository extends MongoRepository<ThreatPrediction, String> {
    
    /**
     * Find active predictions
     */
    List<ThreatPrediction> findByStatus(String status);
    
    /**
     * Find predictions by type
     */
    List<ThreatPrediction> findByPredictionType(String predictionType);
    
    /**
     * Find high-risk predictions
     */
    List<ThreatPrediction> findByRiskScoreGreaterThan(double riskScore);
    
    /**
     * Find predictions by affected region
     */
    List<ThreatPrediction> findByAffectedRegionContaining(String region);
    
    /**
     * Find expired predictions
     */
    List<ThreatPrediction> findByExpiresAtBefore(LocalDateTime dateTime);
    
    /**
     * Find predictions with Sheng content
     */
    List<ThreatPrediction> findByShengKeywordsIsNotNullAndShengKeywordsIsNot();
    
    /**
     * Count active predictions
     */
    long countByStatus(String status);
}

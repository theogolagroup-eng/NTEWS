package com.ntews.prediction.repository;

import com.ntews.prediction.model.Prediction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PredictionRepository extends MongoRepository<Prediction, String> {
    
    Page<Prediction> findByType(String type, Pageable pageable);
    
    Page<Prediction> findByCategory(String category, Pageable pageable);
    
    Page<Prediction> findBySeverity(String severity, Pageable pageable);
    
    Page<Prediction> findByLocation(String location, Pageable pageable);
    
    Page<Prediction> findByStatus(String status, Pageable pageable);
    
    Page<Prediction> findByTypeAndCategory(String type, String category, Pageable pageable);
    
    Page<Prediction> findByTypeAndSeverity(String type, String severity, Pageable pageable);
    
    Page<Prediction> findByValidFromBeforeAndValidUntilAfter(LocalDateTime validFrom, LocalDateTime validUntil, Pageable pageable);
    
    @Query("{ 'type': ?0, 'validFrom': { $lte: ?1 }, 'validUntil': { $gte: ?1 } }")
    List<Prediction> findActivePredictionsByType(String type, LocalDateTime currentTime);
    
    @Query("{ 'status': 'ACTIVE', 'validFrom': { $lte: ?0 }, 'validUntil': { $gte: ?0 } }")
    List<Prediction> findActivePredictions(LocalDateTime currentTime);
    
    @Query("{ 'status': 'ACTIVE', 'validFrom': { $lte: ?0 }, 'validUntil': { $gte: ?0 }, 'severity': ?1 }")
    List<Prediction> findActivePredictionsBySeverity(LocalDateTime currentTime, String severity);
    
    @Query("{ 'location.latitude': { $ne: null }, 'location.longitude': { $ne: null }, 'validFrom': { $lte: ?0 }, 'validUntil': { $gte: ?0 } }")
    List<Prediction> findActivePredictionsWithLocation(LocalDateTime currentTime);
    
    @Query("{ 'predictedAt': { $gte: ?0, $lte: ?1 } }")
    Page<Prediction> findByPredictedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    @Query("{ 'type': ?0, 'predictedAt': { $gte: ?1, $lte: ?2 } }")
    List<Prediction> findByTypeAndPredictedAtBetween(String type, LocalDateTime start, LocalDateTime end);
    
    @Query("{ 'confidence': { $gte: ?0 } }")
    List<Prediction> findByConfidenceGreaterThan(Double minConfidence);
    
    @Query("{ 'riskFactors': { $in: ?0 } }")
    List<Prediction> findByRiskFactorsIn(List<String> riskFactors);
    
    @Query("{ 'affectedAreas': { $in: ?0 } }")
    List<Prediction> findByAffectedAreasIn(List<String> areas);
    
    @Query("{ 'aiProcessed': true }")
    List<Prediction> findAIProcessedPredictions();
    
    @Query("{ 'status': { $ne: 'EXPIRED' }, 'validUntil': { $lte: ?0 } }")
    List<Prediction> findExpiredPredictions(LocalDateTime currentTime);
    
    @Query(value = "{ 'predictedAt': { $gte: ?0, $lte: ?1 } }", count = true)
    long countByPredictedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("{ 'type': ?0, 'status': 'ACTIVE' }")
    long countActivePredictionsByType(String type);
    
    @Query("{ 'severity': ?0, 'status': 'ACTIVE' }")
    long countActivePredictionsBySeverity(String severity);
}

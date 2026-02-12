package com.ntews.predict.repository;

import com.ntews.predict.model.RiskForecast;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RiskForecastRepository extends MongoRepository<RiskForecast, String> {
    
    List<RiskForecast> findByForecastType(String forecastType);
    
    List<RiskForecast> findByValidFromBeforeAndValidToAfter(LocalDateTime before, LocalDateTime after);
    
    List<RiskForecast> findByValidFromBefore(LocalDateTime dateTime);
    
    List<RiskForecast> findByValidToAfter(LocalDateTime dateTime);
    
    @Query("{ 'forecastType': ?0, 'validFrom': { $lte: ?1 }, 'validTo': { $gte: ?1 } }")
    List<RiskForecast> findValidForecastsByType(String forecastType, LocalDateTime dateTime);
    
    @Query("{ 'validFrom': { $lte: ?0 }, 'validTo': { $gte: ?0 } }")
    List<RiskForecast> findValidForecasts(LocalDateTime dateTime);
    
    @Query("{ 'validFrom': { $lte: ?0 }, 'validTo': { $gte: ?0 } }")
    RiskForecast findLatestValidForecast(LocalDateTime dateTime);
    
    @Query("{ 'forecastType': 'hotspot', 'validFrom': { $lte: ?0 }, 'validTo': { $gte: ?0 } }")
    List<RiskForecast> findValidHotspotForecasts(LocalDateTime dateTime);
    
    @Query("{ 'forecastType': 'trend', 'validFrom': { $lte: ?0 }, 'validTo': { $gte: ?0 } }")
    List<RiskForecast> findValidTrendForecasts(LocalDateTime dateTime);
    
    @Query("{ 'generatedAt': { $gte: ?0, $lte: ?1 } }")
    List<RiskForecast> findByGeneratedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("{ 'forecastType': ?0, 'generatedAt': { $gte: ?1, $lte: ?2 } }")
    List<RiskForecast> findByTypeAndGeneratedAtBetween(String forecastType, LocalDateTime start, LocalDateTime end);
    
    @Query(value = "{ 'generatedAt': { $gte: ?0, $lte: ?1 } }", 
           count = true)
    long countByGeneratedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("{ 'hotspots': { $exists: true, $ne: [] } }")
    List<RiskForecast> findWithHotspots();
    
        
    @Query("{ 'locationRisks': { $exists: true, $ne: [] } }")
    List<RiskForecast> findWithLocationRisks();
    
    @Query("{ 'forecastPoints': { $exists: true, $ne: [] } }")
    List<RiskForecast> findWithForecastPoints();
}

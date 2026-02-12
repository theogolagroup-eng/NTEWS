package com.ntews.intel.repository;

import com.ntews.intel.model.IntelligenceReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IntelligenceReportRepository extends MongoRepository<IntelligenceReport, String> {
    
    Page<IntelligenceReport> findByThreatLevel(String threatLevel, Pageable pageable);
    
    Page<IntelligenceReport> findByCategory(String category, Pageable pageable);
    
    Page<IntelligenceReport> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    @Query("{ 'threatLevel': ?0, 'createdAt': { $gte: ?1, $lte: ?2 } }")
    Page<IntelligenceReport> findByThreatLevelAndCreatedAtBetween(
            String threatLevel, LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    @Query("{ 'category': ?0, 'createdAt': { $gte: ?1, $lte: ?2 } }")
    Page<IntelligenceReport> findByCategoryAndCreatedAtBetween(
            String category, LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    List<IntelligenceReport> findByCreatedAtAfter(LocalDateTime dateTime);
    
    List<IntelligenceReport> findByLocationIsNotNull();
    
    @Query("{ 'location.latitude': { $ne: null }, 'location.longitude': { $ne: null }, 'createdAt': { $gte: ?0 } }")
    List<IntelligenceReport> findWithLocationAfter(LocalDateTime dateTime);
    
    @Query("{ 'threatLevel': { $in: ?0 } }")
    List<IntelligenceReport> findByThreatLevelIn(List<String> threatLevels);
    
    @Query("{ 'status': { $ne: 'archived' } }")
    List<IntelligenceReport> findActiveReports();
    
    @Query(value = "{ 'createdAt': { $gte: ?0, $lte: ?1 } }", 
           count = true)
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("{ 'category': ?0, 'createdAt': { $gte: ?1, $lte: ?2 } }")
    List<IntelligenceReport> findByCategoryAndCreatedAtBetween(String category, LocalDateTime start, LocalDateTime end);
}

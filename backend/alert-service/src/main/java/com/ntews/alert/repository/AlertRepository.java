package com.ntews.alert.repository;

import com.ntews.alert.model.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends MongoRepository<Alert, String> {
    
    Page<Alert> findBySeverity(String severity, Pageable pageable);
    
    Page<Alert> findByStatus(String status, Pageable pageable);
    
    List<Alert> findByStatus(String status);
    
    Page<Alert> findByCategory(String category, Pageable pageable);
    
    Page<Alert> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    @Query("{ 'severity': ?0, 'status': ?1, 'createdAt': { $gte: ?2, $lte: ?3 } }")
    Page<Alert> findBySeverityAndStatusAndCreatedAtBetween(
            String severity, String status, LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    @Query("{ 'status': { $ne: 'resolved' }, 'status': { $ne: 'closed' } }")
    Page<Alert> findActiveAlerts(Pageable pageable);
    
    @Query("{ 'status': 'active' }")
    List<Alert> findActiveAlertsOnly();
    
    @Query("{ 'status': 'active', 'severity': { $in: ?0 } }")
    List<Alert> findActiveAlertsBySeverity(List<String> severities);
    
    @Query("{ 'status': 'active' }")
    Page<Alert> findActiveAlerts(Pageable pageable);
    
    @Query("{ 'status': 'active', 'assignedTo': ?0 }")
    List<Alert> findActiveAlertsByAssignee(String assignedTo);
    
    @Query("{ 'location.latitude': { $ne: null }, 'location.longitude': { $ne: null }, 'createdAt': { $gte: ?0 } }")
    List<Alert> findWithLocationAfter(LocalDateTime dateTime);
    
    @Query("{ 'severity': { $in: ?0 } }")
    List<Alert> findBySeverityIn(List<String> severities);
    
    @Query("{ 'category': ?0, 'createdAt': { $gte: ?1, $lte: ?2 } }")
    List<Alert> findByCategoryAndCreatedAtBetween(String category, LocalDateTime start, LocalDateTime end);
    
    @Query("{ 'resolved': true, 'resolvedAt': { $gte: ?0, $lte: ?1 } }")
    List<Alert> findResolvedAlertsBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("{ 'status': 'active', 'createdAt': { $gte: ?0 } }")
    List<Alert> findActiveAlertsAfter(LocalDateTime dateTime);
    
    @Query(value = "{ 'createdAt': { $gte: ?0, $lte: ?1 } }", 
           count = true)
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("{ 'resolved': true, 'resolvedAt': { $gte: ?0, $lte: ?1 } }")
    long countResolvedAlertsBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("{ 'status': 'active', 'severity': ?0 }")
    long countActiveAlertsBySeverity(String severity);
}

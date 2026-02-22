package com.ntews.alert.repository;

import com.ntews.alert.model.ActionPoint;
import com.ntews.alert.model.ActionPoint.ActionStatus;
import com.ntews.alert.model.ActionPoint.Priority;
import com.ntews.alert.model.ActionPoint.ActionType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActionPointRepository extends MongoRepository<ActionPoint, String> {
    
    // Find by status
    List<ActionPoint> findByStatus(ActionStatus status);
    
    // Find by assigned user
    List<ActionPoint> findByAssignedTo(String assignedTo);
    
    // Find by priority
    List<ActionPoint> findByPriority(Priority priority);
    
    // Find by type
    List<ActionPoint> findByType(ActionType type);
    
    // Find by related alert ID
    List<ActionPoint> findByRelatedAlertId(String relatedAlertId);
    
    // Find by related threat ID
    List<ActionPoint> findByRelatedThreatId(String relatedThreatId);
    
    // Find by related hotspot ID
    List<ActionPoint> findByRelatedHotspotId(String relatedHotspotId);
    
    // Find by created by
    List<ActionPoint> findByCreatedBy(String createdBy);
    
    // Find auto-triggered actions
    List<ActionPoint> findByAutoTriggered(Boolean autoTriggered);
    
    // Find actions requiring human approval
    List<ActionPoint> findByHumanApprovalRequired(Boolean humanApprovalRequired);
    
    // Find overdue actions
    @Query("{ 'dueDate': { $lt: ?0 }, 'status': { $ne: 'completed' } }")
    List<ActionPoint> findOverdueActions(LocalDateTime currentDate);
    
    // Find actions created within date range
    @Query("{ 'createdAt': { $gte: ?0, $lte: ?1 } }")
    List<ActionPoint> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find actions by multiple criteria
    @Query("{ $and: [ { 'status': ?0 }, { 'priority': ?1 } ] }")
    List<ActionPoint> findByStatusAndPriority(ActionStatus status, Priority priority);
    
    // Find actions by workflow
    List<ActionPoint> findByWorkflowId(String workflowId);
    
    // Find actions with AI recommendations
    List<ActionPoint> findByAiRecommendationExists(Boolean exists);
    
    // Count actions by status
    long countByStatus(ActionStatus status);
    
    // Count actions by priority
    long countByPriority(Priority priority);
    
    // Count actions by assigned user
    long countByAssignedTo(String assignedTo);
    
    // Find pending actions for a user
    @Query("{ 'assignedTo': ?0, 'status': { $in: ['pending', 'in_progress'] } }")
    List<ActionPoint> findPendingActionsForUser(String assignedTo);
    
    // Find completed actions within date range
    @Query("{ 'status': 'completed', 'updatedAt': { $gte: ?0, $lte: ?1 } }")
    List<ActionPoint> findCompletedActionsBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Search actions by title or description
    @Query("{ $or: [ { 'title': { $regex: ?0, $options: 'i' } }, { 'description': { $regex: ?0, $options: 'i' } } ] }")
    List<ActionPoint> searchByTitleOrDescription(String searchTerm);
    
    // Find actions with completion percentage less than threshold
    @Query("{ 'completionPercentage': { $lt: ?0 }, 'status': { $ne: 'completed' } }")
    List<ActionPoint> findActionsWithLowCompletion(double threshold);
    
    // Find escalated actions
    @Query("{ 'escalationLevel': { $gt: 0 } }")
    List<ActionPoint> findEscalatedActions();
    
    // Find actions created in last N days
    @Query("{ 'createdAt': { $gte: ?0 } }")
    List<ActionPoint> findActionsCreatedSince(LocalDateTime since);
}

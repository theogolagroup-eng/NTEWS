package com.ntews.alert.service;

import com.ntews.alert.model.ActionPoint;
import com.ntews.alert.model.ActionItem;
import com.ntews.alert.model.Alert;
import com.ntews.alert.repository.ActionPointRepository;
import com.ntews.alert.repository.AlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ActionPointService {
    
    private static final Logger logger = LoggerFactory.getLogger(ActionPointService.class);
    
    @Autowired
    private ActionPointRepository actionPointRepository;
    
    @Autowired
    private AlertRepository alertRepository;
    
    // Create Action Point
    public ActionPoint createActionPoint(ActionPoint actionPoint) {
        try {
            logger.info("Creating action point: {}", actionPoint.getTitle());
            
            // Set creation timestamp if not set
            if (actionPoint.getCreatedAt() == null) {
                actionPoint.setCreatedAt(LocalDateTime.now());
            }
            actionPoint.setUpdatedAt(LocalDateTime.now());
            
            // Generate ID if not present
            if (actionPoint.getId() == null || actionPoint.getId().isEmpty()) {
                actionPoint.setId(UUID.randomUUID().toString());
            }
            
            // Initialize actions list if null
            if (actionPoint.getActions() == null) {
                actionPoint.setActions(new ArrayList<>());
            }
            
            // Update completion percentage
            actionPoint.updateCompletionPercentage();
            
            // Auto-trigger related actions if needed
            if (actionPoint.getAutoTriggered() != null && actionPoint.getAutoTriggered()) {
                handleAutoTriggeredAction(actionPoint);
            }
            
            ActionPoint saved = actionPointRepository.save(actionPoint);
            logger.info("Successfully created action point with ID: {}", saved.getId());
            
            return saved;
        } catch (Exception e) {
            logger.error("Error creating action point: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create action point", e);
        }
    }
    
    // Update Action Point
    public ActionPoint updateActionPoint(String id, ActionPoint updates) {
        try {
            logger.info("Updating action point: {}", id);
            
            Optional<ActionPoint> existingOpt = actionPointRepository.findById(id);
            if (!existingOpt.isPresent()) {
                throw new RuntimeException("Action point not found with ID: " + id);
            }
            
            ActionPoint existing = existingOpt.get();
            
            // Update fields
            if (updates.getTitle() != null) {
                existing.setTitle(updates.getTitle());
            }
            if (updates.getDescription() != null) {
                existing.setDescription(updates.getDescription());
            }
            if (updates.getType() != null) {
                existing.setType(updates.getType());
            }
            if (updates.getPriority() != null) {
                existing.setPriority(updates.getPriority());
            }
            if (updates.getStatus() != null) {
                existing.setStatus(updates.getStatus());
            }
            if (updates.getAssignedTo() != null) {
                existing.setAssignedTo(updates.getAssignedTo());
            }
            if (updates.getDueDate() != null) {
                existing.setDueDate(updates.getDueDate());
            }
            if (updates.getHumanNotes() != null) {
                existing.setHumanNotes(updates.getHumanNotes());
            }
            if (updates.getAiRecommendation() != null) {
                existing.setAiRecommendation(updates.getAiRecommendation());
            }
            if (updates.getActions() != null) {
                existing.setActions(updates.getActions());
            }
            
            existing.setUpdatedAt(LocalDateTime.now());
            existing.updateCompletionPercentage();
            
            ActionPoint saved = actionPointRepository.save(existing);
            logger.info("Successfully updated action point: {}", saved.getId());
            
            return saved;
        } catch (Exception e) {
            logger.error("Error updating action point {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update action point", e);
        }
    }
    
    // Get Action Point by ID
    public Optional<ActionPoint> getActionPointById(String id) {
        try {
            return actionPointRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error retrieving action point {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    // Get All Action Points
    public List<ActionPoint> getAllActionPoints() {
        try {
            return actionPointRepository.findAll();
        } catch (Exception e) {
            logger.error("Error retrieving all action points: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    // Delete Action Point
    public boolean deleteActionPoint(String id) {
        try {
            logger.info("Deleting action point: {}", id);
            
            if (actionPointRepository.existsById(id)) {
                actionPointRepository.deleteById(id);
                logger.info("Successfully deleted action point: {}", id);
                return true;
            } else {
                logger.warn("Action point not found for deletion: {}", id);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error deleting action point {}: {}", id, e.getMessage(), e);
            return false;
        }
    }
    
    // Assign Action Point to User
    public ActionPoint assignActionPoint(String id, String assignedTo) {
        try {
            logger.info("Assigning action point {} to user: {}", id, assignedTo);
            
            Optional<ActionPoint> existingOpt = actionPointRepository.findById(id);
            if (!existingOpt.isPresent()) {
                throw new RuntimeException("Action point not found with ID: " + id);
            }
            
            ActionPoint existing = existingOpt.get();
            existing.setAssignedTo(assignedTo);
            existing.setStatus(ActionPoint.ActionStatus.IN_PROGRESS);
            existing.setUpdatedAt(LocalDateTime.now());
            
            ActionPoint saved = actionPointRepository.save(existing);
            logger.info("Successfully assigned action point {} to user: {}", id, assignedTo);
            
            return saved;
        } catch (Exception e) {
            logger.error("Error assigning action point {} to user {}: {}", id, assignedTo, e.getMessage(), e);
            throw new RuntimeException("Failed to assign action point", e);
        }
    }
    
    // Complete Action Point
    public ActionPoint completeActionPoint(String id, String completedBy, String notes) {
        try {
            logger.info("Completing action point: {}", id);
            
            Optional<ActionPoint> existingOpt = actionPointRepository.findById(id);
            if (!existingOpt.isPresent()) {
                throw new RuntimeException("Action point not found with ID: " + id);
            }
            
            ActionPoint existing = existingOpt.get();
            existing.markAsCompleted(completedBy);
            
            if (notes != null && !notes.trim().isEmpty()) {
                String existingNotes = existing.getHumanNotes();
                if (existingNotes != null && !existingNotes.trim().isEmpty()) {
                    existing.setHumanNotes(existingNotes + "\n\nCompletion notes: " + notes);
                } else {
                    existing.setHumanNotes("Completion notes: " + notes);
                }
            }
            
            ActionPoint saved = actionPointRepository.save(existing);
            logger.info("Successfully completed action point: {}", id);
            
            return saved;
        } catch (Exception e) {
            logger.error("Error completing action point {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to complete action point", e);
        }
    }
    
    // Approve Action Point
    public ActionPoint approveActionPoint(String id, String approvedBy, String notes) {
        try {
            logger.info("Approving action point: {}", id);
            
            Optional<ActionPoint> existingOpt = actionPointRepository.findById(id);
            if (!existingOpt.isPresent()) {
                throw new RuntimeException("Action point not found with ID: " + id);
            }
            
            ActionPoint existing = existingOpt.get();
            existing.setStatus(ActionPoint.ActionStatus.IN_PROGRESS);
            existing.setUpdatedAt(LocalDateTime.now());
            
            if (notes != null && !notes.trim().isEmpty()) {
                String existingNotes = existing.getHumanNotes();
                if (existingNotes != null && !existingNotes.trim().isEmpty()) {
                    existing.setHumanNotes(existingNotes + "\n\nApproval notes: " + notes);
                } else {
                    existing.setHumanNotes("Approval notes: " + notes);
                }
            }
            
            ActionPoint saved = actionPointRepository.save(existing);
            logger.info("Successfully approved action point: {}", id);
            
            return saved;
        } catch (Exception e) {
            logger.error("Error approving action point {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to approve action point", e);
        }
    }
    
    // Reject Action Point
    public ActionPoint rejectActionPoint(String id, String reason) {
        try {
            logger.info("Rejecting action point: {}", id);
            
            Optional<ActionPoint> existingOpt = actionPointRepository.findById(id);
            if (!existingOpt.isPresent()) {
                throw new RuntimeException("Action point not found with ID: " + id);
            }
            
            ActionPoint existing = existingOpt.get();
            existing.setStatus(ActionPoint.ActionStatus.CANCELLED);
            existing.setUpdatedAt(LocalDateTime.now());
            
            if (reason != null && !reason.trim().isEmpty()) {
                String existingNotes = existing.getHumanNotes();
                if (existingNotes != null && !existingNotes.trim().isEmpty()) {
                    existing.setHumanNotes(existingNotes + "\n\nRejection reason: " + reason);
                } else {
                    existing.setHumanNotes("Rejection reason: " + reason);
                }
            }
            
            ActionPoint saved = actionPointRepository.save(existing);
            logger.info("Successfully rejected action point: {}", id);
            
            return saved;
        } catch (Exception e) {
            logger.error("Error rejecting action point {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to reject action point", e);
        }
    }
    
    // Get Action Points by Status
    public List<ActionPoint> getActionPointsByStatus(ActionPoint.ActionStatus status) {
        try {
            return actionPointRepository.findByStatus(status);
        } catch (Exception e) {
            logger.error("Error retrieving action points by status {}: {}", status, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    // Get Action Points by Assigned User
    public List<ActionPoint> getActionPointsByAssignee(String assignee) {
        try {
            return actionPointRepository.findByAssignedTo(assignee);
        } catch (Exception e) {
            logger.error("Error retrieving action points for assignee {}: {}", assignee, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    // Get Action Points by Priority
    public List<ActionPoint> getActionPointsByPriority(ActionPoint.Priority priority) {
        try {
            return actionPointRepository.findByPriority(priority);
        } catch (Exception e) {
            logger.error("Error retrieving action points by priority {}: {}", priority, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    // Search Action Points
    public List<ActionPoint> searchActionPoints(String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return new ArrayList<>();
            }
            return actionPointRepository.searchByTitleOrDescription(query.trim());
        } catch (Exception e) {
            logger.error("Error searching action points with query '{}': {}", query, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    // Get Action Points related to Alert
    public List<ActionPoint> getActionPointsForAlert(String alertId) {
        try {
            return actionPointRepository.findByRelatedAlertId(alertId);
        } catch (Exception e) {
            logger.error("Error retrieving action points for alert {}: {}", alertId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    // Auto-trigger actions for alerts
    public void triggerActionsForAlert(Alert alert) {
        try {
            logger.info("Auto-triggering actions for alert: {}", alert.getId());
            
            // Critical alerts get immediate action points
            if (alert.getSeverity() == Alert.Severity.CRITICAL) {
                createCriticalAlertActions(alert);
            }
            // High priority alerts get investigation action points
            else if (alert.getSeverity() == Alert.Severity.HIGH) {
                createHighPriorityAlertActions(alert);
            }
            // Medium priority alerts get monitoring action points
            else if (alert.getSeverity() == Alert.Severity.MEDIUM) {
                createMediumPriorityAlertActions(alert);
            }
            
        } catch (Exception e) {
            logger.error("Error auto-triggering actions for alert {}: {}", alert.getId(), e.getMessage(), e);
        }
    }
    
    private void createCriticalAlertActions(Alert alert) {
        // Create investigation action
        ActionPoint investigation = new ActionPoint(
            "Critical Alert Investigation: " + alert.getTitle(),
            "Immediate investigation required for critical security alert: " + alert.getDescription(),
            ActionPoint.ActionType.INVESTIGATE,
            ActionPoint.Priority.CRITICAL,
            "System Auto-Trigger"
        );
        investigation.setRelatedAlertId(alert.getId());
        investigation.setHumanApprovalRequired(true);
        investigation.setAutoTriggered(true);
        investigation.setActions(Arrays.asList(
            createActionItem("Immediate security analyst review", true, true),
            createActionItem("Conduct comprehensive threat assessment", false, true)
        ));
        createActionPoint(investigation);
        
        // Create escalation action
        ActionPoint escalation = new ActionPoint(
            "Escalate Critical Alert: " + alert.getTitle(),
            "Escalate critical alert to senior security team and management",
            ActionPoint.ActionType.ESCALATE,
            ActionPoint.Priority.CRITICAL,
            "System Auto-Trigger"
        );
        escalation.setRelatedAlertId(alert.getId());
        escalation.setHumanApprovalRequired(true);
        escalation.setAutoTriggered(true);
        escalation.setActions(Arrays.asList(
            createActionItem("Notify senior management of critical threat", true, true)
        ));
        createActionPoint(escalation);
    }
    
    private void createHighPriorityAlertActions(Alert alert) {
        ActionPoint investigation = new ActionPoint(
            "High Priority Alert Investigation: " + alert.getTitle(),
            "Investigate high priority security alert: " + alert.getDescription(),
            ActionPoint.ActionType.INVESTIGATE,
            ActionPoint.Priority.HIGH,
            "System Auto-Trigger"
        );
        investigation.setRelatedAlertId(alert.getId());
        investigation.setHumanApprovalRequired(false);
        investigation.setAutoTriggered(true);
        investigation.setActions(Arrays.asList(
            createActionItem("Conduct preliminary threat analysis", false, true)
        ));
        createActionPoint(investigation);
    }
    
    private void createMediumPriorityAlertActions(Alert alert) {
        ActionPoint monitoring = new ActionPoint(
            "Monitor Alert: " + alert.getTitle(),
            "Monitor medium priority alert for escalation: " + alert.getDescription(),
            ActionPoint.ActionType.MONITOR,
            ActionPoint.Priority.MEDIUM,
            "System Auto-Trigger"
        );
        monitoring.setRelatedAlertId(alert.getId());
        monitoring.setHumanApprovalRequired(false);
        monitoring.setAutoTriggered(true);
        monitoring.setActions(Arrays.asList(
            createActionItem("Set up continuous monitoring for this alert", false, true)
        ));
        createActionPoint(monitoring);
    }
    
    private ActionItem createActionItem(String description, boolean requiresHumanApproval, boolean aiSuggested) {
        ActionItem item = new ActionItem(description, requiresHumanApproval, aiSuggested);
        item.setId(UUID.randomUUID().toString());
        return item;
    }
    
    private void handleAutoTriggeredAction(ActionPoint actionPoint) {
        // Additional logic for auto-triggered actions
        logger.info("Handling auto-triggered action point: {}", actionPoint.getId());
        
        // Could send notifications, update related entities, etc.
        if (actionPoint.getRelatedAlertId() != null) {
            Optional<Alert> alertOpt = alertRepository.findById(actionPoint.getRelatedAlertId());
            if (alertOpt.isPresent()) {
                Alert alert = alertOpt.get();
                logger.info("Auto-triggered action {} related to alert {}", 
                    actionPoint.getId(), alert.getId());
                // Could update alert status or add metadata
            }
        }
    }
    
    // Get dashboard summary for action points
    public Map<String, Object> getActionDashboardSummary() {
        try {
            Map<String, Object> summary = new HashMap<>();
            
            // Total counts
            summary.put("totalActionPoints", actionPointRepository.count());
            summary.put("pendingActions", actionPointRepository.countByStatus(ActionPoint.ActionStatus.PENDING));
            summary.put("inProgressActions", actionPointRepository.countByStatus(ActionPoint.ActionStatus.IN_PROGRESS));
            summary.put("completedActions", actionPointRepository.countByStatus(ActionPoint.ActionStatus.COMPLETED));
            
            // Priority breakdown
            summary.put("criticalActions", actionPointRepository.countByPriority(ActionPoint.Priority.CRITICAL));
            summary.put("highActions", actionPointRepository.countByPriority(ActionPoint.Priority.HIGH));
            summary.put("mediumActions", actionPointRepository.countByPriority(ActionPoint.Priority.MEDIUM));
            summary.put("lowActions", actionPointRepository.countByPriority(ActionPoint.Priority.LOW));
            
            // Overdue actions
            summary.put("overdueActions", actionPointRepository.findOverdueActions(LocalDateTime.now()).size());
            
            // Auto-triggered actions
            summary.put("autoTriggeredActions", actionPointRepository.findByAutoTriggered(true).size());
            
            // Actions requiring human approval
            summary.put("actionsNeedingApproval", actionPointRepository.findByHumanApprovalRequired(true).size());
            
            return summary;
        } catch (Exception e) {
            logger.error("Error generating action dashboard summary: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
    // Get recent action points
    public List<ActionPoint> getRecentActionPoints(int limit) {
        try {
            return actionPointRepository.findActionsCreatedSince(LocalDateTime.now().minusDays(7))
                    .stream()
                    .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error retrieving recent action points: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}

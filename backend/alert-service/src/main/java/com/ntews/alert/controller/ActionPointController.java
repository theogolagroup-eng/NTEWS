package com.ntews.alert.controller;

import com.ntews.alert.model.ActionPoint;
import com.ntews.alert.model.Alert;
import com.ntews.alert.service.ActionPointService;
import com.ntews.alert.service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/action-points")
@CrossOrigin(origins = "*")
public class ActionPointController {
    
    private static final Logger logger = LoggerFactory.getLogger(ActionPointController.class);
    
    @Autowired
    private ActionPointService actionPointService;
    
    @Autowired
    private AlertService alertService;
    
    // Test endpoint
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        try {
            logger.info("ActionPoints controller is working!");
            return ResponseEntity.ok(Map.of("status", "ActionPoints controller is working!"));
        } catch (Exception e) {
            logger.error("Error in test endpoint: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    // Test endpoint without dependencies
    @GetMapping("/test-simple")
    public ResponseEntity<Map<String, String>> testSimple() {
        try {
            logger.info("Simple test endpoint is working!");
            return ResponseEntity.ok(Map.of("status", "Simple test endpoint is working!"));
        } catch (Exception e) {
            logger.error("Error in simple test endpoint: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    // Create Action Point
    @PostMapping
    public ResponseEntity<ActionPoint> createActionPoint(@RequestBody ActionPoint actionPoint) {
        try {
            logger.info("REST request to create action point: {}", actionPoint.getTitle());
            logger.debug("ActionPoint data received: {}", actionPoint);
            
            ActionPoint created = actionPointService.createActionPoint(actionPoint);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            logger.error("Error creating action point: {}", e.getMessage(), e);
            logger.error("ActionPoint that caused error: {}", actionPoint);
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Update Action Point
    @PutMapping("/{id}")
    public ResponseEntity<ActionPoint> updateActionPoint(
            @PathVariable String id, 
            @RequestBody ActionPoint updates) {
        try {
            logger.info("REST request to update action point: {}", id);
            
            ActionPoint updated = actionPointService.updateActionPoint(id, updates);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error updating action point {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Get Action Point by ID
    @GetMapping("/{id}")
    public ResponseEntity<ActionPoint> getActionPoint(@PathVariable String id) {
        try {
            logger.info("REST request to get action point: {}", id);
            
            Optional<ActionPoint> actionPoint = actionPointService.getActionPointById(id);
            return actionPoint.map(ResponseEntity::ok)
                              .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error retrieving action point {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Get All Action Points
    @GetMapping
    public ResponseEntity<List<ActionPoint>> getAllActionPoints() {
        try {
            logger.info("REST request to get all action points");
            
            List<ActionPoint> actionPoints = actionPointService.getAllActionPoints();
            return ResponseEntity.ok(actionPoints);
        } catch (Exception e) {
            logger.error("Error retrieving all action points: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Delete Action Point
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActionPoint(@PathVariable String id) {
        try {
            logger.info("REST request to delete action point: {}", id);
            
            boolean deleted = actionPointService.deleteActionPoint(id);
            return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting action point {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Assign Action Point
    @PostMapping("/{id}/assign")
    public ResponseEntity<ActionPoint> assignActionPoint(
            @PathVariable String id,
            @RequestParam String assignedTo) {
        try {
            logger.info("REST request to assign action point {} to user: {}", id, assignedTo);
            
            ActionPoint assigned = actionPointService.assignActionPoint(id, assignedTo);
            return ResponseEntity.ok(assigned);
        } catch (Exception e) {
            logger.error("Error assigning action point {} to user {}: {}", id, assignedTo, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Complete Action Point
    @PostMapping("/{id}/complete")
    public ResponseEntity<ActionPoint> completeActionPoint(
            @PathVariable String id,
            @RequestParam(required = false) String completedBy,
            @RequestBody(required = false) Map<String, String> requestBody) {
        try {
            logger.info("REST request to complete action point: {}", id);
            
            String notes = null;
            String by = completedBy;
            
            if (requestBody != null) {
                notes = requestBody.get("notes");
                if (by == null) {
                    by = requestBody.get("completedBy");
                }
            }
            
            if (by == null) {
                by = "System Analyst";
            }
            
            ActionPoint completed = actionPointService.completeActionPoint(id, by, notes);
            return ResponseEntity.ok(completed);
        } catch (Exception e) {
            logger.error("Error completing action point {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Approve Action Point
    @PostMapping("/{id}/approve")
    public ResponseEntity<ActionPoint> approveActionPoint(
            @PathVariable String id,
            @RequestParam(required = false) String approvedBy,
            @RequestBody(required = false) Map<String, String> requestBody) {
        try {
            logger.info("REST request to approve action point: {}", id);
            
            String notes = null;
            String by = approvedBy;
            
            if (requestBody != null) {
                notes = requestBody.get("notes");
                if (by == null) {
                    by = requestBody.get("approvedBy");
                }
            }
            
            if (by == null) {
                by = "System Analyst";
            }
            
            ActionPoint approved = actionPointService.approveActionPoint(id, by, notes);
            return ResponseEntity.ok(approved);
        } catch (Exception e) {
            logger.error("Error approving action point {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Reject Action Point
    @PostMapping("/{id}/reject")
    public ResponseEntity<ActionPoint> rejectActionPoint(
            @PathVariable String id,
            @RequestParam(required = false) String reason,
            @RequestBody(required = false) Map<String, String> requestBody) {
        try {
            logger.info("REST request to reject action point: {}", id);
            
            String rejectionReason = reason;
            
            if (requestBody != null && rejectionReason == null) {
                rejectionReason = requestBody.get("reason");
            }
            
            if (rejectionReason == null) {
                rejectionReason = "No reason provided";
            }
            
            ActionPoint rejected = actionPointService.rejectActionPoint(id, rejectionReason);
            return ResponseEntity.ok(rejected);
        } catch (Exception e) {
            logger.error("Error rejecting action point {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Get Action Points by Status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ActionPoint>> getActionPointsByStatus(@PathVariable String status) {
        try {
            logger.info("REST request to get action points by status: {}", status);
            
            ActionPoint.ActionStatus actionStatus;
            try {
                actionStatus = ActionPoint.ActionStatus.fromValue(status);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
            
            List<ActionPoint> actionPoints = actionPointService.getActionPointsByStatus(actionStatus);
            return ResponseEntity.ok(actionPoints);
        } catch (Exception e) {
            logger.error("Error retrieving action points by status {}: {}", status, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Get Action Points by Assignee
    @GetMapping("/assigned/{assignee}")
    public ResponseEntity<List<ActionPoint>> getActionPointsByAssignee(@PathVariable String assignee) {
        try {
            logger.info("REST request to get action points for assignee: {}", assignee);
            
            List<ActionPoint> actionPoints = actionPointService.getActionPointsByAssignee(assignee);
            return ResponseEntity.ok(actionPoints);
        } catch (Exception e) {
            logger.error("Error retrieving action points for assignee {}: {}", assignee, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Get Action Points by Priority
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<ActionPoint>> getActionPointsByPriority(@PathVariable String priority) {
        try {
            logger.info("REST request to get action points by priority: {}", priority);
            
            ActionPoint.Priority actionPriority;
            try {
                actionPriority = ActionPoint.Priority.fromValue(priority);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
            
            List<ActionPoint> actionPoints = actionPointService.getActionPointsByPriority(actionPriority);
            return ResponseEntity.ok(actionPoints);
        } catch (Exception e) {
            logger.error("Error retrieving action points by priority {}: {}", priority, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Search Action Points
    @GetMapping("/search")
    public ResponseEntity<List<ActionPoint>> searchActionPoints(@RequestParam String q) {
        try {
            logger.info("REST request to search action points with query: {}", q);
            
            List<ActionPoint> actionPoints = actionPointService.searchActionPoints(q);
            return ResponseEntity.ok(actionPoints);
        } catch (Exception e) {
            logger.error("Error searching action points with query '{}': {}", q, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Get Action Points for Alert
    @GetMapping("/alert/{alertId}")
    public ResponseEntity<List<ActionPoint>> getActionPointsForAlert(@PathVariable String alertId) {
        try {
            logger.info("REST request to get action points for alert: {}", alertId);
            
            List<ActionPoint> actionPoints = actionPointService.getActionPointsForAlert(alertId);
            return ResponseEntity.ok(actionPoints);
        } catch (Exception e) {
            logger.error("Error retrieving action points for alert {}: {}", alertId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Trigger Actions for Alert
    @PostMapping("/alert/{alertId}/trigger")
    public ResponseEntity<Void> triggerActionsForAlert(@PathVariable String alertId) {
        try {
            logger.info("REST request to trigger actions for alert: {}", alertId);
            
            // Get the alert and trigger actions
            Optional<Alert> alertOpt = Optional.ofNullable(alertService.getAlertById(alertId));
            if (alertOpt.isPresent()) {
                Alert alert = alertOpt.get();
                actionPointService.triggerActionsForAlert(alert);
            }
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error triggering actions for alert {}: {}", alertId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Get Recent Action Points
    @GetMapping("/recent")
    public ResponseEntity<List<ActionPoint>> getRecentActionPoints(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            logger.info("REST request to get recent action points with limit: {}", limit);
            
            List<ActionPoint> recentActions = actionPointService.getRecentActionPoints(limit);
            return ResponseEntity.ok(recentActions);
        } catch (Exception e) {
            logger.error("Error retrieving recent action points: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Generate AI Recommendation
    @PostMapping("/{id}/ai-recommendation")
    public ResponseEntity<ActionPoint> generateAIRecommendation(@PathVariable String id) {
        try {
            logger.info("REST request to generate AI recommendation for action point: {}", id);
            
            Optional<ActionPoint> actionPointOpt = actionPointService.getActionPointById(id);
            if (!actionPointOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            ActionPoint actionPoint = actionPointOpt.get();
            
            // Mock AI recommendation - in real system, this would call AI service
            String[] recommendations = {
                "Escalate to senior security analyst due to critical nature",
                "Initiate immediate incident response protocol",
                "Conduct thorough forensic analysis of affected systems",
                "Implement temporary security measures while investigating",
                "Coordinate with external security agencies if required"
            };
            
            String recommendation = recommendations[(int) (Math.random() * recommendations.length)];
            actionPoint.setAiRecommendation(recommendation);
            
            ActionPoint updated = actionPointService.updateActionPoint(id, actionPoint);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error generating AI recommendation for action point {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Apply AI Recommendation
    @PostMapping("/{id}/apply-ai-recommendation")
    public ResponseEntity<ActionPoint> applyAIRecommendation(@PathVariable String id) {
        try {
            logger.info("REST request to apply AI recommendation for action point: {}", id);
            
            Optional<ActionPoint> actionPointOpt = actionPointService.getActionPointById(id);
            if (!actionPointOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            ActionPoint actionPoint = actionPointOpt.get();
            
            if (actionPoint.getAiRecommendation() == null) {
                return ResponseEntity.badRequest().build();
            }
            
            // Apply the AI recommendation by setting status to pending for human review
            actionPoint.setStatus(ActionPoint.ActionStatus.PENDING);
            
            ActionPoint updated = actionPointService.updateActionPoint(id, actionPoint);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error applying AI recommendation for action point {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Trigger Actions for Intelligence Report
    @PostMapping("/intelligence/{intelligenceId}/trigger")
    public ResponseEntity<Void> triggerActionsForIntelligence(@PathVariable String intelligenceId) {
        try {
            logger.info("REST request to trigger actions for intelligence: {}", intelligenceId);
            
            // Mock implementation - in real system, this would fetch intelligence and trigger appropriate actions
            // For now, we'll create a sample action point
            ActionPoint actionPoint = new ActionPoint(
                "Intelligence Analysis Required",
                "Analyze intelligence report and take appropriate actions",
                ActionPoint.ActionType.INVESTIGATE,
                ActionPoint.Priority.HIGH,
                "Intelligence Service"
            );
            actionPoint.setRelatedThreatId(intelligenceId);
            actionPoint.setAutoTriggered(true);
            actionPoint.setHumanApprovalRequired(true);
            
            actionPointService.createActionPoint(actionPoint);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error triggering actions for intelligence {}: {}", intelligenceId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Trigger Actions for Hotspot
    @PostMapping("/hotspot/{hotspotId}/trigger")
    public ResponseEntity<Void> triggerActionsForHotspot(@PathVariable String hotspotId) {
        try {
            logger.info("REST request to trigger actions for hotspot: {}", hotspotId);
            
            // Mock implementation - in real system, this would fetch hotspot and trigger appropriate actions
            // For now, we'll create a sample action point
            ActionPoint actionPoint = new ActionPoint(
                "Hotspot Monitoring Required",
                "Monitor high-probability threat hotspot and implement countermeasures",
                ActionPoint.ActionType.MONITOR,
                ActionPoint.Priority.HIGH,
                "Prediction Service"
            );
            actionPoint.setRelatedHotspotId(hotspotId);
            actionPoint.setAutoTriggered(true);
            actionPoint.setHumanApprovalRequired(true);
            
            actionPointService.createActionPoint(actionPoint);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error triggering actions for hotspot {}: {}", hotspotId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Dashboard Summary
    @GetMapping("/dashboard/summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        try {
            logger.info("REST request to get action points dashboard summary");
            
            Map<String, Object> summary = actionPointService.getDashboardSummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            logger.error("Error getting dashboard summary: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

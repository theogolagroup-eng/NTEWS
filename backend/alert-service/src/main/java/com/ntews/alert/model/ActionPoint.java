package com.ntews.alert.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "action_points")
public class ActionPoint {
    
    @Id
    private String id;
    
    @Field("title")
    private String title;
    
    @Field("description")
    private String description;
    
    @Field("type")
    private ActionType type;
    
    @Field("priority")
    private Priority priority;
    
    @Field("status")
    private ActionStatus status;
    
    @Field("assigned_to")
    private String assignedTo;
    
    @Field("created_by")
    private String createdBy;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("due_date")
    private LocalDateTime dueDate;
    
    @Field("related_alert_id")
    private String relatedAlertId;
    
    @Field("related_threat_id")
    private String relatedThreatId;
    
    @Field("related_hotspot_id")
    private String relatedHotspotId;
    
    @Field("actions")
    private List<ActionItem> actions;
    
    @Field("human_approval_required")
    private Boolean humanApprovalRequired;
    
    @Field("auto_triggered")
    private Boolean autoTriggered;
    
    @Field("ai_recommendation")
    private String aiRecommendation;
    
    @Field("human_notes")
    private String humanNotes;
    
    @Field("workflow_id")
    private String workflowId;
    
    @Field("escalation_level")
    private Integer escalationLevel;
    
    @Field("completion_percentage")
    private Double completionPercentage;
    
    // Enums
    public enum ActionType {
        INVESTIGATE("investigate"),
        ACKNOWLEDGE("acknowledge"),
        ESCALATE("escalate"),
        RESOLVE("resolve"),
        MONITOR("monitor"),
        REPORT("report");
        
        private final String value;
        
        ActionType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        @JsonCreator
        public static ActionType fromValue(String value) {
            for (ActionType type : ActionType.values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown ActionType: " + value);
        }
        
        @JsonProperty("type")
        public String toJson() {
            return value;
        }
    }
    
    public enum Priority {
        LOW("low"),
        MEDIUM("medium"),
        HIGH("high"),
        CRITICAL("critical");
        
        private final String value;
        
        Priority(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        @JsonCreator
        public static Priority fromValue(String value) {
            for (Priority priority : Priority.values()) {
                if (priority.value.equals(value)) {
                    return priority;
                }
            }
            throw new IllegalArgumentException("Unknown Priority: " + value);
        }
        
        @JsonProperty("priority")
        public String toJson() {
            return value;
        }
    }
    
    public enum ActionStatus {
        PENDING("pending"),
        IN_PROGRESS("in_progress"),
        COMPLETED("completed"),
        CANCELLED("cancelled");
        
        private final String value;
        
        ActionStatus(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        @JsonCreator
        public static ActionStatus fromValue(String value) {
            for (ActionStatus status : ActionStatus.values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown ActionStatus: " + value);
        }
        
        @JsonProperty("status")
        public String toJson() {
            return value;
        }
    }
    
    // Constructors
    public ActionPoint() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = ActionStatus.PENDING;
        this.escalationLevel = 0;
        this.completionPercentage = 0.0;
    }
    
    public ActionPoint(String title, String description, ActionType type, Priority priority, String createdBy) {
        this();
        this.title = title;
        this.description = description;
        this.type = type;
        this.priority = priority;
        this.createdBy = createdBy;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public ActionType getType() {
        return type;
    }
    
    public void setType(ActionType type) {
        this.type = type;
    }
    
    public Priority getPriority() {
        return priority;
    }
    
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
    public ActionStatus getStatus() {
        return status;
    }
    
    public void setStatus(ActionStatus status) {
        this.status = status;
    }
    
    public String getAssignedTo() {
        return assignedTo;
    }
    
    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
    
    public String getRelatedAlertId() {
        return relatedAlertId;
    }
    
    public void setRelatedAlertId(String relatedAlertId) {
        this.relatedAlertId = relatedAlertId;
    }
    
    public String getRelatedThreatId() {
        return relatedThreatId;
    }
    
    public void setRelatedThreatId(String relatedThreatId) {
        this.relatedThreatId = relatedThreatId;
    }
    
    public String getRelatedHotspotId() {
        return relatedHotspotId;
    }
    
    public void setRelatedHotspotId(String relatedHotspotId) {
        this.relatedHotspotId = relatedHotspotId;
    }
    
    public List<ActionItem> getActions() {
        return actions;
    }
    
    public void setActions(List<ActionItem> actions) {
        this.actions = actions;
    }
    
    public Boolean getHumanApprovalRequired() {
        return humanApprovalRequired;
    }
    
    public void setHumanApprovalRequired(Boolean humanApprovalRequired) {
        this.humanApprovalRequired = humanApprovalRequired;
    }
    
    public Boolean getAutoTriggered() {
        return autoTriggered;
    }
    
    public void setAutoTriggered(Boolean autoTriggered) {
        this.autoTriggered = autoTriggered;
    }
    
    public String getAiRecommendation() {
        return aiRecommendation;
    }
    
    public void setAiRecommendation(String aiRecommendation) {
        this.aiRecommendation = aiRecommendation;
    }
    
    public String getHumanNotes() {
        return humanNotes;
    }
    
    public void setHumanNotes(String humanNotes) {
        this.humanNotes = humanNotes;
    }
    
    public String getWorkflowId() {
        return workflowId;
    }
    
    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }
    
    public Integer getEscalationLevel() {
        return escalationLevel;
    }
    
    public void setEscalationLevel(Integer escalationLevel) {
        this.escalationLevel = escalationLevel;
    }
    
    public Double getCompletionPercentage() {
        return completionPercentage;
    }
    
    public void setCompletionPercentage(Double completionPercentage) {
        this.completionPercentage = completionPercentage;
    }
    
    // Helper method to update completion percentage
    public void updateCompletionPercentage() {
        if (actions == null || actions.isEmpty()) {
            this.completionPercentage = 0.0;
            return;
        }
        
        long completedCount = actions.stream()
            .mapToLong(action -> action.isCompleted() ? 1 : 0)
            .sum();
        
        this.completionPercentage = (double) completedCount / actions.size() * 100;
    }
    
    // Helper method to mark as completed
    public void markAsCompleted(String completedBy) {
        this.status = ActionStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
        if (actions != null) {
            actions.forEach(action -> {
                if (!action.isCompleted()) {
                    action.setCompleted(true);
                    action.setCompletedBy(completedBy);
                    action.setCompletedAt(LocalDateTime.now());
                }
            });
        }
        updateCompletionPercentage();
    }
}

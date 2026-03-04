package com.ntews.alert.model;

import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

public class ActionItem {
    
    @Field("id")
    private String id;
    
    @Field("description")
    private String description;
    
    @Field("completed")
    private boolean completed;
    
    @Field("completed_by")
    private String completedBy;
    
    @Field("completed_at")
    private LocalDateTime completedAt;
    
    @Field("requires_human_approval")
    private boolean requiresHumanApproval;
    
    @Field("ai_suggested")
    private boolean aiSuggested;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public ActionItem() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.completed = false;
    }
    
    public ActionItem(String description, boolean requiresHumanApproval, boolean aiSuggested) {
        this();
        this.description = description;
        this.requiresHumanApproval = requiresHumanApproval;
        this.aiSuggested = aiSuggested;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getCompletedBy() {
        return completedBy;
    }
    
    public void setCompletedBy(String completedBy) {
        this.completedBy = completedBy;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public boolean isRequiresHumanApproval() {
        return requiresHumanApproval;
    }
    
    public void setRequiresHumanApproval(boolean requiresHumanApproval) {
        this.requiresHumanApproval = requiresHumanApproval;
    }
    
    public boolean isAiSuggested() {
        return aiSuggested;
    }
    
    public void setAiSuggested(boolean aiSuggested) {
        this.aiSuggested = aiSuggested;
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
    
    // Helper method to mark as completed
    public void markAsCompleted(String completedBy) {
        this.completed = true;
        this.completedBy = completedBy;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}

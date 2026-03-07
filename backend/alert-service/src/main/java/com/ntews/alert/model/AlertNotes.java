package com.ntews.alert.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "alert_notes")
public class AlertNotes {
    
    @Id
    private String id;
    
    @Field("alert_id")
    private String alertId;
    
    @Field("notes_type")
    private NotesType notesType;
    
    @Field("notes")
    private String notes;
    
    @Field("assigned_to")
    private String assignedTo;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("last_modified")
    private LocalDateTime lastModified;
    
    @Field("created_by")
    private String createdBy;
    
    @Field("modified_by")
    private String modifiedBy;
    
    public enum NotesType {
        ASSIGNMENT("assignment"),
        RESOLUTION("resolution");
        
        private final String value;
        
        NotesType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
}

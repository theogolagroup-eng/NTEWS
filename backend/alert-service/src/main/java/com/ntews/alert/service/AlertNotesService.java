package com.ntews.alert.service;

import com.ntews.alert.model.AlertNotes;
import com.ntews.alert.model.AlertNotes.NotesType;
import com.ntews.alert.repository.AlertNotesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertNotesService {
    
    private final AlertNotesRepository alertNotesRepository;
    
    public AlertNotes saveNotes(String alertId, NotesType notesType, String notes, String assignedTo, String userId) {
        log.info("Saving {} notes for alert: {}", notesType, alertId);
        
        // Check if notes already exist for this alert and type
        Optional<AlertNotes> existingNotes = alertNotesRepository.findByAlertIdAndNotesType(alertId, notesType);
        
        AlertNotes alertNotes;
        if (existingNotes.isPresent()) {
            // Update existing notes
            alertNotes = existingNotes.get();
            alertNotes.setNotes(notes);
            alertNotes.setLastModified(LocalDateTime.now());
            alertNotes.setModifiedBy(userId);
            if (assignedTo != null && !assignedTo.trim().isEmpty()) {
                alertNotes.setAssignedTo(assignedTo);
            }
        } else {
            // Create new notes
            alertNotes = AlertNotes.builder()
                    .alertId(alertId)
                    .notesType(notesType)
                    .notes(notes)
                    .assignedTo(assignedTo)
                    .createdAt(LocalDateTime.now())
                    .lastModified(LocalDateTime.now())
                    .createdBy(userId)
                    .modifiedBy(userId)
                    .build();
        }
        
        return alertNotesRepository.save(alertNotes);
    }
    
    public Optional<AlertNotes> getNotes(String alertId, NotesType notesType) {
        log.debug("Retrieving {} notes for alert: {}", notesType, alertId);
        return alertNotesRepository.findByAlertIdAndNotesType(alertId, notesType);
    }
    
    public List<AlertNotes> getAllNotesForAlert(String alertId) {
        log.debug("Retrieving all notes for alert: {}", alertId);
        return alertNotesRepository.findByAlertId(alertId);
    }
    
    public void deleteNotes(String alertId, NotesType notesType) {
        log.info("Deleting {} notes for alert: {}", notesType, alertId);
        alertNotesRepository.deleteByAlertIdAndNotesType(alertId, notesType);
    }
    
    public void deleteAllNotesForAlert(String alertId) {
        log.info("Deleting all notes for alert: {}", alertId);
        alertNotesRepository.deleteByAlertId(alertId);
    }
}

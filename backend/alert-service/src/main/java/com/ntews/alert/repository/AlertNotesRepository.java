package com.ntews.alert.repository;

import com.ntews.alert.model.AlertNotes;
import com.ntews.alert.model.AlertNotes.NotesType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertNotesRepository extends MongoRepository<AlertNotes, String> {
    
    Optional<AlertNotes> findByAlertIdAndNotesType(String alertId, NotesType notesType);
    
    List<AlertNotes> findByAlertId(String alertId);
    
    List<AlertNotes> findByNotesType(NotesType notesType);
    
    @Query(value = "{ 'alertId' : ?0, 'notesType' : ?1 }", delete = true)
    void deleteByAlertIdAndNotesType(String alertId, NotesType notesType);
    
    @Query(value = "{ 'alertId' : ?0 }", delete = true)
    void deleteByAlertId(String alertId);
}

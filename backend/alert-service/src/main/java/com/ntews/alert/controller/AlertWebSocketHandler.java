package com.ntews.alert.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;

@Component
public class AlertWebSocketHandler implements WebSocketHandler {
    
    private static final Logger log = LoggerFactory.getLogger(AlertWebSocketHandler.class);
    
    @Autowired
    private ApplicationContext applicationContext;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Plain WebSocket connection established: {}", session.getId());
        sessions.put(session.getId(), session);
        
        // Send recent alerts to new connection
        try {
            com.ntews.alert.service.AlertService alertService = applicationContext.getBean(com.ntews.alert.service.AlertService.class);
            List<com.ntews.alert.model.Alert> recentAlerts = alertService.getRecentAlerts(10);
            String message = objectMapper.writeValueAsString(Map.of(
                "type", "initial_data",
                "alerts", recentAlerts
            ));
            session.sendMessage(new TextMessage(message));
        } catch (Exception e) {
            log.error("Error sending initial data to WebSocket: {}", e.getMessage());
        }
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        log.info("Received WebSocket message from {}: {}", session.getId(), message.getPayload());
        
        try {
            Map<String, Object> messageData = objectMapper.readValue(message.getPayload().toString(), Map.class);
            String type = (String) messageData.get("type");
            
            if ("subscribe".equals(type)) {
                // Handle subscription request
                String response = objectMapper.writeValueAsString(Map.of(
                    "type", "subscription_confirmed",
                    "message", "Subscribed to alerts"
                ));
                session.sendMessage(new TextMessage(response));
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket message: {}", e.getMessage());
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
        sessions.remove(session.getId());
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("WebSocket connection closed: {} - {}", session.getId(), closeStatus);
        sessions.remove(session.getId());
    }
    
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    
    /**
     * Broadcast alert to all connected WebSocket clients
     */
    public void broadcastAlert(com.ntews.alert.model.Alert alert) {
        try {
            String message = objectMapper.writeValueAsString(Map.of(
                "type", "alert",
                "data", alert
            ));
            
            sessions.values().removeIf(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(message));
                        return false;
                    }
                } catch (Exception e) {
                    log.error("Error sending message to WebSocket session {}: {}", session.getId(), e.getMessage());
                }
                return true; // Remove closed sessions
            });
            
            log.info("Broadcasted alert {} to {} WebSocket clients", alert.getId(), sessions.size());
        } catch (Exception e) {
            log.error("Error broadcasting alert to WebSockets: {}", e.getMessage());
        }
    }
    
    /**
     * Broadcast system status to all connected clients
     */
    public void broadcastSystemStatus(String status) {
        try {
            String message = objectMapper.writeValueAsString(Map.of(
                "type", "system_status",
                "status", status
            ));
            
            sessions.values().removeIf(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(message));
                        return false;
                    }
                } catch (Exception e) {
                    log.error("Error sending status to WebSocket session {}: {}", session.getId(), e.getMessage());
                }
                return true;
            });
            
            log.info("Broadcasted system status to {} WebSocket clients", sessions.size());
        } catch (Exception e) {
            log.error("Error broadcasting system status to WebSockets: {}", e.getMessage());
        }
    }
}

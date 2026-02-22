package com.ntews.alert.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import com.ntews.alert.model.Alert;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class WebSocketController {
    
    private static final Logger log = LoggerFactory.getLogger(WebSocketController.class);
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    /**
     * Handle WebSocket connection requests
     */
    @MessageMapping("/connect")
    @SendTo("/topic/alerts")
    public String handleConnection(String message) {
        log.info("WebSocket connection established: {}", message);
        return "Connected to NTEWS Alert Service";
    }
    
    /**
     * Send real-time alerts to connected clients
     */
    public void sendRealTimeAlert(Alert alert) {
        try {
            messagingTemplate.convertAndSend("/topic/alerts", alert);
            log.info("Sent real-time alert: {}", alert.getId());
        } catch (Exception e) {
            log.error("Error sending real-time alert: {}", e.getMessage());
        }
    }
    
    /**
     * Broadcast system status updates
     */
    public void sendSystemStatus(String status) {
        try {
            messagingTemplate.convertAndSend("/topic/system-status", status);
            log.info("Sent system status: {}", status);
        } catch (Exception e) {
            log.error("Error sending system status: {}", e.getMessage());
        }
    }
    
    /**
     * Handle client subscription requests
     */
    @MessageMapping("/subscribe")
    @SendTo("/topic/alerts")
    public List<Alert> handleSubscription(String subscriptionType) {
        log.info("Client subscribed to: {}", subscriptionType);
        
        // Send recent alerts to new subscribers
        try {
            com.ntews.alert.service.AlertService alertService = applicationContext.getBean(com.ntews.alert.service.AlertService.class);
            return alertService.getRecentAlerts(10);
        } catch (Exception e) {
            log.error("Error getting recent alerts for subscription: {}", e.getMessage());
            return List.of();
        }
    }
}

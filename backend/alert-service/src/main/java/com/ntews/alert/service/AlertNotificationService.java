package com.ntews.alert.service;

import com.ntews.alert.model.Alert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertNotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    public void sendNotifications(Alert alert) {
        if (alert.getNotifications() != null) {
            List<String> channels = alert.getNotifications().getChannels();
            
            for (String channel : channels) {
                switch (channel) {
                    case "websocket":
                        sendWebSocketNotification(alert);
                        break;
                    case "email":
                        sendEmailNotification(alert);
                        break;
                    case "sms":
                        sendSMSNotification(alert);
                        break;
                    default:
                        log.warn("Unknown notification channel: {}", channel);
                }
            }
        }
    }
    
    private void sendWebSocketNotification(Alert alert) {
        try {
            // Send to general alerts topic
            messagingTemplate.convertAndSend("/topic/alerts", alert);
            
            // Send to severity-specific topic
            messagingTemplate.convertAndSend("/topic/alerts/" + alert.getSeverity().getValue(), alert);
            
            // Send to active alerts topic if alert is active
            if (alert.getStatus() == Alert.AlertStatus.ACTIVE) {
                messagingTemplate.convertAndSend("/topic/active-alerts", alert);
            }
            
            log.info("Sent WebSocket notification for alert: {}", alert.getId());
            
        } catch (Exception e) {
            log.error("Error sending WebSocket notification for alert {}: {}", alert.getId(), e.getMessage());
        }
    }
    
    private void sendEmailNotification(Alert alert) {
        try {
            // In a real implementation, you would use Spring Mail to send emails
            log.info("Sending email notification for alert: {} - {}", alert.getId(), alert.getTitle());
            
            // Mock email sending
            EmailNotification email = EmailNotification.builder()
                    .to(alert.getNotifications().getRecipients())
                    .subject("NTEWS Alert: " + alert.getTitle())
                    .body(buildEmailBody(alert))
                    .priority(alert.getSeverity().getValue())
                    .build();
            
            // Send email logic would go here
            log.debug("Email notification prepared: {}", email);
            
        } catch (Exception e) {
            log.error("Error sending email notification for alert {}: {}", alert.getId(), e.getMessage());
        }
    }
    
    private void sendSMSNotification(Alert alert) {
        try {
            // In a real implementation, you would integrate with an SMS service
            log.info("Sending SMS notification for alert: {} - {}", alert.getId(), alert.getTitle());
            
            // Mock SMS sending
            SMSNotification sms = SMSNotification.builder()
                    .to(alert.getNotifications().getRecipients())
                    .message(buildSMSMessage(alert))
                    .priority(alert.getSeverity().getValue())
                    .build();
            
            // Send SMS logic would go here
            log.debug("SMS notification prepared: {}", sms);
            
        } catch (Exception e) {
            log.error("Error sending SMS notification for alert {}: {}", alert.getId(), e.getMessage());
        }
    }
    
    public void sendDashboardUpdate(Map<String, Object> dashboardData) {
        try {
            messagingTemplate.convertAndSend("/topic/dashboard-updates", dashboardData);
            log.debug("Sent dashboard update");
        } catch (Exception e) {
            log.error("Error sending dashboard update: {}", e.getMessage());
        }
    }
    
    public void sendRiskForecastUpdate(Object forecastData) {
        try {
            messagingTemplate.convertAndSend("/topic/risk-forecasts", forecastData);
            log.debug("Sent risk forecast update");
        } catch (Exception e) {
            log.error("Error sending risk forecast update: {}", e.getMessage());
        }
    }
    
    private String buildEmailBody(Alert alert) {
        StringBuilder body = new StringBuilder();
        body.append("<html><body>");
        body.append("<h2>NTEWS Alert Notification</h2>");
        body.append("<p><strong>Title:</strong> ").append(alert.getTitle()).append("</p>");
        body.append("<p><strong>Severity:</strong> ").append(alert.getSeverity().getValue()).append("</p>");
        body.append("<p><strong>Priority:</strong> ").append(alert.getPriority().getValue()).append("</p>");
        body.append("<p><strong>Category:</strong> ").append(alert.getCategory()).append("</p>");
        body.append("<p><strong>Description:</strong> ").append(alert.getDescription()).append("</p>");
        body.append("<p><strong>Timestamp:</strong> ").append(alert.getTimestamp()).append("</p>");
        
        if (alert.getLocation() != null) {
            body.append("<p><strong>Location:</strong> ").append(alert.getLocation().getAddress()).append("</p>");
        }
        
        body.append("<p><strong>Confidence:</strong> ").append(String.format("%.2f%%", alert.getConfidence() * 100)).append("</p>");
        body.append("<p><strong>Threat Level:</strong> ").append(alert.getThreatLevel()).append("</p>");
        
        body.append("<hr>");
        body.append("<p>This is an automated alert from the NTEWS system.</p>");
        body.append("</body></html>");
        
        return body.toString();
    }
    
    private String buildSMSMessage(Alert alert) {
        StringBuilder message = new StringBuilder();
        message.append("NTEWS ALERT: ").append(alert.getTitle()).append("\n");
        message.append("Severity: ").append(alert.getSeverity().getValue()).append("\n");
        message.append("Priority: ").append(alert.getPriority().getValue()).append("\n");
        
        if (alert.getLocation() != null) {
            message.append("Location: ").append(alert.getLocation().getAddress()).append("\n");
        }
        
        message.append("Time: ").append(alert.getTimestamp()).append("\n");
        message.append("Confidence: ").append(String.format("%.0f%%", alert.getConfidence() * 100));
        
        return message.toString();
    }
    
    // DTOs for notifications
    public static class EmailNotification {
        private List<String> to;
        private String subject;
        private String body;
        private String priority;
        
        public static EmailNotificationBuilder builder() {
            return new EmailNotificationBuilder();
        }
        
        // Getters
        public List<String> getTo() { return to; }
        public String getSubject() { return subject; }
        public String getBody() { return body; }
        public String getPriority() { return priority; }
        
        public static class EmailNotificationBuilder {
            private List<String> to;
            private String subject;
            private String body;
            private String priority;
            
            public EmailNotificationBuilder to(List<String> to) { this.to = to; return this; }
            public EmailNotificationBuilder subject(String subject) { this.subject = subject; return this; }
            public EmailNotificationBuilder body(String body) { this.body = body; return this; }
            public EmailNotificationBuilder priority(String priority) { this.priority = priority; return this; }
            
            public EmailNotification build() {
                EmailNotification notification = new EmailNotification();
                notification.to = this.to;
                notification.subject = this.subject;
                notification.body = this.body;
                notification.priority = this.priority;
                return notification;
            }
        }
    }
    
    public static class SMSNotification {
        private List<String> to;
        private String message;
        private String priority;
        
        public static SMSNotificationBuilder builder() {
            return new SMSNotificationBuilder();
        }
        
        // Getters
        public List<String> getTo() { return to; }
        public String getMessage() { return message; }
        public String getPriority() { return priority; }
        
        public static class SMSNotificationBuilder {
            private List<String> to;
            private String message;
            private String priority;
            
            public SMSNotificationBuilder to(List<String> to) { this.to = to; return this; }
            public SMSNotificationBuilder message(String message) { this.message = message; return this; }
            public SMSNotificationBuilder priority(String priority) { this.priority = priority; return this; }
            
            public SMSNotification build() {
                SMSNotification notification = new SMSNotification();
                notification.to = this.to;
                notification.message = this.message;
                notification.priority = this.priority;
                return notification;
            }
        }
    }
}

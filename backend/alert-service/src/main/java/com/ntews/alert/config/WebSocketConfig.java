package com.ntews.alert.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.config.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;

/**
 * Enhanced WebSocket Configuration for Real-time Security Monitoring
 * Supports Sheng-aware security alerts with real-time bidirectional communication
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for WebSocket messaging
        config.enableSimpleBroker("/topic");
        
        // Set application destination prefix for client-to-server messages
        config.setApplicationDestinationPrefixes("/app");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register WebSocket endpoint for security monitoring
        registry.addEndpoint("/security-monitor")
                .setAllowedOriginPatterns("*") // Allow all origins for development
                .withSockJS();
        
        // Additional endpoint for high-frequency security alerts
        registry.addEndpoint("/security-alerts-stream")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}

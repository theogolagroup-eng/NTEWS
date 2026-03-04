package com.ntews.alert.model;



import lombok.Data;

import lombok.NoArgsConstructor;

import lombok.AllArgsConstructor;

import lombok.Builder;



import java.time.LocalDateTime;

import java.util.List;

import java.util.Map;



@Data

@NoArgsConstructor

@AllArgsConstructor

@Builder

public class Alert {

    private String id;

    private String title;

    private String description;

    private String summary;

    

    // Alert classification

    private AlertType type;

    private Severity severity;

    private Priority priority;

    private String category;

    

    // Source information

    private String sourceId;

    private String sourceType; // intelligence_report, risk_forecast, system

    private String source;

    

    // Temporal and spatial information

    private LocalDateTime timestamp;

    private LocalDateTime expiresAt;

    private LocationInfo location;

    private List<String> affectedAreas;

    

    // Alert content

    private Map<String, Object> content;

    private List<String> tags;

    private List<String> keywords;

    

    // Status and lifecycle

    private AlertStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String assignedTo;

    

    // NLP Analysis fields

    private Double nlpRiskScore;

    private String nlpClassification;

    private Double nlpConfidence;

    private List<String> threatKeywords;

    private Map<String, Object> sentimentScores;

    private List<String> nlpRecommendations;

    private Double combinedRiskScore;

    private String priorityRecommendation;

    private LocalDateTime nlpAnalyzedAt;

    

    // AI Engine integration fields

    private Double aiConfidence;

    private Double aiThreatScore;

    private String aiRiskLevel;

    private String aiAnalysis;

    private String aiKeywords;

    private String aiRecommendations;

    private Double aiUrgencyScore;

    private Double aiImpactScore;

    private String aiRiskFactors;

    private Integer timeToEscalate; // minutes

    

    // AI Prediction fields

    private String predictedSeverity;

    private Integer timeToCritical; // minutes

    private String evolutionTrend;

    

    // Escalation and notification

    private EscalationInfo escalation;

    private NotificationInfo notifications;

    

    // Verification and resolution

    private Boolean verified;

    private String verificationNotes;

    private LocalDateTime verifiedAt;

    private String verifiedBy;

    private Boolean resolved;

    private String resolutionNotes;

    private LocalDateTime resolvedAt;

    private String resolvedBy;

    

    // Metadata

    private Map<String, Object> metadata;

    private Double confidence;

    private String threatLevel;

    

    @Data

    @NoArgsConstructor

    @AllArgsConstructor

    @Builder

    public static class LocationInfo {

        private String latitude;

        private String longitude;

        private String city;

        private String region;

        private String country;

        private String address;

        private Double radius;  // Alert radius in meters

        private String geoHash;

    }

    

    public enum AlertType {

        THREAT_DETECTION("threat_detection"),

        RISK_FORECAST("risk_forecast"),

        SYSTEM_ALERT("system_alert"),

        ESCALATION("escalation"),

        RESOLUTION("resolution");

        

        private final String value;

        

        AlertType(String value) {

            this.value = value;

        }

        

        public String getValue() {

            return value;

        }

    }

    

    public enum Severity {

        LOW("low", 1),

        MEDIUM("medium", 2),

        HIGH("high", 3),

        CRITICAL("critical", 4);

        

        private final String value;

        private final int level;

        

        Severity(String value, int level) {

            this.value = value;

            this.level = level;

        }

        

        public String getValue() {

            return value;

        }

        

        public int getLevel() {

            return level;

        }

    }

    

    public enum Priority {

        LOW("low", 4),

        NORMAL("normal", 3),

        HIGH("high", 2),

        URGENT("urgent", 1);

        

        private final String value;

        private final int order;

        

        Priority(String value, int order) {

            this.value = value;

            this.order = order;

        }

        

        public String getValue() {

            return value;

        }

        

        public int getOrder() {

            return order;

        }

    }

    

    public enum AlertStatus {

        ACTIVE("active"),

        ACKNOWLEDGED("acknowledged"),

        INVESTIGATING("investigating"),

        RESOLVED("resolved"),

        CLOSED("closed"),

        FALSE_POSITIVE("false_positive");

        

        private final String value;

        

        AlertStatus(String value) {

            this.value = value;

        }

        

        public String getValue() {

            return value;

        }

    }

    

    @Data

    @NoArgsConstructor

    @AllArgsConstructor

    @Builder

    public static class EscalationInfo {

        private Integer escalationLevel;

        private LocalDateTime escalatedAt;

        private String escalatedBy;

        private String escalationReason;

        private LocalDateTime nextEscalation;

        private List<String> escalationHistory;

    }

    

    @Data

    @NoArgsConstructor

    @AllArgsConstructor

    @Builder

    public static class NotificationInfo {

        private List<String> channels; // websocket, email, sms

        private List<String> recipients;

        private LocalDateTime lastNotified;

        private Integer notificationCount;

        private Boolean notificationSent;

        private Map<String, Object> notificationMetadata;

    }

}


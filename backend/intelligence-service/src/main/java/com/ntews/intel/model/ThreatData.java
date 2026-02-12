package com.ntews.intel.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThreatData {
    private String id;
    private String type;
    private String source;
    private String sourceType;
    private String content;
    private LocalDateTime timestamp;
    private Map<String, Object> metadata;
    private String location;
    private Double confidence;
    private String severity;
}

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
public class CCTVData {
    private String id;
    private String cameraId;
    private String location;
    private String imageUrl;
    private String videoUrl;
    private LocalDateTime timestamp;
    private Map<String, Object> detectionResults;
    private Double confidence;
    private String alertType;
}

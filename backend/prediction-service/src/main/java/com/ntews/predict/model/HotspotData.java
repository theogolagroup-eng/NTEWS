package com.ntews.predict.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotspotData {
    private String id;
    private String locationName;
    private String latitude;
    private String longitude;
    private Double probability;
    private Double radius;
    private LocalDateTime peakTime;
    private String threatType;
    private Double confidence;
    private String severity;
}

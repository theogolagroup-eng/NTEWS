package com.ntews.prediction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
public class RiskTrend {
    private String timestamp;
    private String type;
    private Double riskScore;
    private String riskLevel;
    private Integer count;
}

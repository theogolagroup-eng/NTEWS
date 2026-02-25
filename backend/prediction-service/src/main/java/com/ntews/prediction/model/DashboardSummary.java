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
public class DashboardSummary {
    private int totalPredictions;
    private int activePredictions;
    private int highRiskPredictions;
    private int criticalPredictions;
    private int expiredPredictions;
}

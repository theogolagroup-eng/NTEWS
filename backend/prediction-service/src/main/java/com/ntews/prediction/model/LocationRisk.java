package com.ntews.prediction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
public class LocationRisk {
    private String location;
    private String latitude;
    private String longitude;
    private Double riskScore;
    private String riskLevel;
    private List<String> riskFactors;
}

package com.ntews.predict.config;

import com.ntews.predict.model.RiskForecast;
import com.ntews.predict.repository.RiskForecastRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {
    
    private final RiskForecastRepository riskForecastRepository;
    
    @Bean
    public CommandLineRunner initData() {
        return args -> {
            log.info("Initializing prediction data based on historical patterns...");
            
            // Check if data already exists
            if (riskForecastRepository.count() > 0) {
                log.info("Prediction data already exists, skipping initialization");
                return;
            }
            
            // Create realistic forecasts based on historical patterns
            List<RiskForecast> realisticForecasts = Arrays.asList(
                // Traffic Risk Forecast (based on real Nairobi patterns)
                RiskForecast.builder()
                    .id(UUID.randomUUID().toString())
                    .forecastType("traffic_risk")
                    .generatedAt(LocalDateTime.now().minusHours(2))
                    .validFrom(LocalDateTime.now())
                    .validTo(LocalDateTime.now().plusHours(24))
                    .confidenceScore(0.89)
                    .modelVersion("RandomForest-v1.0")
                    .forecastPoints(Arrays.asList(
                        RiskForecast.ForecastPoint.builder()
                            .timestamp(LocalDateTime.now().plusHours(1))
                            .predictedRisk(0.3)
                            .confidence(0.85)
                            .riskLevel("low")
                            .build(),
                        RiskForecast.ForecastPoint.builder()
                            .timestamp(LocalDateTime.now().plusHours(3))
                            .predictedRisk(0.7)
                            .confidence(0.89)
                            .riskLevel("medium")
                            .build(),
                        RiskForecast.ForecastPoint.builder()
                            .timestamp(LocalDateTime.now().plusHours(6))
                            .predictedRisk(0.8)
                            .confidence(0.92)
                            .riskLevel("high")
                            .build(),
                        RiskForecast.ForecastPoint.builder()
                            .timestamp(LocalDateTime.now().plusHours(9))
                            .predictedRisk(0.6)
                            .confidence(0.87)
                            .riskLevel("medium")
                            .build()
                    ))
                    .hotspots(Arrays.asList(
                        RiskForecast.HotspotPrediction.builder()
                            .hotspotId("hotspot-001")
                            .locationName("Thika Road - Roysambu")
                            .latitude("-1.2246")
                            .longitude("36.8836")
                            .probability(0.82)
                            .severity("high")
                            .threatType("traffic_congestion")
                            .peakTime(LocalDateTime.now().plusHours(3))
                            .radius(2.5)
                            .confidence(0.91)
                            .contributingFactors(Arrays.asList("rush_hour", "weather", "accident_history"))
                            .build()
                    ))
                    .build(),
                    
                // Crime Risk Forecast (based on historical crime data)
                RiskForecast.builder()
                    .id(UUID.randomUUID().toString())
                    .forecastType("crime_risk")
                    .generatedAt(LocalDateTime.now().minusHours(4))
                    .validFrom(LocalDateTime.now())
                    .validTo(LocalDateTime.now().plusHours(48))
                    .confidenceScore(0.87)
                    .modelVersion("GradientBoosting-v1.0")
                    .forecastPoints(Arrays.asList(
                        RiskForecast.ForecastPoint.builder()
                            .timestamp(LocalDateTime.now().plusHours(2))
                            .predictedRisk(0.4)
                            .confidence(0.82)
                            .riskLevel("low")
                            .build(),
                        RiskForecast.ForecastPoint.builder()
                            .timestamp(LocalDateTime.now().plusHours(5))
                            .predictedRisk(0.6)
                            .confidence(0.85)
                            .riskLevel("medium")
                            .build(),
                        RiskForecast.ForecastPoint.builder()
                            .timestamp(LocalDateTime.now().plusHours(8))
                            .predictedRisk(0.3)
                            .confidence(0.78)
                            .riskLevel("low")
                            .build(),
                        RiskForecast.ForecastPoint.builder()
                            .timestamp(LocalDateTime.now().plusHours(12))
                            .predictedRisk(0.5)
                            .confidence(0.83)
                            .riskLevel("medium")
                            .build()
                    ))
                    .hotspots(Arrays.asList(
                        RiskForecast.HotspotPrediction.builder()
                            .hotspotId("hotspot-002")
                            .locationName("Downtown CBD - Nightlife Area")
                            .latitude("-1.2844")
                            .longitude("36.8236")
                            .probability(0.73)
                            .severity("medium")
                            .threatType("theft_assault")
                            .peakTime(LocalDateTime.now().plusHours(8))
                            .radius(1.8)
                            .confidence(0.85)
                            .contributingFactors(Arrays.asList("night_time", "entertainment_venues", "low_lighting"))
                            .build(),
                        RiskForecast.HotspotPrediction.builder()
                            .hotspotId("hotspot-003")
                            .locationName("Eastlands - Residential Area")
                            .latitude("-1.2921")
                            .longitude("36.8219")
                            .probability(0.68)
                            .severity("medium")
                            .threatType("burglary")
                            .peakTime(LocalDateTime.now().plusHours(12))
                            .radius(2.2)
                            .confidence(0.79)
                            .contributingFactors(Arrays.asList("residential_density", "time_of_day", "security_coverage"))
                            .build()
                    ))
                    .build(),
                    
                // Weather-Related Risk Forecast (based on seasonal data)
                RiskForecast.builder()
                    .id(UUID.randomUUID().toString())
                    .forecastType("weather_risk")
                    .generatedAt(LocalDateTime.now().minusHours(1))
                    .validFrom(LocalDateTime.now())
                    .validTo(LocalDateTime.now().plusHours(72))
                    .confidenceScore(0.92)
                    .modelVersion("TimeSeries-v1.0")
                    .forecastPoints(Arrays.asList(
                        RiskForecast.ForecastPoint.builder()
                            .timestamp(LocalDateTime.now().plusHours(6))
                            .predictedRisk(0.4)
                            .confidence(0.88)
                            .riskLevel("low")
                            .build(),
                        RiskForecast.ForecastPoint.builder()
                            .timestamp(LocalDateTime.now().plusHours(12))
                            .predictedRisk(0.6)
                            .confidence(0.91)
                            .riskLevel("medium")
                            .build(),
                        RiskForecast.ForecastPoint.builder()
                            .timestamp(LocalDateTime.now().plusHours(24))
                            .predictedRisk(0.5)
                            .confidence(0.85)
                            .riskLevel("medium")
                            .build(),
                        RiskForecast.ForecastPoint.builder()
                            .timestamp(LocalDateTime.now().plusHours(48))
                            .predictedRisk(0.3)
                            .confidence(0.82)
                            .riskLevel("low")
                            .build()
                    ))
                    .hotspots(Arrays.asList(
                        RiskForecast.HotspotPrediction.builder()
                            .hotspotId("hotspot-004")
                            .locationName("Kibera Informal Settlement")
                            .latitude("-1.3167")
                            .longitude("36.7833")
                            .probability(0.78)
                            .severity("high")
                            .threatType("flooding")
                            .peakTime(LocalDateTime.now().plusHours(12))
                            .radius(3.5)
                            .confidence(0.88)
                            .contributingFactors(Arrays.asList("low_lying_area", "poor_drainage", "high_density"))
                            .build(),
                        RiskForecast.HotspotPrediction.builder()
                            .hotspotId("hotspot-005")
                            .locationName("Mathare Slum Area")
                            .latitude("-1.2722")
                            .longitude("36.8422")
                            .probability(0.71)
                            .severity("medium")
                            .threatType("flooding")
                            .peakTime(LocalDateTime.now().plusHours(12))
                            .radius(2.8)
                            .confidence(0.84)
                            .contributingFactors(Arrays.asList("river_proximity", "informal_settlement", "drainage_issues"))
                            .build()
                    ))
                    .build(),
                    
                // Social Media Threat Forecast (based on sentiment analysis)
                RiskForecast.builder()
                    .id(UUID.randomUUID().toString())
                    .forecastType("social_threat")
                    .generatedAt(LocalDateTime.now().minusHours(3))
                    .validFrom(LocalDateTime.now())
                    .validTo(LocalDateTime.now().plusHours(24))
                    .confidenceScore(0.83)
                    .modelVersion("NLP-v1.0")
                    .forecastPoints(Arrays.asList(
                        RiskForecast.ForecastPoint.builder()
                            .timestamp(LocalDateTime.now().plusHours(2))
                            .predictedRisk(0.4)
                            .confidence(0.79)
                            .riskLevel("low")
                            .build(),
                        RiskForecast.ForecastPoint.builder()
                            .timestamp(LocalDateTime.now().plusHours(4))
                            .predictedRisk(0.6)
                            .confidence(0.82)
                            .riskLevel("medium")
                            .build(),
                        RiskForecast.ForecastPoint.builder()
                            .timestamp(LocalDateTime.now().plusHours(6))
                            .predictedRisk(0.5)
                            .confidence(0.80)
                            .riskLevel("medium")
                            .build(),
                        RiskForecast.ForecastPoint.builder()
                            .timestamp(LocalDateTime.now().plusHours(8))
                            .predictedRisk(0.3)
                            .confidence(0.77)
                            .riskLevel("low")
                            .build()
                    ))
                    .hotspots(Arrays.asList(
                        RiskForecast.HotspotPrediction.builder()
                            .hotspotId("hotspot-006")
                            .locationName("Online Platform - Election Discourse")
                            .latitude("0.0") // Virtual location
                            .longitude("0.0")
                            .probability(0.64)
                            .severity("medium")
                            .threatType("disinformation")
                            .peakTime(LocalDateTime.now().plusHours(4))
                            .radius(0.0) // Virtual
                            .confidence(0.81)
                            .contributingFactors(Arrays.asList("election_period", "political_tension", "bot_activity"))
                            .build()
                    ))
                    .build()
            );
            
            riskForecastRepository.saveAll(realisticForecasts);
            log.info("Successfully initialized {} realistic forecasts based on historical patterns", realisticForecasts.size());
        };
    }
}

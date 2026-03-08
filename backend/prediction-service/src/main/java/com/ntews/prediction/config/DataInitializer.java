package com.ntews.prediction.config;

import com.ntews.alert.model.Alert;
import com.ntews.prediction.repository.ThreatPredictionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {
    
    private final ThreatPredictionRepository threatPredictionRepository;
    private final MongoTemplate mongoTemplate;
    
    @Bean
    public CommandLineRunner initPredictionData() {
        return args -> {
            log.info("🔮 Initializing threat prediction data with AI Engine integration...");
            
            // Clear existing predictions
            long deletedCount = threatPredictionRepository.count();
            if (deletedCount > 0) {
                log.info("🗑️  Clearing {} existing predictions for fresh initialization", deletedCount);
                threatPredictionRepository.deleteAll();
            }
            
            // Create prediction data based on Alert patterns
            List<Alert> sampleAlerts = Arrays.asList(
                // Sample alerts for prediction training
                Alert.builder()
                    .id("pred-sample-001")
                    .title("Sample Protest Alert")
                    .description("Sample alert for prediction model training")
                    .severity(Alert.Severity.HIGH)
                    .priority(Alert.Priority.HIGH)
                    .status(Alert.AlertStatus.ACTIVE)
                    .category("Social Media Threat")
                    .location(Alert.LocationInfo.builder()
                        .latitude("-1.2844")
                        .longitude("36.8236")
                        .city("Nairobi")
                        .region("Nairobi County")
                        .country("Kenya")
                        .address("Sample Location")
                        .build())
                    .source("Sample Source")
                    .confidence(0.85)
                    .aiConfidence(0.90)
                    .createdAt(LocalDateTime.now().minusHours(1))
                    .updatedAt(LocalDateTime.now().minusHours(1))
                    .build(),
                    
                Alert.builder()
                    .id("pred-sample-002")
                    .title("Sample Security Alert")
                    .description("Sample security alert for prediction model training")
                    .severity(Alert.Severity.MEDIUM)
                    .priority(Alert.Priority.NORMAL)
                    .status(Alert.AlertStatus.ACTIVE)
                    .category("Infrastructure Security")
                    .location(Alert.LocationInfo.builder()
                        .latitude("-1.2921")
                        .longitude("36.8219")
                        .city("Nairobi")
                        .region("Nairobi County")
                        .country("Kenya")
                        .address("Sample Location 2")
                        .build())
                    .source("Sample Source 2")
                    .confidence(0.75)
                    .aiConfidence(0.80)
                    .createdAt(LocalDateTime.now().minusHours(2))
                    .updatedAt(LocalDateTime.now().minusHours(2))
                    .build()
            );
            
            // Save sample alerts for prediction service to use
            try {
                // Save to alerts collection for prediction service access
                mongoTemplate.saveAll(sampleAlerts, "alerts");
                log.info("✅ Saved {} sample alerts for prediction service training", sampleAlerts.size());
            } catch (Exception e) {
                log.error("❌ Error saving sample alerts: {}", e.getMessage());
            }
            
            log.info("🔮 Prediction service initialization complete");
            log.info("📊 Sample data ready for prediction model training");
            return null;
        };
    }
}
            threatPredictionRepository.deleteAll();
            log.info("🗑️  Deleted {} existing threat predictions", deletedCount);
            
            // Create multilingual threat predictions
            List<ThreatPrediction> predictions = Arrays.asList(
                // High-risk political unrest prediction
                ThreatPrediction.builder()
                    .id("pred-001")
                    .predictionType("POLITICAL_UNREST")
                    .title("Election Period Tension Prediction")
                    .description("AI analysis predicts 78% probability of political unrest during upcoming election period. Historical data shows similar patterns in 2017 and 2022 elections.")
                    .confidenceLevel(0.78)
                    .riskScore(0.85)
                    .affectedRegion("Nairobi, Mombasa, Kisumu")
                    .timeframe("Next 7 days")
                    .dataSource("AI Historical Pattern Analysis")
                    .languageContext("multilingual")
                    .shengKeywords("maandamano, karao, wanakambo")
                    .swahiliKeywords("uchaguzi, tishio, usalama")
                    .englishKeywords("election, unrest, protest")
                    .createdAt(LocalDateTime.now().minusHours(2))
                    .expiresAt(LocalDateTime.now().plusDays(7))
                    .status("ACTIVE")
                    .build(),
                    
                // Border security threat prediction
                ThreatPrediction.builder()
                    .id("pred-002")
                    .predictionType("BORDER_SECURITY")
                    .title("Cross-Border Movement Prediction")
                    .description("ML models analyzing satellite and communication data predict 65% probability of increased cross-border activity in Mandera region.")
                    .confidenceLevel(0.65)
                    .riskScore(0.7)
                    .affectedRegion("Mandera, Wajir, Garissa")
                    .timeframe("Next 72 hours")
                    .dataSource("AI Satellite + Communication Analysis")
                    .languageContext("english-swahili-mixed")
                    .shengKeywords("")
                    .swahiliKeywords("mpaka, uhalifu, kuingia")
                    .englishKeywords("border, infiltration, movement")
                    .createdAt(LocalDateTime.now().minusHours(4))
                    .expiresAt(LocalDateTime.now().plusDays(3))
                    .status("ACTIVE")
                    .build(),
                    
                // Social media viral content prediction
                ThreatPrediction.builder()
                    .id("pred-003")
                    .predictionType("SOCIAL_MEDIA_VIRAL")
                    .title("Viral Content Spread Prediction")
                    .description("AI social media monitoring predicts 82% probability of viral spread of controversial content. Pattern matches previous misinformation campaigns.")
                    .confidenceLevel(0.82)
                    .riskScore(0.6)
                    .affectedRegion("Nationwide - Urban Centers")
                    .timeframe("Next 24 hours")
                    .dataSource("AI Social Media Analysis")
                    .languageContext("sheng-heavy")
                    .shengKeywords("mambo, walevi, kongea, wanakambo")
                    .swahiliKeywords("habari, jumuiya, kuenea")
                    .englishKeywords("viral, trending, misinformation")
                    .createdAt(LocalDateTime.now().minusMinutes(30))
                    .expiresAt(LocalDateTime.now().plusDays(1))
                    .status("ACTIVE")
                    .build(),
                    
                // Low risk benign prediction
                ThreatPrediction.builder()
                    .id("pred-004")
                    .predictionType("COMMUNITY_EVENT")
                    .title("Community Sports Tournament")
                    .description("AI predicts peaceful community gathering with 95% confidence. No security concerns identified.")
                    .confidenceLevel(0.95)
                    .riskScore(0.1)
                    .affectedRegion("Nakuru, Eldoret")
                    .timeframe("Weekend")
                    .dataSource("AI Event Analysis")
                    .languageContext("swahili-english")
                    .shengKeywords("poa, mambo mzuri")
                    .swahikiKeywords("michezo, jumuiya, amani")
                    .englishKeywords("sports, community, peaceful")
                    .createdAt(LocalDateTime.now().minusHours(1))
                    .expiresAt(LocalDateTime.now().plusDays(2))
                    .status("ACTIVE")
                    .build(),
                    
                // Medium risk cyber threat prediction
                ThreatPrediction.builder()
                    .id("pred-005")
                    .predictionType("CYBER_SECURITY")
                    .title("Government Infrastructure Cyber Threat")
                    .description("AI network analysis predicts 70% probability of cyber attacks on government systems during election period.")
                    .confidenceLevel(0.70)
                    .riskScore(0.75)
                    .affectedRegion("National Government Networks")
                    .timeframe("Next 48 hours")
                    .dataSource("AI Network Traffic Analysis")
                    .languageContext("english")
                    .shengKeywords("")
                    .swahikiKeywords("mtandao, kushambuliwa")
                    .englishKeywords("cyber, attack, infrastructure")
                    .createdAt(LocalDateTime.now().minusHours(3))
                    .expiresAt(LocalDateTime.now().plusDays(2))
                    .status("ACTIVE")
                    .build()
            );
            
            threatPredictionRepository.saveAll(predictions);
            log.info("✅ Successfully initialized {} threat predictions with AI Engine integration", predictions.size());
            log.info("🌍 Language contexts covered: English, Swahili, Sheng, Mixed");
            log.info("🔮 Prediction types: Political, Border, Social Media, Cyber, Community");
            log.info("📊 Risk levels: HIGH (0.85), MEDIUM (0.7), LOW (0.1)");
        };
    }
}

package com.ntews.alert.config;

import com.ntews.alert.model.Alert;
import com.ntews.alert.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {
    
    private final AlertRepository alertRepository;
    
    @Bean
    public CommandLineRunner initData() {
        return args -> {
            log.info("🚨 Initializing multilingual alerts with AI Engine analysis...");
            
            // Check if data already exists and force refresh
            long existingCount = alertRepository.count();
            if (existingCount > 0) {
                log.info("🗑️  Clearing {} existing alerts for fresh multilingual initialization", existingCount);
                alertRepository.deleteAll();
            }
            
            // Create alerts with multilingual content and AI Engine integration
            List<Alert> multilingualAlerts = Arrays.asList(
                // High-threat English alert
                Alert.builder()
                    .id("alert-001")
                    .title("Critical Security Threat Detected")
                    .description("Violent protest activity reported near government building with immediate threat to public safety")
                    .severity(Alert.Severity.CRITICAL)
                    .priority(Alert.Priority.HIGH)
                    .status(Alert.AlertStatus.ACTIVE)
                    .category("Security Threat")
                    .location(Alert.LocationInfo.builder()
                        .latitude("-1.2844")
                        .longitude("36.8236")
                        .city("Nairobi")
                        .region("Nairobi County")
                        .country("Kenya")
                        .address("Nairobi CBD")
                        .build())
                    .source("Network Monitor")
                    .confidence(0.85)
                    .aiConfidence(0.90)
                    .createdAt(LocalDateTime.now().minusMinutes(30))
                    .updatedAt(LocalDateTime.now().minusMinutes(30))
                    .build(),
                    
                // Swahili threat alert
                Alert.builder()
                    .id("alert-002")
                    .title("Tishio la Usalama - Kiswahili")
                    .description("Maandamano yasiyo na idhini yameripotiwa katika mji wa Mombasa, polisi wamejibiwa kwa mawe")
                    .severity(Alert.Severity.HIGH)
                    .priority(Alert.Priority.HIGH)
                    .status(Alert.AlertStatus.ACTIVE)
                    .category("Civil Unrest")
                    .location(Alert.LocationInfo.builder()
                        .latitude("-4.0435")
                        .longitude("39.6682")
                        .city("Mombasa")
                        .region("Mombasa County")
                        .country("Kenya")
                        .address("Mombasa Old Town")
                        .build())
                    .source("Community Reporter")
                    .confidence(0.75)
                    .aiConfidence(0.80)
                    .createdAt(LocalDateTime.now().minusHours(1))
                    .updatedAt(LocalDateTime.now().minusHours(1))
                    .build(),
                    
                // Sheng threat alert
                Alert.builder()
                    .id("alert-003")
                    .title("Ghasia za Sheng - Westlands")
                    .description("Walevi wameibia duka karao wananchi wakaanza kutoroka maandamano yanazidi")
                    .severity(Alert.Severity.HIGH)
                    .priority(Alert.Priority.HIGH)
                    .status(Alert.AlertStatus.ACTIVE)
                    .category("Criminal Activity")
                    .location(Alert.LocationInfo.builder()
                        .latitude("-1.2620")
                        .longitude("36.7965")
                        .city("Nairobi")
                        .region("Nairobi County")
                        .country("Kenya")
                        .address("Westlands")
                        .build())
                    .source("Social Media Monitor")
                    .confidence(0.80)
                    .aiConfidence(0.85)
                    .createdAt(LocalDateTime.now().minusMinutes(45))
                    .updatedAt(LocalDateTime.now().minusMinutes(45))
                    .build(),
                    
                // Suspicious activity - English
                Alert.builder()
                    .id("alert-004")
                    .title("Suspicious Network Activity")
                    .description("Unusual traffic patterns detected on critical infrastructure systems")
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
                        .address("Data Center A")
                        .build())
                    .source("Network Monitor")
                    .confidence(0.60)
                    .aiConfidence(0.65)
                    .createdAt(LocalDateTime.now().minusMinutes(20))
                    .updatedAt(LocalDateTime.now().minusMinutes(20))
                    .build(),
                    
                // Benign activity - Swahili
                Alert.builder()
                    .id("alert-005")
                    .title("Shughuli za Kawaida - Kiswahili")
                    .description("Wananchi wakijumuika katika sherehe ya kijiji chini ya usalama kamili")
                    .severity(Alert.Severity.LOW)
                    .priority(Alert.Priority.LOW)
                    .status(Alert.AlertStatus.RESOLVED)
                    .category("Community Activity")
                    .location(Alert.LocationInfo.builder()
                        .latitude("-0.0917")
                        .longitude("34.7680")
                        .city("Kisumu")
                        .region("Kisumu County")
                        .country("Kenya")
                        .address("Kisumu")
                        .build())
                    .source("Community Leader")
                    .confidence(0.80)
                    .aiConfidence(0.85)
                    .createdAt(LocalDateTime.now().minusHours(2))
                    .updatedAt(LocalDateTime.now().minusHours(2))
                    .build(),
                    
                // Non-threat English content
                Alert.builder()
                    .id("alert-006")
                    .title("Community Sports Event")
                    .description("Local football tournament bringing communities together in peaceful celebration")
                    .severity(Alert.Severity.LOW)
                    .priority(Alert.Priority.LOW)
                    .status(Alert.AlertStatus.ACTIVE)
                    .category("Community Event")
                    .location(Alert.LocationInfo.builder()
                        .latitude("-0.3031")
                        .longitude("36.0695")
                        .city("Nakuru")
                        .region("Nakuru County")
                        .country("Kenya")
                        .address("Nakuru")
                        .build())
                    .source("Event Coordinator")
                    .confidence(0.90)
                    .aiConfidence(0.95)
                    .createdAt(LocalDateTime.now().minusMinutes(15))
                    .updatedAt(LocalDateTime.now().minusMinutes(15))
                    .build(),
                    
                // High-threat mixed language
                Alert.builder()
                    .id("alert-007")
                    .title("Mixed Language Threat Alert")
                    .description("Walevi wanakambo protest area violence unrest wanahitaji action immediately")
                    .severity(Alert.Severity.CRITICAL)
                    .priority(Alert.Priority.URGENT)
                    .status(Alert.AlertStatus.ACTIVE)
                    .category("Coordinated Attack")
                    .location(Alert.LocationInfo.builder()
                        .latitude("0.5143")
                        .longitude("35.2698")
                        .city("Eldoret")
                        .region("Uasin Gishu County")
                        .country("Kenya")
                        .address("Eldoret Town")
                        .build())
                    .source("Emergency Services")
                    .confidence(0.85)
                    .aiConfidence(0.90)
                    .createdAt(LocalDateTime.now().minusMinutes(10))
                    .updatedAt(LocalDateTime.now().minusMinutes(10))
                    .build(),
                    
                // Suspicious Sheng content
                Alert.builder()
                    .id("alert-008")
                    .title("Kongea Mambo ya Usiku")
                    .description("Mambo ya kongea usiku karao wanatafuta walevi waliiba duka la simu")
                    .severity(Alert.Severity.MEDIUM)
                    .priority(Alert.Priority.NORMAL)
                    .status(Alert.AlertStatus.ACTIVE)
                    .category("Criminal Activity")
                    .location(Alert.LocationInfo.builder()
                        .latitude("-1.2206")
                        .longitude("36.8275")
                        .city("Nairobi")
                        .region("Nairobi County")
                        .country("Kenya")
                        .address("Thika Road")
                        .build())
                    .source("Night Watch")
                    .confidence(0.65)
                    .aiConfidence(0.70)
                    .createdAt(LocalDateTime.now().minusMinutes(5))
                    .updatedAt(LocalDateTime.now().minusMinutes(5))
                    .build(),
                    
                // Political tension alert
                Alert.builder()
                    .id("alert-009")
                    .title("Political Tension Rising")
                    .description("Election period approaching, political tensions increasing in urban centers")
                    .severity(Alert.Severity.HIGH)
                    .priority(Alert.Priority.HIGH)
                    .status(Alert.AlertStatus.ACTIVE)
                    .category("Political Security")
                    .location(Alert.LocationInfo.builder()
                        .latitude("-1.2844")
                        .longitude("36.8236")
                        .city("Nairobi")
                        .region("Nairobi County")
                        .country("Kenya")
                        .address("Nationwide")
                        .build())
                    .source("Political Analyst")
                    .confidence(0.70)
                    .aiConfidence(0.75)
                    .createdAt(LocalDateTime.now().minusHours(3))
                    .updatedAt(LocalDateTime.now().minusHours(3))
                    .build(),
                    
                // Normal daily activity - Swahili
                Alert.builder()
                    .id("alert-010")
                    .title("Shughuli za Kila Siku")
                    .description("Leo nimepika ugali wa maharage na samaki wa kukaanga, kila kitu poa sana")
                    .severity(Alert.Severity.LOW)
                    .priority(Alert.Priority.LOW)
                    .status(Alert.AlertStatus.RESOLVED)
                    .category("Community Activity")
                    .location(Alert.LocationInfo.builder()
                        .latitude("-3.6371")
                        .longitude("39.8502")
                        .city("Kilifi")
                        .region("Kilifi County")
                        .country("Kenya")
                        .address("Kilifi")
                        .build())
                    .source("Local Resident")
                    .confidence(0.80)
                    .aiConfidence(0.85)
                    .createdAt(LocalDateTime.now().minusHours(1))
                    .updatedAt(LocalDateTime.now().minusHours(1))
                    .build()
            );
            
            alertRepository.saveAll(multilingualAlerts);
            log.info("✅ Successfully initialized {} multilingual alerts with AI Engine analysis", multilingualAlerts.size());
            log.info("🌍 Languages covered: English, Swahili, Sheng-mixed");
            log.info("🚨 Threat categories: CRITICAL, HIGH, MEDIUM, LOW");
            log.info("🔍 AI Engine integration: Rule-based threat detection");
            log.info("📍 East African relevance: High priority alerts flagged");
        };
    }
}

package com.ntews.ingestion.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntews.ingestion.document.TwitterStreamDocument;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Secure Twitter/X Ingestion Service
 * Connects to X API v2 Filtered Stream for real-time security monitoring
 * Processes Sheng and English security keywords for East African context
 * Saves processed data to MongoDB with AI Engine analysis
 */
// @Service - DISABLED to save 8GB RAM resources
// @Slf4j
public class TwitterIngestionService {

    private static final Logger log = LoggerFactory.getLogger(TwitterIngestionService.class);

    private final String BEARER_TOKEN;
    private final WebClient twitterWebClient;
    private final TwitterProcessingService twitterProcessingService;
    private final ObjectMapper objectMapper;
    
    // Security keywords for East African context
    private static final String[] SECURITY_KEYWORDS = {
        "maandamano", "karao", "protest CBD", "police", "unrest", 
        "demonstration", "riot", "violence", "security", "threat",
        "kongea", "mambo", "wanakambo", "walevi"
    };

    public TwitterIngestionService(@Value("${twitter.bearer.token:}") String bearerToken,
                                  TwitterProcessingService twitterProcessingService,
                                  WebClient.Builder webClientBuilder) {
        this.BEARER_TOKEN = bearerToken;
        this.twitterProcessingService = twitterProcessingService;
        this.objectMapper = new ObjectMapper();
        this.twitterWebClient = webClientBuilder
                .baseUrl("https://api.twitter.com")
                .defaultHeader("Authorization", "Bearer " + bearerToken)
                .build();
    }

    @PostConstruct
    public void startIngestion() {
        if (BEARER_TOKEN == null || BEARER_TOKEN.isEmpty()) {
            log.error("Twitter Bearer Token not configured. Please set twitter.bearer.token property");
            return;
        }

        log.info("Starting Twitter/X ingestion service for security monitoring...");
        log.info("Monitoring keywords: {}", String.join(", ", SECURITY_KEYWORDS));
        
        try {
            // Setup filtered stream rules
            setupStreamRules()
                .thenMany(startFilteredStream())
                .subscribe(
                    tweetData -> {
                        log.info("🐦 Processing Tweet: {}", tweetData.get("text").asText());
                        
                        // Convert to TwitterStreamDocument and process
                        TwitterStreamDocument document = convertToDocument(tweetData);
                        twitterProcessingService.processTweet(document);
                    },
                    error -> log.error("❌ Twitter streaming error: {}", error.getMessage()),
                    () -> log.info("✅ Twitter streaming completed")
                );
            
        } catch (Exception e) {
            log.error("❌ Failed to start Twitter ingestion service", e);
        }
    }

    /**
     * Setup filtered stream rules
     */
    private Mono<Void> setupStreamRules() {
        // First, delete existing rules
        return twitterWebClient.get()
                .uri("/2/tweets/search/stream/rules")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMap(rulesNode -> {
                    if (rulesNode.has("data") && rulesNode.get("data").size() > 0) {
                        // Delete existing rules
                        Map<String, Object> deleteRequest = new HashMap<>();
                        deleteRequest.put("delete", Map.of("ids", 
                            Arrays.asList(rulesNode.get("data").get(0).get("id").asText())));
                        
                        return twitterWebClient.post()
                                .uri("/2/tweets/search/stream/rules")
                                .bodyValue(deleteRequest)
                                .retrieve()
                                .bodyToMono(Void.class);
                    }
                    return Mono.empty();
                })
                .then(Mono.defer(() -> {
                    // Add new rule
                    String keywordRule = String.join(" OR ", SECURITY_KEYWORDS);
                    Map<String, Object> addRequest = new HashMap<>();
                    addRequest.put("add", Arrays.asList(Map.of(
                        "value", keywordRule,
                        "tag", "ntews_security_monitor"
                    )));
                    
                    return twitterWebClient.post()
                            .uri("/2/tweets/search/stream/rules")
                            .bodyValue(addRequest)
                            .retrieve()
                            .bodyToMono(JsonNode.class)
                            .doOnSuccess(response -> log.info("✅ Added security monitoring rule"));
                }))
                .then();
    }

    /**
     * Start filtered stream
     */
    private Flux<JsonNode> startFilteredStream() {
        return twitterWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/2/tweets/search/stream")
                        .queryParam("tweet.fields", "created_at,public_metrics,geo,lang,entities")
                        .queryParam("user.fields", "location,verified,created_at,public_metrics")
                        .queryParam("expansions", "author_id")
                        .build())
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response -> {
                    log.error("Twitter API 4xx error: {}", response.statusCode());
                    return Mono.error(new RuntimeException("Twitter API authentication failed: " + response.statusCode()));
                })
                .onStatus(status -> status.is5xxServerError(), response -> {
                    log.error("Twitter API 5xx error: {}", response.statusCode());
                    return Mono.error(new RuntimeException("Twitter API server error: " + response.statusCode()));
                })
                .bodyToFlux(String.class)
                .filter(line -> !line.trim().isEmpty() && line.startsWith("{"))
                .map(this::parseTweet)
                .filter(tweetData -> tweetData != null && tweetData.has("data"))
                .doOnNext(tweet -> log.info(" Received tweet: {}", tweet.get("data").get("text").asText()))
                .doOnError(error -> log.error(" Twitter stream error: {}", error.getMessage()));
    }

    /**
     * Parse tweet JSON
     */
    private JsonNode parseTweet(String tweetJson) {
        try {
            return objectMapper.readTree(tweetJson);
        } catch (Exception e) {
            log.error("Error parsing tweet JSON: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Convert JSON tweet to TwitterStreamDocument
     */
    private TwitterStreamDocument convertToDocument(JsonNode tweetData) {
        try {
            JsonNode tweet = tweetData.get("data");
            JsonNode includes = tweetData.get("includes");
            
            TwitterStreamDocument document = new TwitterStreamDocument(
                tweet.get("id").asText(),
                tweet.get("author_id").asText(),
                tweet.get("text").asText(),
                tweet.has("lang") ? tweet.get("lang").asText() : "unknown",
                LocalDateTime.now() // Will be updated with actual created_at if available
            );

            // Add user information if available
            if (includes != null && includes.has("users") && includes.get("users").size() > 0) {
                JsonNode user = includes.get("users").get(0);
                document.setUsername(user.get("name").asText());
                if (user.has("location")) {
                    document.setUserLocation(user.get("location").asText());
                }
                document.setUserVerified(user.get("verified").asBoolean());
                document.setFollowersCount(user.get("public_metrics").get("followers_count").asLong());
            }

            // Add engagement metrics if available
            if (tweet.has("public_metrics")) {
                JsonNode metrics = tweet.get("public_metrics");
                Map<String, Object> metricsMap = new HashMap<>();
                metricsMap.put("retweet_count", metrics.get("retweet_count").asLong());
                metricsMap.put("like_count", metrics.get("like_count").asLong());
                metricsMap.put("reply_count", metrics.get("reply_count").asLong());
                metricsMap.put("quote_count", metrics.get("quote_count").asLong());
                document.setPublicMetrics(metricsMap);
            }

            return document;
            
        } catch (Exception e) {
            log.error("Error converting tweet to document: {}", e.getMessage());
            return null;
        }
    }
}

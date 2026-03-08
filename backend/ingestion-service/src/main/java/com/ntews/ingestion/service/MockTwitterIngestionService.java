package com.ntews.ingestion.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntews.ingestion.document.TwitterStreamDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Mock Twitter/X Ingestion Service
 * Simulates real-time Twitter streaming using local JSON data
 * Processes Sheng and English security keywords for East African context
 * Saves processed data to MongoDB with AI Engine analysis
 * 
 * This service replaces the paid Twitter API for development/testing
 */
@Service
@Slf4j
public class MockTwitterIngestionService {

    private final TwitterProcessingService twitterProcessingService;
    private final ObjectMapper objectMapper;
    private final boolean enableMockStream;
    
    // Security keywords for East African context
    private static final String[] SECURITY_KEYWORDS = {
        "maandamano", "karao", "protest CBD", "police", "unrest", 
        "demonstration", "riot", "violence", "security", "threat",
        "kongea", "mambo", "wanakambo", "walevi"
    };

    public MockTwitterIngestionService(TwitterProcessingService twitterProcessingService,
                                     ObjectMapper objectMapper,
                                     @Value("${ingestion.mock.enabled:true}") boolean enableMockStream) {
        this.twitterProcessingService = twitterProcessingService;
        this.objectMapper = objectMapper;
        this.enableMockStream = enableMockStream;
    }

    @PostConstruct
    public void startMockIngestion() {
        if (!enableMockStream) {
            log.info("Mock Twitter streaming disabled");
            return;
        }

        log.info("🎭 Starting Mock Twitter/X ingestion service for security monitoring...");
        log.info("📝 Monitoring keywords: {}", String.join(", ", SECURITY_KEYWORDS));
        
        try {
            // Load mock tweets and start streaming simulation
            loadMockTweets()
                .flatMapMany(tweets -> Flux.fromIterable(tweets)
                    .delayElements(Duration.ofSeconds(3)) // Simulate real-time streaming
                    .doOnNext(tweet -> log.info("🐦 Mock Tweet: {}", tweet.get("text").asText()))
                    .flatMap(this::processMockTweet))
                .subscribe(
                    document -> log.info("✅ Processed mock tweet: {}", document.getTweetId()),
                    error -> log.error("❌ Mock streaming error: {}", error.getMessage()),
                    () -> log.info("🎉 Mock streaming completed")
                );
            
        } catch (Exception e) {
            log.error("❌ Failed to start mock Twitter ingestion service", e);
        }
    }

    /**
     * Load mock tweets from JSON file
     */
    private Mono<List<JsonNode>> loadMockTweets() {
        return Mono.fromCallable(() -> {
            Resource resource = new ClassPathResource("mock-tweets.json");
            List<JsonNode> tweets = objectMapper.readValue(resource.getInputStream(), 
                new TypeReference<List<JsonNode>>() {});
            log.info("📋 Loaded {} mock tweets for streaming", tweets.size());
            return tweets;
        })
        .doOnError(error -> log.error("Failed to load mock tweets: {}", error.getMessage()));
    }

    /**
     * Process individual mock tweet
     */
    private Mono<TwitterStreamDocument> processMockTweet(JsonNode tweetData) {
        try {
            // Convert to TwitterStreamDocument
            TwitterStreamDocument document = convertMockToDocument(tweetData);
            
            // Process with AI Engine and save to MongoDB
            return Mono.fromRunnable(() -> twitterProcessingService.processTweet(document))
                .thenReturn(document)
                .doOnSuccess(doc -> log.info("🗄️ Saved mock tweet {} to MongoDB", doc.getTweetId()));
                
        } catch (Exception e) {
            log.error("Error processing mock tweet: {}", e.getMessage());
            return Mono.empty();
        }
    }

    /**
     * Convert mock JSON tweet to TwitterStreamDocument
     */
    private TwitterStreamDocument convertMockToDocument(JsonNode tweetData) {
        try {
            JsonNode tweet = tweetData;
            JsonNode authorInfo = tweet.get("author_info");
            
            // Parse created_at
            LocalDateTime createdAt = LocalDateTime.now();
            if (tweet.has("created_at")) {
                try {
                    createdAt = LocalDateTime.parse(tweet.get("created_at").asText(), 
                        DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                } catch (Exception e) {
                    log.warn("Could not parse created_at, using current time");
                }
            }
            
            TwitterStreamDocument document = new TwitterStreamDocument(
                tweet.get("id").asText(),
                tweet.get("author_id").asText(),
                tweet.get("text").asText(),
                tweet.has("lang") ? tweet.get("lang").asText() : "unknown",
                createdAt
            );

            // Add user information
            if (authorInfo != null) {
                document.setUsername(authorInfo.get("name").asText());
                if (authorInfo.has("location")) {
                    document.setUserLocation(authorInfo.get("location").asText());
                }
                document.setUserVerified(authorInfo.get("verified").asBoolean());
                document.setFollowersCount(authorInfo.get("followers_count").asLong());
            }

            // Add engagement metrics
            if (tweet.has("public_metrics")) {
                JsonNode metrics = tweet.get("public_metrics");
                Map<String, Object> metricsMap = new HashMap<>();
                metricsMap.put("retweet_count", metrics.get("retweet_count").asLong());
                metricsMap.put("like_count", metrics.get("like_count").asLong());
                metricsMap.put("reply_count", metrics.get("reply_count").asLong());
                metricsMap.put("quote_count", metrics.get("quote_count").asLong());
                document.setPublicMetrics(metricsMap);
            }

            // Set mock-specific fields
            document.setSource("mock_twitter_api");
            document.setContext("security_monitoring");
            
            return document;
            
        } catch (Exception e) {
            log.error("Error converting mock tweet to document: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Manual trigger for processing specific mock tweet
     */
    public Mono<TwitterStreamDocument> processSpecificMockTweet(String tweetId) {
        return loadMockTweets()
            .flatMapMany(tweets -> Flux.fromIterable(tweets))
            .filter(tweet -> tweet.get("id").asText().equals(tweetId))
            .next()
            .flatMap(this::processMockTweet)
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Mock tweet not found: " + tweetId)));
    }

    /**
     * Get statistics about mock tweets
     */
    public Mono<Map<String, Object>> getMockStats() {
        return loadMockTweets()
            .map(tweets -> {
                Map<String, Object> stats = new HashMap<>();
                stats.put("total_tweets", tweets.size());
                stats.put("languages", tweets.stream()
                    .map(t -> t.get("lang").asText())
                    .distinct()
                    .count());
                stats.put("verified_users", tweets.stream()
                    .filter(t -> t.get("author_info").get("verified").asBoolean())
                    .count());
                stats.put("avg_engagement", tweets.stream()
                    .mapToLong(t -> t.get("public_metrics").get("like_count").asLong())
                    .average()
                    .orElse(0.0));
                stats.put("security_keywords_found", Arrays.stream(SECURITY_KEYWORDS).count());
                return stats;
            });
    }
}

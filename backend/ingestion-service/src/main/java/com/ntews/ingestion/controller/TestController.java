package com.ntews.ingestion.controller;

import com.ntews.ingestion.document.TwitterStreamDocument;
import com.ntews.ingestion.service.MockTwitterIngestionService;
import com.ntews.ingestion.service.TwitterProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Test controller for verifying Twitter ingestion functionality
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private TwitterProcessingService twitterProcessingService;

    @Autowired(required = false)
    private MockTwitterIngestionService mockTwitterService;

    @Value("${twitter.bearer.token}")
    private String bearerToken;

    @Value("${ingestion.mock.enabled:false}")
    private boolean mockEnabled;

    private final WebClient twitterWebClient;

    public TestController(WebClient.Builder webClientBuilder) {
        this.twitterWebClient = webClientBuilder
                .baseUrl("https://api.twitter.com")
                .defaultHeader("Authorization", "Bearer " + bearerToken)
                .build();
    }

    /**
     * Test Twitter API authentication
     */
    @GetMapping("/twitter-auth")
    public ResponseEntity<Map<String, Object>> testTwitterAuth() {
        try {
            return twitterWebClient.get()
                    .uri("/2/users/me")
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(response -> {
                        Map<String, Object> result = new HashMap<>();
                        result.put("status", "success");
                        result.put("message", "Twitter API authentication successful");
                        result.put("user_info", response);
                        return ResponseEntity.ok(result);
                    })
                    .onErrorReturn(ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Twitter API authentication failed"
                    )))
                    .block();
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Twitter API test failed: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Test mock Twitter service statistics
     */
    @GetMapping("/mock-stats")
    public ResponseEntity<Map<String, Object>> getMockStats() {
        if (!mockEnabled || mockTwitterService == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Mock service not enabled"
            ));
        }

        try {
            Map<String, Object> stats = mockTwitterService.getMockStats().block();
            stats.put("status", "success");
            stats.put("message", "Mock service statistics");
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to get mock stats: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Process specific mock tweet by ID
     */
    @PostMapping("/mock-tweet/{tweetId}")
    public ResponseEntity<Map<String, Object>> processMockTweet(@PathVariable String tweetId) {
        if (!mockEnabled || mockTwitterService == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Mock service not enabled"
            ));
        }

        try {
            TwitterStreamDocument document = mockTwitterService.processSpecificMockTweet(tweetId).block();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Mock tweet processed successfully");
            response.put("tweetId", document.getTweetId());
            response.put("text", document.getText());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to process mock tweet: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Test endpoint to simulate Twitter data processing
     */
    @PostMapping("/simulate-tweet")
    public ResponseEntity<Map<String, Object>> simulateTweet() {
        try {
            // Create a test TwitterStreamDocument
            TwitterStreamDocument testDocument = new TwitterStreamDocument(
                "test_tweet_123",
                "test_user_456",
                "Maandamano ya police Nairobi wananchi wanakambo karao",
                "sw",
                LocalDateTime.now()
            );

            // Set additional metadata
            testDocument.setUsername("Test User");
            testDocument.setUserLocation("Nairobi, Kenya");
            testDocument.setUserVerified(false);
            testDocument.setFollowersCount(1500L);
            
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("retweet_count", 25L);
            metrics.put("like_count", 100L);
            metrics.put("reply_count", 15L);
            metrics.put("quote_count", 5L);
            testDocument.setPublicMetrics(metrics);

            // Process the test tweet
            twitterProcessingService.processTweet(testDocument);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Test tweet processed successfully");
            response.put("tweetId", testDocument.getTweetId());
            response.put("text", testDocument.getText());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to process test tweet: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "healthy");
        status.put("service", "Twitter Ingestion Service");
        status.put("timestamp", LocalDateTime.now());
        status.put("components", Map.of(
            "ai_engine", "connected",
            "mongodb", "connected",
            "twitter_api", mockEnabled ? "mock_enabled" : "configured",
            "mock_service", mockEnabled ? "enabled" : "disabled"
        ));
        return ResponseEntity.ok(status);
    }
}

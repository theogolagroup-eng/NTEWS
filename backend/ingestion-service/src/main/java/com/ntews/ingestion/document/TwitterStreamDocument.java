package com.ntews.ingestion.document;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

/**
 * MongoDB Document for storing processed Twitter stream data
 * Includes AI Engine analysis results and metadata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "twitter_streams")
public class TwitterStreamDocument {

    @Id
    private String id;

    @Field("tweet_id")
    private String tweetId;

    @Field("author_id")
    private String authorId;

    @Field("username")
    private String username;

    @Field("text")
    private String text;

    @Field("user_location")
    private String userLocation;

    @Field("user_verified")
    private Boolean userVerified;

    @Field("followers_count")
    private Long followersCount;

    @Field("language")
    private String language;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("ingested_at")
    private LocalDateTime ingestedAt;

    @Field("public_metrics")
    private Map<String, Object> publicMetrics;

    // AI Engine Analysis Results
    @Field("sheng_detected")
    private Boolean shengDetected;

    @Field("original_language")
    private String originalLanguage;

    @Field("classification")
    private String classification;

    @Field("confidence")
    private Double confidence;

    @Field("sheng_words_detected")
    private List<String> shengWordsDetected;

    @Field("risk_score")
    private Double riskScore;

    @Field("processor_used")
    private String processorUsed;

    @Field("threat_probabilities")
    private Map<String, Double> threatProbabilities;

    @Field("sentiment_scores")
    private Map<String, Double> sentimentScores;

    @Field("threat_keywords")
    private List<String> threatKeywords;

    @Field("recommendations")
    private List<String> recommendations;

    @Field("normalized_text")
    private String normalizedText;

    // Processing metadata
    @Field("source")
    private String source;

    @Field("context")
    private String context;

    @Field("processing_time_ms")
    private Long processingTimeMs;

    @Field("ai_engine_response")
    private String aiEngineResponse;

    // Security classification
    @Field("is_threat")
    private Boolean isThreat;

    @Field("risk_category")
    private String riskCategory;

    @Field("security_relevance")
    private String securityRelevance;

    // Constructor for initial tweet data
    public TwitterStreamDocument(String tweetId, String authorId, String text, String language, LocalDateTime createdAt) {
        this.tweetId = tweetId;
        this.authorId = authorId;
        this.text = text;
        this.language = language;
        this.createdAt = createdAt;
        this.ingestedAt = LocalDateTime.now();
        this.source = "twitter_api_v2";
        this.context = "security_monitoring";
    }
}

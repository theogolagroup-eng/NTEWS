package com.ntews.ingestion.repository;

import com.ntews.ingestion.document.TwitterStreamDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB Repository for Twitter Stream Data
 * Provides methods for storing and querying processed Twitter data
 */
@Repository
public interface TwitterStreamRepository extends MongoRepository<TwitterStreamDocument, String> {

    /**
     * Find tweets by classification
     */
    List<TwitterStreamDocument> findByClassification(String classification);

    /**
     * Find tweets with Sheng content
     */
    List<TwitterStreamDocument> findByShengDetectedTrue();

    /**
     * Find tweets marked as threats
     */
    List<TwitterStreamDocument> findByIsThreatTrue();

    /**
     * Find tweets by risk category
     */
    List<TwitterStreamDocument> findByRiskCategory(String riskCategory);

    /**
     * Find tweets by language
     */
    List<TwitterStreamDocument> findByOriginalLanguage(String originalLanguage);

    /**
     * Find tweets within a time range
     */
    List<TwitterStreamDocument> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Find tweets by author
     */
    List<TwitterStreamDocument> findByAuthorId(String authorId);

    /**
     * Find tweets by user location
     */
    List<TwitterStreamDocument> findByUserLocationContaining(String location);

    /**
     * Find high-risk tweets (risk score above threshold)
     */
    @Query("{'riskScore': {$gt: ?0}}")
    List<TwitterStreamDocument> findByRiskScoreGreaterThan(Double threshold);

    /**
     * Find recent tweets (last N hours)
     */
    @Query("{'ingestedAt': {$gte: ?0}}")
    List<TwitterStreamDocument> findRecentTweets(LocalDateTime since);

    /**
     * Count tweets by classification
     */
    @Query("{'classification': ?0}")
    long countByClassification(String classification);

    /**
     * Find tweets with specific threat keywords
     */
    @Query("{'threatKeywords': {$in: ?0}}")
    List<TwitterStreamDocument> findByThreatKeywordsIn(List<String> keywords);

    /**
     * Get statistics for dashboard
     */
    @Query(value = "{}", count = true)
    long getTotalTweetsCount();

    /**
     * Find tweets with high engagement (retweets + likes > threshold)
     */
    @Query("{'public_metrics.retweet_count': {$gt: ?0}}")
    List<TwitterStreamDocument> findHighEngagementTweets(int minRetweets);
}

package com.ntews.alert.repository;

import com.ntews.alert.model.UnifiedPost;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Repository for storing processed posts with AI analysis
 * Shared between Alert service and Metrics Aggregator
 */
@Repository
public class ProcessedPostRepository {
    
    private final Map<String, UnifiedPost> processedPosts = new ConcurrentHashMap<>();
    
    /**
     * Store a processed post with AI analysis
     */
    public void savePost(UnifiedPost post) {
        processedPosts.put(post.getId(), post);
    }
    
    /**
     * Get a processed post by ID
     */
    public UnifiedPost getPost(String postId) {
        return processedPosts.get(postId);
    }
    
    /**
     * Get all processed posts
     */
    public Map<String, UnifiedPost> getAllPosts() {
        return new ConcurrentHashMap<>(processedPosts);
    }
    
    /**
     * Remove old posts to manage memory
     */
    public void cleanup() {
        // Keep only last 1000 posts
        if (processedPosts.size() > 1000) {
            processedPosts.entrySet().removeIf(entry -> {
                // Remove posts older than 6 hours
                return entry.getValue().getProcessedAt().isBefore(
                    java.time.LocalDateTime.now().minusHours(6)
                );
            });
        }
    }
}

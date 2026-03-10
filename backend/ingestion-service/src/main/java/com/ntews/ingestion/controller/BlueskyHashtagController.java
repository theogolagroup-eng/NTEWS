package com.ntews.ingestion.controller;

import com.ntews.ingestion.service.BlueskyMetricsAggregator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for Bluesky hashtag analysis
 */
@RestController
@RequestMapping("/api/bluesky")
@RequiredArgsConstructor
public class BlueskyHashtagController {
    
    private static final Logger log = LoggerFactory.getLogger(BlueskyHashtagController.class);
    
    private final BlueskyMetricsAggregator metricsAggregator;
    
    /**
     * Get trending hashtags from recent posts
     */
    @GetMapping("/metrics/trending-hashtags")
    public ResponseEntity<List<Map<String, Object>>> getTrendingHashtags(
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<Map<String, Object>> trendingPosts = metricsAggregator.getTrendingPosts(limit * 2); // Get more posts for hashtag analysis
            
            // Extract and count hashtags
            Map<String, Integer> hashtagCounts = new java.util.HashMap<>();
            List<Map<String, Object>> hashtags = new java.util.ArrayList<>();
            
            for (Map<String, Object> post : trendingPosts) {
                @SuppressWarnings("unchecked")
                List<String> postHashtags = (List<String>) post.get("hashtags");
                if (postHashtags != null) {
                    for (String hashtag : postHashtags) {
                        hashtagCounts.put(hashtag, hashtagCounts.getOrDefault(hashtag, 0) + 1);
                    }
                }
            }
            
            // Convert to sorted list
            hashtagCounts.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(limit)
                .forEach(entry -> {
                    Map<String, Object> hashtagData = new java.util.HashMap<>();
                    hashtagData.put("hashtag", entry.getKey());
                    hashtagData.put("count", entry.getValue());
                    hashtagData.put("posts", hashtagCounts.get(entry.getKey()));
                    hashtags.add(hashtagData);
                });
            
            return ResponseEntity.ok(hashtags);
            
        } catch (Exception e) {
            log.error("❌ Error getting trending hashtags: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get posts with specific hashtag
     */
    @GetMapping("/metrics/posts-by-hashtag")
    public ResponseEntity<List<Map<String, Object>>> getPostsByHashtag(
            @RequestParam String hashtag,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> allPosts = metricsAggregator.getTrendingPosts(100); // Get more posts to filter
            
            List<Map<String, Object>> filteredPosts = allPosts.stream()
                .filter(post -> {
                    @SuppressWarnings("unchecked")
                    List<String> postHashtags = (List<String>) post.get("hashtags");
                    return postHashtags != null && postHashtags.contains(hashtag);
                })
                .limit(limit)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(filteredPosts);
            
        } catch (Exception e) {
            log.error("❌ Error getting posts by hashtag: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}

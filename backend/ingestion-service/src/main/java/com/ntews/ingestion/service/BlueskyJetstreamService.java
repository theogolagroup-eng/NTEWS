package com.ntews.ingestion.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntews.ingestion.client.ServiceWebSocketClient;
import com.ntews.ingestion.model.UnifiedPost;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bluesky Jetstream WebSocket client for real-time data ingestion
 * Optimized for 8GB laptop with multi-stream filtering and metrics aggregation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BlueskyJetstreamService {

    private final ServiceWebSocketClient serviceWebSocketClient;
    private final ObjectMapper objectMapper;
    
    // Target keywords for threat detection (Swahili/Sheng/English)
    private static final List<String> THREAT_KEYWORDS = Arrays.asList(
        "maandamano", "karao", "wanakambo", "tishio", "usalama",
        "protest", "unrest", "violence", "threat", "danger",
        "flood", "disaster", "emergency", "security", "attack"
    );
    
    // Target languages
    private static final List<String> TARGET_LANGUAGES = Arrays.asList("en", "sw", "ke");
    
    // In-memory cache for post metrics (to reduce DB writes)
    private final Map<String, UnifiedPost.PostMetrics> metricsCache = new ConcurrentHashMap<>();
    
    // Jetstream endpoints
    private static final String JETSTREAM_URL = "wss://jetstream1.us-west.bsky.network/subscribe";
    
    // Collections to subscribe to
    private static final String COLLECTIONS_PARAM = 
        "wantedCollections=app.bsky.feed.post" +
        "&wantedCollections=app.bsky.feed.like" +
        "&wantedCollections=app.bsky.feed.repost" +
        "&wantedCollections=app.bsky.graph.follow";
    
    @PostConstruct
    public void initialize() {
        log.info("🔵 Initializing Bluesky Jetstream WebSocket client...");
        log.info("📡 Service WebSocket client will handle communication with NTEWS services");
        
        connectToJetstream();
    }
    
    private void connectToJetstream() {
        String url = JETSTREAM_URL + "?" + COLLECTIONS_PARAM;
        
        // Create temporary client for Bluesky connection
        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .pingInterval(30, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS) // No timeout for streaming
            .build();
        
        Request request = new Request.Builder()
            .url(url)
            .build();
            
        WebSocket webSocket = client.newWebSocket(request, new BlueskyWebSocketListener());
        
        log.info("🔗 Connected to Bluesky Jetstream: {}", url);
    }
    
    /**
     * WebSocket listener for handling Bluesky events
     */
    private class BlueskyWebSocketListener extends WebSocketListener {
        
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            log.info("✅ Bluesky Jetstream connection opened");
        }
        
        @Override
        public void onMessage(WebSocket webSocket, String text) {
            try {
                JsonNode event = objectMapper.readTree(text);
                String collection = event.at("/commit/collection").asText();
                
                switch (collection) {
                    case "app.bsky.feed.post":
                        handlePostEvent(event);
                        break;
                    case "app.bsky.feed.like":
                        handleLikeEvent(event);
                        break;
                    case "app.bsky.feed.repost":
                        handleRepostEvent(event);
                        break;
                    case "app.bsky.graph.follow":
                        handleFollowEvent(event);
                        break;
                    default:
                        log.debug("Ignoring collection: {}", collection);
                }
            } catch (Exception e) {
                log.error("❌ Error processing Bluesky message: {}", e.getMessage());
            }
        }
        
        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            log.error("💥 Bluesky WebSocket failure: {}", t.getMessage());
            
            // Attempt reconnection after delay
            try {
                Thread.sleep(5000);
                connectToJetstream();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            log.warn("🔌 Bluesky WebSocket closing: {} - {}", code, reason);
        }
    }
    
    /**
     * Handle new post events
     */
    private void handlePostEvent(JsonNode event) {
        try {
            JsonNode record = event.at("/commit/record");
            String text = record.at("/text").asText();
            String author = event.at("/commit/author").asText();
            String uri = event.at("/commit/uri").asText();
            String cid = event.at("/commit/cid").asText();
            LocalDateTime timestamp = parseTimestamp(event.at("/commit/createdAt").asText());
            
            // Extract hashtags and mentions
            List<String> hashtags = extractHashtags(text);
            List<String> mentions = extractMentions(text);
            
            // Detect language (simple heuristic for now)
            String language = detectLanguage(text);
            
            // Create unified post
            UnifiedPost post = UnifiedPost.builder()
                .id(uri)
                .source("bluesky")
                .author(author)
                .authorHandle(extractHandle(author))
                .text(text)
                .timestamp(timestamp)
                .url("https://bsky.app/profile/" + author + "/post/" + cid)
                .language(language)
                .hashtags(hashtags)
                .mentions(mentions)
                .metrics(UnifiedPost.PostMetrics.builder()
                    .likes(0)
                    .reposts(0)
                    .replies(0)
                    .quotes(0)
                    .lastUpdated(LocalDateTime.now())
                    .build())
                .metadata(createBlueskyMetadata(event))
                .processedAt(LocalDateTime.now())
                .build();
            
            // Keyword pre-filtering (critical for 8GB RAM optimization)
            if (post.containsKeywords(THREAT_KEYWORDS) && post.isTargetLanguage(TARGET_LANGUAGES)) {
                log.info("🎯 Threat keyword detected in Bluesky post: {}", post.getId());
                
                // Send to all NTEWS services via WebSocket
                serviceWebSocketClient.sendToAllServices(post);
                
                // Cache metrics for aggregation
                metricsCache.put(post.getId(), post.getMetrics());
            } else {
                log.debug("📝 Non-threatening Bluesky post filtered out: {}", post.getId());
            }
            
        } catch (Exception e) {
            log.error("❌ Error handling post event: {}", e.getMessage());
        }
    }
    
    /**
     * Handle like events - update metrics for existing posts
     */
    private void handleLikeEvent(JsonNode event) {
        try {
            String subjectUri = event.at("/commit/record/subject/uri").asText();
            UnifiedPost.PostMetrics metrics = metricsCache.get(subjectUri);
            
            if (metrics != null) {
                metrics.setLikes(metrics.getLikes() + 1);
                metrics.setLastUpdated(LocalDateTime.now());
                
                // Metrics are now handled in-memory only
                
                log.debug("❤️ Like updated for post {}: {}", subjectUri, metrics.getLikes());
            }
        } catch (Exception e) {
            log.error("❌ Error handling like event: {}", e.getMessage());
        }
    }
    
    /**
     * Handle repost events - update metrics for existing posts
     */
    private void handleRepostEvent(JsonNode event) {
        try {
            String subjectUri = event.at("/commit/record/subject/uri").asText();
            UnifiedPost.PostMetrics metrics = metricsCache.get(subjectUri);
            
            if (metrics != null) {
                metrics.setReposts(metrics.getReposts() + 1);
                metrics.setLastUpdated(LocalDateTime.now());
                
                // Metrics are now handled in-memory only
                
                log.debug("🔄 Repost updated for post {}: {}", subjectUri, metrics.getReposts());
            }
        } catch (Exception e) {
            log.error("❌ Error handling repost event: {}", e.getMessage());
        }
    }
    
    /**
     * Handle follow events (for network analysis)
     */
    private void handleFollowEvent(JsonNode event) {
        try {
            String subject = event.at("/commit/record/subject").asText();
            String author = event.at("/commit/author").asText();
            
            log.debug("👥 Follow event: {} followed {}", author, subject);
            
            // Could be used for influence analysis later
            // For now, just log to save memory
            
        } catch (Exception e) {
            log.error("❌ Error handling follow event: {}", e.getMessage());
        }
    }
    
    /**
     * Extract hashtags from text
     */
    private List<String> extractHashtags(String text) {
        List<String> hashtags = new ArrayList<>();
        Pattern pattern = Pattern.compile("#\\w+");
        Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            hashtags.add(matcher.group().substring(1));
        }
        
        return hashtags;
    }
    
    /**
     * Extract mentions from text
     */
    private List<String> extractMentions(String text) {
        List<String> mentions = new ArrayList<>();
        Pattern pattern = Pattern.compile("@\\w+");
        Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            mentions.add(matcher.group().substring(1));
        }
        
        return mentions;
    }
    
    /**
     * Simple language detection based on character patterns
     */
    private String detectLanguage(String text) {
        // Simple heuristic for Swahili/Sheng detection
        if (text.toLowerCase().matches(".*[aeiou].*[aeiou].*")) {
            // Contains common Swahili/Sheng words
            if (text.toLowerCase().matches(".*(na|la|wa|ya|za|ma|me|we|ye).*")) {
                return "sw";
            }
        }
        return "en"; // Default to English
    }
    
    /**
     * Extract handle from DID
     */
    private String extractHandle(String did) {
        // For now, return DID. In production, resolve to actual handle
        return did;
    }
    
    /**
     * Parse timestamp from ISO string
     */
    private LocalDateTime parseTimestamp(String timestampStr) {
        try {
            return LocalDateTime.parse(timestampStr.replace("Z", ""));
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
    
    /**
     * Create Bluesky-specific metadata
     */
    private Map<String, Object> createBlueskyMetadata(JsonNode event) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("uri", event.at("/commit/uri").asText());
        metadata.put("cid", event.at("/commit/cid").asText());
        metadata.put("author_did", event.at("/commit/author").asText());
        metadata.put("collection", event.at("/commit/collection").asText());
        metadata.put("new_key", "new_value");
        return metadata;
    }
    
        
    /**
     * Get metrics for a specific post
     */
    public UnifiedPost.PostMetrics getPostMetrics(String postId) {
        UnifiedPost.PostMetrics metrics = metricsCache.get(postId);
        if (metrics == null) {
            metrics = new UnifiedPost.PostMetrics();
        }
        return metrics;
    }
    
    /**
     * Get all cached metrics
     */
    public Map<String, UnifiedPost.PostMetrics> getAllMetrics() {
        return new HashMap<>(metricsCache);
    }
    
    @PreDestroy
    public void cleanup() {
        log.info("🔌 Shutting down Bluesky Jetstream WebSocket client...");
        log.info("📡 Service WebSocket client will be cleaned up automatically");
        
        log.info("✅ Bluesky Jetstream client shutdown complete");
    }
}

package com.ntews.ingestion.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntews.ingestion.client.ServiceWebSocketClient;
import com.ntews.ingestion.client.AIEngineClient;
import com.ntews.ingestion.model.UnifiedPost;
import com.ntews.ingestion.service.BlueskyMetricsAggregator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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
public class BlueskyJetstreamService {
    
    private static final Logger log = LoggerFactory.getLogger(BlueskyJetstreamService.class);

    private final ServiceWebSocketClient serviceWebSocketClient;
    private final AIEngineClient aiEngineClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BlueskyMetricsAggregator metricsAggregator;
    private WebSocket webSocket;
    
    // 8GB-Safe DID Resolution Cache
    private final Map<String, String> authorCache = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();
    
    // Target keywords for threat detection (Swahili/Sheng/English)
    private static final List<String> THREAT_KEYWORDS = Arrays.asList(
        "maandamano", "karao", "wanakambo", "tishio", "usalama",
        "protest", "unrest", "violence", "threat", "danger",
        "flood", "disaster", "emergency", "security", "attack"
    );
    
    // High-level threat keywords (immediate danger)
    private static final List<String> HIGH_THREAT_KEYWORDS = Arrays.asList(
        "attack", "violence", "danger", "emergency", "disaster", "kill", "bomb", "terror"
    );
    
    // Medium-level threat keywords (potential issues)
    private static final List<String> MEDIUM_THREAT_KEYWORDS = Arrays.asList(
        "protest", "unrest", "security", "flood", "threat", "danger", "maandamano"
    );
    
    // Low-level threat keywords (monitoring needed)
    private static final List<String> LOW_THREAT_KEYWORDS = Arrays.asList(
        "karao", "wanakambo", "tishio", "usalama"
    );
    
    // Target languages
    private static final List<String> TARGET_LANGUAGES = List.of("en", "sw", "ke");
    
    // Intelligent security keyword filter - Kenya/East Africa context
    private static final List<String> SECURITY_KEYWORDS = List.of(
        // Threat/Violence
        "maandamano", "protest", "riot", "unrest", "violence", "chaos", "clash", "attack",
        "police", "arrest", "detention", "brutality", "teargas", "rubber bullets",
        
        // Political/Social Unrest
        "karao", "demonstration", "rally", "strike", "shutdown", "curfew", "lockdown",
        "election", "vote", "rigging", "tallying", "results", "controversy",
        
        // Security/Terrorism
        "terror", "bomb", "explosion", "shooting", "gun", "weapon", "threat", "suspicious",
        "alshabaab", "extremist", "militant", "attack", "casualty", "fatality",
        
        // Emergency/Crisis
        "emergency", "crisis", "disaster", "evacuation", "rescue", "alert", "warning",
        "accident", "crash", "fire", "flood", "earthquake", "building collapse",
        
        // Cyber Security
        "hack", "breach", "cyber", "data leak", "phishing", "malware", "ransomware",
        
        // Regional Security Issues
        "bandit", "cattle rustling", "kidnap", "abduction", "hostage", "security",
        "military", "army", "forces", "operation", "crackdown"
    );
    
    // Kenya/East Africa location filter - reduce international noise
    private static final List<String> KENYA_LOCATIONS = List.of(
        "kenya", "nairobi", "mombasa", "kisumu", "nakuru", "eldoret", 
        "thika", "kitale", "garissa", "kakuma", "lamu", "malindi",
        "kilifi", "kwale", "taita taveta", "machakos", "kiambu",
        "kajiado", "narok", "bomet", "kericho", "nyeri", "muranga",
        "kirinyaga", "embu", "meru", "tharaka", "isiolo", "marsabit",
        "samburu", "turkana", "west pokot", "bungoma", "busia", "siaya",
        "kisumu", "homabay", "migori", "homa bay", "nyamira", "nyandarua",
        "laikipia", "samburu", "trans nzoia", "uasin gishu", "elgeyo marakwet",
        "nandi", "baringo", "pokot", "turkana", "wajir", "mandera", "garissa"
    );
    
    /**
     * Classify threat level based on keywords
     */
    private String classifyThreatLevel(String text) {
        if (text == null) return "none";
        
        String lowerText = text.toLowerCase();
        
        // Check high-level threats first
        for (String keyword : HIGH_THREAT_KEYWORDS) {
            if (lowerText.contains(keyword.toLowerCase())) {
                return "high";
            }
        }
        
        // Check medium-level threats
        for (String keyword : MEDIUM_THREAT_KEYWORDS) {
            if (lowerText.contains(keyword.toLowerCase())) {
                return "medium";
            }
        }
        
        // Check low-level threats
        for (String keyword : LOW_THREAT_KEYWORDS) {
            if (lowerText.contains(keyword.toLowerCase())) {
                return "low";
            }
        }
        
        return "none";
    }
    
        
    // Jetstream endpoints
    private static final String JETSTREAM_URL = "wss://jetstream1.us-west.bsky.network/subscribe";
    
    // Collections to subscribe to - ONLY posts to avoid empty content
    private static final String COLLECTIONS_PARAM = 
        "wantedCollections=app.bsky.feed.post";
    
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
            
        webSocket = client.newWebSocket(request, new BlueskyWebSocketListener());
        
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
            log.warn("Bluesky WebSocket closing: {} - {}", code, reason);
        }
    }
    
    /**
     * Handle new post events
     */
    private void handlePostEvent(JsonNode event) {
        log.debug("POST EVENT HANDLER CALLED - NEW CODE ACTIVE");
        try {
            JsonNode record = event.at("/commit/record");
            String text = record.at("/text").asText();
            
            // STOP THE LOOP: Never process empty text or author
            if (text == null || text.trim().isEmpty()) {
                // Silent return - no logging to save CPU on 8GB laptop
                return;
            }
            
            // Get DID from ROOT of JSON message (correct path)
            String author = event.has("did") ? event.get("did").asText() : "";
            if (author == null || author.trim().isEmpty()) {
                // Silent return - no logging to save CPU
                return;
            }
            
            String uri = event.at("/commit/uri").asText();
            String cid = event.at("/commit/cid").asText();
            
            // Extract timestamp from record (correct path)
            String createdAt = record.at("/createdAt").asText();
            LocalDateTime timestamp = parseTimestamp(createdAt);
            
            // Extract hashtags, mentions, and reply info
            List<String> hashtags = extractHashtags(text);
            List<String> mentions = extractMentions(text);
            
            // Check if this is a reply (for reply count tracking)
            String replyTo = record.has("reply") ? record.at("/reply").asText() : null;
            
            // If this is a reply, increment reply count for the parent post
            if (replyTo != null && !replyTo.trim().isEmpty()) {
                metricsAggregator.incrementReplies(replyTo);
            }
            
            // INTELLIGENT SECURITY FILTER - Check if post is relevant with word boundaries
            String lowerText = text.toLowerCase();
            boolean isSecurityRelevant = SECURITY_KEYWORDS.stream()
                .anyMatch(keyword -> {
                    // Use word boundaries to avoid substring false positives
                    String pattern = "\\b" + Pattern.quote(keyword) + "\\b";
                    return Pattern.compile(pattern).matcher(lowerText).find();
                });
            
            if (!isSecurityRelevant) {
                // DROP IT! Silent return to save CPU - no logging
                return;
            }
            
            // KENYA LOCATION FILTER - Reduce international noise
            boolean hasKenyaContext = KENYA_LOCATIONS.stream()
                .anyMatch(location -> Pattern.compile("\\b" + Pattern.quote(location) + "\\b", Pattern.CASE_INSENSITIVE)
                    .matcher(lowerText).find());
            
            // Allow if security keyword matches AND (has Kenya context OR is high-priority threat)
            if (!hasKenyaContext) {
                // Check if this is a high-priority global threat that should be included
                boolean isHighPriorityThreat = List.of("terror", "bomb", "attack", "shooting", "explosion")
                    .stream()
                    .anyMatch(keyword -> Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b")
                        .matcher(lowerText).find());
                
                if (!isHighPriorityThreat) {
                    // DROP IT! International noise - no logging to save CPU
                    return;
                }
            }
            
            // Detect language (simple heuristic for now)
            String language = detectLanguage(text);
            
            // Create unified post with comprehensive metadata
            UnifiedPost post = UnifiedPost.builder()
                .id(cid)
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
                .metadata(new HashMap<>())
                .processedAt(LocalDateTime.now())
                .build();
            
            // Classify threat level and store accordingly
            String threatLevel = classifyThreatLevel(text);
            
            // 8GB-Safe Lazy DID Resolution - Only for high-priority threats
            String readableAuthor = author;
            if ("high".equals(threatLevel)) {
                readableAuthor = resolveHandle(author);
                log.info("🎯 RESOLVED HIGH-THREAT AUTHOR: {} -> {}", author, readableAuthor);
            }
            
            // Update post with resolved author
            post.author = readableAuthor;
            
            // Store post in metrics aggregator with intelligent threat-based storage
            metricsAggregator.storePost(post, threatLevel);
            
            // Analyze with AI Engine for enhanced threat detection
            try {
                aiEngineClient.analyzeBlueskyPost(post);
                log.info("🤖 AI Engine analysis completed for post: {}", post.id);
            } catch (Exception e) {
                log.warn("⚠️ AI Engine analysis failed for post {}: {}", post.id, e.getMessage());
            }
            
            log.info("🚨 SECURITY POST: ID={}, Threat={}, Length={}, Author={}", 
                post.id, threatLevel, text.length(), readableAuthor);
            
            // Send to all NTEWS services via WebSocket (ONLY for security-relevant posts)
            serviceWebSocketClient.sendToAllServices(post);
            
            // Log threat level for monitoring
            if ("none".equals(threatLevel)) {
                log.debug("Non-threatening post stored: {}", post.id);
            } else if ("low".equals(threatLevel)) {
                log.warn("LOW THREAT: {} | Author: {}", post.id, post.author);
            } else if ("medium".equals(threatLevel)) {
                log.error("MEDIUM THREAT: {} | Author: {} | Keywords: {}", 
                    post.id, post.author,
                    java.util.Arrays.toString(THREAT_KEYWORDS.stream().filter(keyword -> post.text.toLowerCase().contains(keyword.toLowerCase())).toArray(String[]::new)));
            } else if ("high".equals(threatLevel)) {
                log.error("HIGH THREAT: {} | Author: {} | Keywords: {}", 
                    post.id, post.author,
                    java.util.Arrays.toString(THREAT_KEYWORDS.stream().filter(keyword -> post.text.toLowerCase().contains(keyword.toLowerCase())).toArray(String[]::new)));
            }
            
        } catch (Exception e) {
            log.error("Error handling post event: {}", e.getMessage());
        }
    }
    
    /**
     * Handle like events - update metrics for existing posts
     */
    private void handleLikeEvent(JsonNode event) {
        try {
            String subjectUri = event.at("/commit/record/subject/uri").asText();
            
            // Only track metrics for posts that are already in our cache (security-relevant posts)
            if (metricsAggregator.getPostMetrics(subjectUri) != null) {
                metricsAggregator.incrementLikes(subjectUri);
                log.debug("❤️ Like incremented for security post: {}", subjectUri);
            }
        } catch (Exception e) {
            log.debug("❌ Error handling like event: {}", e.getMessage());
        }
    }
    
    /**
     * Handle repost events - update metrics for existing posts
     */
    private void handleRepostEvent(JsonNode event) {
        try {
            String subjectUri = event.at("/commit/record/subject/uri").asText();
            UnifiedPost.PostMetrics metrics = metricsAggregator.getPostMetrics(subjectUri);
            
            if (metrics != null) {
                metricsAggregator.incrementReposts(subjectUri);
                log.debug("🔄 Repost updated for post {}: {}", subjectUri);
            }
        } catch (Exception e) {
            log.error("❌ Error handling repost event: {}", e.getMessage());
        }
    }
    
    /**
     * Handle follow events
     */
    private void handleFollowEvent(JsonNode event) {
        try {
            String subject = event.at("/commit/record/subject").asText();
            log.debug("👥 Follow event: {}", subject);
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
     * 8GB-Safe Lazy DID Resolution with Cache
     * Only resolves handles for high-priority threats to save RAM/CPU
     */
    private String resolveHandle(String did) {
        // 1. Check Cache first (Saves RAM and Network)
        if (authorCache.containsKey(did)) {
            return authorCache.get(did);
        }

        try {
            // 2. Public PLC Directory API (No Auth required)
            String url = "https://plc.directory/" + did;
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            
            if (response != null && response.has("alsoKnownAs") && response.get("alsoKnownAs").size() > 0) {
                // 3. Extract the handle from the DID document
                String handle = response.get("alsoKnownAs").get(0).asText().replace("at://", "");
                
                // Cache the result for future use
                authorCache.put(did, handle);
                log.debug("🎯 RESOLVED DID: {} -> {}", did, handle);
                return handle;
            }
        } catch (Exception e) {
            log.debug("❌ Failed to resolve DID: {} - {}", did, e.getMessage());
        }
        
        // Fallback to raw DID if API fails
        return did;
    }
    
    /**
     * Get all cached metrics
     */
    public Map<String, UnifiedPost.PostMetrics> getAllMetrics() {
        return metricsAggregator.getAllMetrics();
    }
    
    /**
     * Create comprehensive Bluesky metadata from event
     */
    private Map<String, Object> createComprehensiveBlueskyMetadata(JsonNode event, String text, List<String> hashtags, List<String> mentions) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("uri", event.at("/commit/uri").asText());
        metadata.put("cid", event.at("/commit/cid").asText());
        metadata.put("author_did", event.at("/commit/author").at("/did").asText());
        metadata.put("reply_count", event.at("/commit/replyCount").asInt());
        metadata.put("embed_images", event.at("/commit/embed").at("/images"));
        metadata.put("embed_external", event.at("/commit/embed").at("/external"));
        
        // Enhanced metadata for comprehensive analysis
        metadata.put("text_length", text != null ? text.length() : 0);
        metadata.put("hashtag_count", hashtags != null ? hashtags.size() : 0);
        metadata.put("mention_count", mentions != null ? mentions.size() : 0);
        metadata.put("word_count", text != null ? text.split("\\s+").length : 0);
        metadata.put("language_detected", detectLanguage(text));
        metadata.put("sentiment_score", calculateSentimentScore(text));
        metadata.put("engagement_potential", calculateEngagementPotential(hashtags, mentions));
        
        return metadata;
    }
    
    /**
     * Calculate sentiment score (simple heuristic)
     */
    private double calculateSentimentScore(String text) {
        if (text == null) return 0.5;
        
        String lowerText = text.toLowerCase();
        int positive = 0, negative = 0;
        
        String[] positiveWords = {"good", "great", "excellent", "amazing", "love", "happy", "wonderful"};
        String[] negativeWords = {"bad", "terrible", "awful", "hate", "angry", "sad", "worst"};
        
        for (String word : positiveWords) {
            if (lowerText.contains(word)) positive++;
        }
        
        for (String word : negativeWords) {
            if (lowerText.contains(word)) negative++;
        }
        
        if (positive + negative == 0) return 0.5; // neutral
        return (double) (positive - negative) / (positive + negative);
    }
    
    /**
     * Calculate engagement potential
     */
    private double calculateEngagementPotential(List<String> hashtags, List<String> mentions) {
        double hashtagScore = hashtags != null ? hashtags.size() * 2.0 : 0;
        double mentionScore = mentions != null ? mentions.size() * 3.0 : 0;
        return hashtagScore + mentionScore;
    }
    
        
    @PreDestroy
    public void cleanup() {
        if (webSocket != null) {
            webSocket.close(1000, "Service shutting down");
        }
        log.info("🔵 Bluesky Jetstream WebSocket client shutdown complete");
    }
}

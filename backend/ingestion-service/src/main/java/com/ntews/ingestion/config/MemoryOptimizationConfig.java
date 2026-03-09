package com.ntews.ingestion.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Memory optimization configuration for 8GB laptop deployment
 * Simplified for WebSocket-only architecture
 */
@Configuration
@Slf4j
public class MemoryOptimizationConfig {

    /**
     * JVM memory optimization recommendations for 8GB laptop
     * 
     * Memory allocation strategy:
     * - JVM Heap: 1GB (leaves 7GB for other processes)
     * - Python AI Models: 2GB (DistilBERT, transformers)
     * - OS & Browser: 3GB (Windows, Chrome, development tools)
     * - Buffer: 2GB (system overhead, other services)
     * 
     * Total: 8GB (optimal for laptop deployment)
     */
    public static class JVMTuning {
        
        // Recommended JVM arguments for 8GB laptop
        public static final String[] RECOMMENDED_JVM_OPTS = {
            "-Xmx1g",                    // Maximum heap size 1GB
            "-Xms512m",                  // Initial heap size 512MB
            "-XX:+UseG1GC",              // G1 garbage collector
            "-XX:MaxGCPauseMillis=200",   // Target 200ms GC pauses
            "-XX:+UseStringDeduplication", // Deduplicate strings
            "-XX:+UseCompressedOops",       // Compress object pointers
            "-XX:+PrintGCDetails",         // Monitor GC behavior
            "-XX:+PrintGCTimeStamps",      // Track GC timing
            "-XX:+UseContainerSupport",       // Container-aware (if needed)
            "-XX:MaxRAMPercentage=12.5"   // Use 12.5% of system RAM
        };
        
        // Memory monitoring thresholds
        public static final double HEAP_USAGE_WARNING = 0.8;    // 80% heap usage
        public static final double HEAP_USAGE_CRITICAL = 0.9;  // 90% heap usage
        
        // Performance tuning parameters
        public static final int THREAD_POOL_SIZE = 8;          // CPU cores
        public static final int CONNECTION_POOL_SIZE = 20;      // WebSocket connections
        public static final int CACHE_SIZE_LIMIT = 10000;      // Metrics cache
    }
    
    /**
     * WebSocket client optimization settings
     */
    @Bean
    public WebSocketClientConfig webSocketClientConfig() {
        return new WebSocketClientConfig();
    }
    
    public static class WebSocketClientConfig {
        
        // Connection timeouts (seconds)
        public int connectTimeout = 30;
        public int readTimeout = 0;        // No timeout for streaming
        public int writeTimeout = 30;
        public int pingInterval = 30;
        
        // Connection pool settings
        public int maxIdleConnections = 10;
        public int keepAliveDuration = 300;   // 5 minutes
        
        // Retry configuration
        public int maxRetries = 3;
        public int retryDelay = 5000;        // 5 seconds
        public boolean enableExponentialBackoff = true;
        
        // Buffer sizes
        public int receiveBufferSize = 8192;    // 8KB
        public int sendBufferSize = 8192;       // 8KB
    }
    
    /**
     * Metrics aggregation optimization
     */
    @Bean
    public MetricsConfig metricsConfig() {
        return new MetricsConfig();
    }
    
    public static class MetricsConfig {
        
        // Cache management
        public int maxCacheSize = JVMTuning.CACHE_SIZE_LIMIT;
        public long cleanupIntervalMs = 300000;    // 5 minutes
        public long retentionPeriodMs = 21600000;   // 6 hours
        
        // Batch processing
        public int batchSize = 100;
        public int flushIntervalMs = 10000;        // 10 seconds
        
        // Performance monitoring
        public boolean enableDetailedLogging = false;
        public int logSampleRate = 100;            // Log every 100th item
    }
    
    /**
     * System resource monitoring
     */
    @Bean
    public SystemMonitorConfig systemMonitorConfig() {
        return new SystemMonitorConfig();
    }
    
    public static class SystemMonitorConfig {
        
        // Memory monitoring
        public boolean enableMemoryMonitoring = true;
        public long memoryCheckIntervalMs = 30000;   // 30 seconds
        
        // CPU monitoring
        public boolean enableCpuMonitoring = true;
        public long cpuCheckIntervalMs = 60000;       // 1 minute
        
        // Connection monitoring
        public boolean enableConnectionMonitoring = true;
        public long connectionCheckIntervalMs = 15000;  // 15 seconds
        
        // Alert thresholds
        public double memoryAlertThreshold = JVMTuning.HEAP_USAGE_WARNING;
        public double cpuAlertThreshold = 0.8;        // 80% CPU usage
        public int connectionAlertThreshold = 5;          // Max failed connections
    }
    
    /**
     * Log memory usage on startup
     */
    public MemoryOptimizationConfig() {
        log.info("🧠 Memory Optimization Config - WebSocket Only Architecture");
        log.info("📊 Recommended JVM settings:");
        for (String opt : JVMTuning.RECOMMENDED_JVM_OPTS) {
            log.info("   {}", opt);
        }
        log.info("💾 Metrics cache limit: {}", JVMTuning.CACHE_SIZE_LIMIT);
        log.info("🔗 WebSocket connection pool: {}", JVMTuning.CONNECTION_POOL_SIZE);
        log.info("⚡ Thread pool size: {}", JVMTuning.THREAD_POOL_SIZE);
    }
}

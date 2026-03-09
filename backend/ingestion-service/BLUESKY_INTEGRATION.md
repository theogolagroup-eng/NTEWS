# Bluesky Jetstream Integration for NTEWS

## Overview

This integration provides real-time data ingestion from Bluesky's Jetstream WebSocket API, optimized for 8GB laptop deployment. It replaces expensive X API access with free, high-volume Bluesky data while maintaining threat detection capabilities.

## Architecture

### Multi-Stream Pipeline

```
Bluesky Jetstream → WebSocket Client → Keyword Filter → Kafka → AI Engine → Database → Dashboard
```

### Data Flow

1. **Ingestion**: Connect to Bluesky Jetstream WebSocket
2. **Filtering**: Pre-filter by keywords and language (99% reduction in AI load)
3. **Metrics**: Aggregate likes, reposts, replies in real-time
4. **Processing**: Send only relevant posts to AI engine
5. **Storage**: Persist metrics in Redis for dashboard

## Features

### 🎯 Smart Filtering
- **Keyword Pre-filter**: Only processes posts containing threat keywords
- **Language Detection**: Focuses on English, Swahili, and Sheng
- **Multi-stream**: Monitors posts, likes, reposts, and follows

### 📊 Real-time Metrics
- **Engagement Tracking**: Likes, reposts, replies, quotes
- **Trending Detection**: Identifies high-engagement posts
- **Threat Prioritization**: Ranks posts by engagement score

### 💾 Memory Optimization
- **8GB Laptop Friendly**: JVM limited to 1GB heap
- **Intelligent Caching**: 10,000 post limit with automatic cleanup
- **Batch Processing**: Reduces database writes

## Configuration

### Application Properties

```yaml
bluesky:
  jetstream-url: wss://jetstream1.us-west.bsky.network/subscribe
  threat-keywords:
    - maandamano
    - karao
    - protest
    - unrest
  target-languages:
    - en
    - sw
    - ke
  max-cache-size: 10000
```

### JVM Settings (8GB Laptop)

```bash
-Xmx1g -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

## API Endpoints

### Metrics & Monitoring

- `GET /api/bluesky/metrics/summary` - Overall metrics
- `GET /api/bluesky/metrics/top-posts` - Top posts by engagement
- `GET /api/bluesky/metrics/trending` - Trending posts
- `GET /api/bluesky/health` - Service health check

### Admin Functions

- `POST /api/bluesky/admin/cleanup` - Force cleanup old metrics
- `POST /api/bluesky/admin/persist` - Force persist to Redis

## Memory Management

### Cache Strategy
- **In-memory cache**: 10,000 most recent posts
- **Redis persistence**: Every 5 minutes
- **Automatic cleanup**: Remove posts older than 6 hours

### Performance Metrics
- **Memory usage**: ~500MB for Java service
- **CPU usage**: Minimal (string matching only)
- **Throughput**: 100+ posts/second processing

## Threat Detection

### Multi-Language Keywords

**Swahili/Sheng:**
- maandamano (protest)
- karao (chaos)
- wanakambo (troublemakers)
- tishio (threat)
- usalama (security)

**English:**
- protest, unrest, violence
- threat, danger, flood
- disaster, emergency, security, attack

### Classification Flow

1. **Collect**: Raw post from Bluesky
2. **Filter**: Keyword and language check
3. **Extract**: Hashtags, mentions, entities
4. **Classify**: AI engine threat analysis
5. **Score**: Engagement-based prioritization
6. **Store**: Results in database
7. **Alert**: Dashboard notification

## Deployment

### Prerequisites

- Java 17+
- Kafka (localhost:9092)
- Redis (localhost:6379)
- MongoDB (for post storage)

### Startup

```bash
# Using optimized script
./run_ingestion_with_bluesky.bat

# Or manually
java -Xmx1g -Xms512m -XX:+UseG1GC \
     -Dspring.profiles.active=bluesky \
     -jar ingestion-service-0.0.1-SNAPSHOT.jar
```

### Monitoring

- **Health Check**: `GET /api/bluesky/health`
- **Cache Stats**: `GET /api/bluesky/cache/stats`
- **JVM Metrics**: Actuator endpoints

## Integration with AI Engine

### Kafka Message Format

```json
{
  "id": "at://did:plc:xyz/app.bsky.feed.post/abc123",
  "source": "bluesky",
  "author": "did:plc:xyz",
  "text": "Maandamano scheduled for tomorrow CBD",
  "timestamp": "2026-03-09T10:15:00Z",
  "language": "sw",
  "hashtags": ["maandamano", "cbd"],
  "metrics": {
    "likes": 15,
    "reposts": 8,
    "replies": 3,
    "engagement_score": 47.5
  }
}
```

### AI Engine Processing

1. Receive filtered posts from Kafka
2. Run threat classification models
3. Generate risk scores
4. Store results in prediction database
5. Update dashboard in real-time

## Performance Optimization

### For 8GB Laptop

| Component | Memory | CPU | Optimization |
|-----------|--------|-----|-------------|
| JVM Heap | 1GB | Low | G1GC, string deduplication |
| AI Models | 2GB | Medium | DistilBERT (not BERT) |
| OS/Browser | 3GB | Variable | Chrome memory management |
| Buffer | 2GB | - | System overhead |

### Bottleneck Prevention

- **Pre-filtering**: 99% reduction in AI processing
- **Batch operations**: Reduce database calls
- **Async processing**: Non-blocking WebSocket
- **Memory limits**: Prevent heap overflow

## Troubleshooting

### Common Issues

1. **WebSocket Connection Failed**
   - Check internet connectivity
   - Verify Jetstream endpoint
   - Try backup endpoint

2. **High Memory Usage**
   - Reduce `max-cache-size`
   - Increase cleanup frequency
   - Check for memory leaks

3. **Kafka Connection Issues**
   - Verify Kafka is running
   - Check topic permissions
   - Review network configuration

4. **Redis Connection Errors**
   - Confirm Redis server status
   - Check connection settings
   - Verify authentication

### Debug Mode

Enable detailed logging:

```yaml
logging:
  level:
    com.ntews.ingestion.service.BlueskyJetstreamService: DEBUG
bluesky:
  enable-detailed-logging: true
```

## Future Enhancements

### Planned Features

- **Reddit Integration**: Subreddit monitoring
- **RSS Feeds**: News source integration
- **Telegram Channels**: Public channel monitoring
- **Advanced ML**: Better language detection
- **Geo-fencing**: Location-based filtering

### Scalability

- **Multi-instance**: Horizontal scaling
- **Load Balancing**: Multiple Jetstream connections
- **Distributed Cache**: Redis Cluster
- **Stream Processing**: Apache Flink integration

## Support

For issues and questions:
1. Check logs in `logs/ingestion-service.log`
2. Verify configuration in `application-bluesky.yml`
3. Test connectivity with health endpoint
4. Monitor JVM metrics with VisualVM

---

**Note**: This integration is designed specifically for 8GB laptop deployment while maintaining production-level threat detection capabilities.

@echo off
REM NTEWS Ingestion Service with Bluesky Integration - WebSocket Only Architecture
REM Optimized for 8GB laptop deployment

echo 🚀 Starting NTEWS Ingestion Service with Bluesky Jetstream...
echo 📊 Memory settings optimized for 8GB laptop
echo 🔗 Connecting to Bluesky Jetstream WebSocket...
echo 📡 Direct WebSocket communication to NTEWS services:
echo    - Alert Service: ws://localhost:8081/ws/alerts
echo    - Prediction Service: ws://localhost:8082/ws/predictions  
echo    - Intelligence Service: ws://localhost:8083/ws/intelligence
echo.

REM JVM memory optimization for 8GB laptop
REM -Xmx1g: Maximum heap size 1GB (leaves 7GB for OS, Python AI models, browser)
REM -Xms512m: Initial heap size 512MB
REM -XX:+UseG1GC: G1 garbage collector for better pause times
REM -XX:MaxGCPauseMillis=200: Target 200ms GC pauses
REM -XX:+UseStringDeduplication: Deduplicate strings to save memory
REM -XX:+UseCompressedOops: Compress object pointers
REM -XX:+PrintGCDetails: Monitor GC behavior
REM -XX:+PrintGCTimeStamps: Track GC timing

set JAVA_OPTS=-Xmx1g -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -XX:+UseCompressedOops

REM Spring Boot profile for Bluesky configuration
set SPRING_PROFILES_ACTIVE=bluesky

REM Start the application
echo 🔄 Starting with JVM settings: %JAVA_OPTS%
echo 📋 Using Spring profile: %SPRING_PROFILES_ACTIVE%
echo 🏗️  Architecture: WebSocket-only (no Kafka/Redis dependencies)
echo.

java %JAVA_OPTS% -jar build\libs\ingestion-service-0.0.1-SNAPSHOT.jar

if %ERRORLEVEL% EQU 0 (
    echo ✅ Ingestion service started successfully
    echo 📊 Monitor at: http://localhost:8080/api/bluesky/health
    echo 📈 Metrics at: http://localhost:8080/api/bluesky/metrics/summary
) else (
    echo ❌ Failed to start ingestion service
    echo 💡 Verify Bluesky Jetstream connectivity
    echo 💡 Ensure NTEWS services are running on their respective ports
    echo 💡 Check network connectivity to wss://jetstream1.us-west.bsky.network
)

pause

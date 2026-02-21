import requests
import json
import time
from datetime import datetime

def test_endpoint(url, method='POST', data=None, headers=None):
    try:
        if method == 'GET':
            response = requests.get(url, headers=headers)
        elif method == 'PUT':
            response = requests.put(url, json=data, headers=headers)
        elif method == 'DELETE':
            response = requests.delete(url, headers=headers)
        else:
            response = requests.post(url, json=data, headers=headers)
        
        print(f"✓ {method} {url}: {response.status_code}")
        if response.status_code == 200:
            try:
                resp_json = response.json()
                print(f"  Response: {json.dumps(resp_json, indent=2)[:200]}...")
            except:
                print(f"  Response: {response.text[:200]}...")
        else:
            print(f"  Error: {response.text}")
        return response.status_code == 200
    except Exception as e:
        print(f"✗ {url}: {e}")
        return False

def test_ai_engine():
    print("\n" + "="*60)
    print("🤖 TESTING AI ENGINE ENDPOINTS (Port 8000)")
    print("="*60)
    
    ai_base = "http://localhost:8000"
    
    # Health and Status Endpoints
    print("\n📊 Health & Status:")
    test_endpoint(f"{ai_base}/health", "GET")
    test_endpoint(f"{ai_base}/root", "GET")
    test_endpoint(f"{ai_base}/stats", "GET")
    test_endpoint(f"{ai_base}/models", "GET")
    test_endpoint(f"{ai_base}/capabilities", "GET")
    
    # Core Analysis Endpoints
    print("\n🔍 Core Analysis:")
    test_endpoint(f"{ai_base}/analyze", "POST", {
        "id": "test-001",
        "type": "threat",
        "source": "test",
        "sourceType": "manual",
        "content": "Suspicious activity detected near CBD area",
        "timestamp": datetime.now().isoformat(),
        "severity": "high"
    })
    
    test_endpoint(f"{ai_base}/predict", "POST", {
        "reports": [],
        "lookback_days": 30,
        "forecast_hours": 24,
        "forecast_type": "risk_trend"
    })
    
    test_endpoint(f"{ai_base}/predict/hotspots", "POST", {
        "center_latitude": -1.2864,
        "center_longitude": 36.8172,
        "radius_km": 50,
        "time_window_hours": 24
    })
    
    test_endpoint(f"{ai_base}/prediction-analysis", "POST", {
        "reports": [],
        "lookback_days": 7,
        "forecast_hours": 12
    })
    
    # NLP Endpoints
    print("\n🧠 NLP Analysis:")
    test_endpoint(f"{ai_base}/nlp/analyze-text", "POST", {
        "text": "Multiple gunshots heard in Eastlands area, people running",
        "context": "security incident"
    })
    
    test_endpoint(f"{ai_base}/nlp/analyze-alert", "POST", {
        "alert_id": "alert-001",
        "title": "Security Threat",
        "description": "Suspicious armed individuals reported",
        "category": "SECURITY",
        "source": "public"
    })
    
    test_endpoint(f"{ai_base}/nlp/batch-analyze", "POST", [
        {"text": "Protest gathering at Uhuru Park", "context": "social unrest"},
        {"text": "Theft reported in Westlands", "context": "criminal activity"}
    ])
    
    test_endpoint(f"{ai_base}/nlp/capabilities", "GET")

def test_alert_service():
    print("\n" + "="*60)
    print("🚨 TESTING ALERT SERVICE ENDPOINTS (Port 8081)")
    print("="*60)
    
    alert_base = "http://localhost:8081/api/alerts"
    
    # Basic CRUD
    print("\n📝 Basic CRUD:")
    test_endpoint(f"{alert_base}", "GET")
    test_endpoint(f"{alert_base}/active", "GET")
    test_endpoint(f"{alert_base}/unacknowledged", "GET")
    test_endpoint(f"{alert_base}/statistics", "GET")
    test_endpoint(f"{alert_base}/dashboard/summary", "GET")
    
    # Create Alert
    test_endpoint(f"{alert_base}", "POST", {
        "title": "Test Security Alert",
        "description": "Suspicious activity detected",
        "severity": "HIGH",
        "status": "ACTIVE",
        "category": "SECURITY",
        "source": "Test System",
        "location": "Nairobi CBD"
    })
    
    # NLP Analysis
    print("\n🧠 Alert NLP Analysis:")
    test_endpoint(f"{alert_base}/nlp/analyze-text", "POST", {
        "text": "Armed robbery in progress at bank",
        "context": "criminal activity"
    })
    
    test_endpoint(f"{alert_base}/nlp/capabilities", "GET")

def test_prediction_service():
    print("\n" + "="*60)
    print("🔮 TESTING PREDICTION SERVICE ENDPOINTS (Port 8082)")
    print("="*60)
    
    pred_base = "http://localhost:8082/api/predictions"
    ai_base = "http://localhost:8082/api/ai-engine"
    
    # Prediction Endpoints
    print("\n📈 Predictions:")
    test_endpoint(f"{pred_base}/forecasts", "GET")
    test_endpoint(f"{pred_base}/forecasts/current", "GET")
    test_endpoint(f"{pred_base}/hotspots", "GET")
    test_endpoint(f"{pred_base}/risk-trends", "GET")
    test_endpoint(f"{pred_base}/dashboard/summary", "GET")
    test_endpoint(f"{pred_base}/location-risk", "GET")
    
    test_endpoint(f"{pred_base}/generate-forecast", "POST", {
        "forecastType": "risk_trend",
        "parameters": {"hours": 24}
    })
    
    # AI Engine Integration
    print("\n🤖 AI Engine Integration:")
    test_endpoint(f"{ai_base}/health", "GET")
    test_endpoint(f"{ai_base}/models", "GET")
    test_endpoint(f"{ai_base}/stats", "GET")
    test_endpoint(f"{ai_base}/capabilities", "GET")
    test_endpoint(f"{ai_base}/prediction-analysis", "GET")

def test_intelligence_service():
    print("\n" + "="*60)
    print("🕵️ TESTING INTELLIGENCE SERVICE ENDPOINTS (Port 8083)")
    print("="*60)
    
    intel_base = "http://localhost:8083/api/intelligence"
    
    print("\n📋 Intelligence Reports:")
    test_endpoint(f"{intel_base}/reports", "GET")
    test_endpoint(f"{intel_base}/dashboard/summary", "GET")
    test_endpoint(f"{intel_base}/threat-trends", "GET")
    test_endpoint(f"{intel_base}/threat-map", "GET")
    
    # Create Report
    test_endpoint(f"{intel_base}/reports", "POST", {
        "title": "Intelligence Brief",
        "threatLevel": "HIGH",
        "category": "SECURITY",
        "content": "Gathering intelligence on suspicious activities"
    })

def test_ingestion_service():
    print("\n" + "="*60)
    print("📥 TESTING INGESTION SERVICE ENDPOINTS (Port 8084)")
    print("="*60)
    
    ingest_base = "http://localhost:8084/api/ingestion"
    
    print("\n📊 Data Ingestion:")
    test_endpoint(f"{ingest_base}/status", "GET")
    
    # Social Media Ingestion
    test_endpoint(f"{ingest_base}/social-media", "POST", {
        "id": "social-001",
        "platform": "twitter",
        "postId": "post-123",
        "userId": "user-456",
        "username": "user123",
        "displayName": "User 123",
        "content": "Protest forming at central park #nairobi",
        "language": "en",
        "likes": 15,
        "shares": 8,
        "comments": 3,
        "views": 150,
        "location": "Nairobi, Kenya",
        "geoTagLatitude": "-1.2864",
        "geoTagLongitude": "36.8172",
        "hasMedia": False,
        "postedAt": datetime.now().isoformat(),
        "ingestedAt": datetime.now().isoformat(),
        "sentimentScore": 0.5,
        "sentimentLabel": "neutral",
        "threatKeywords": ["protest", "central park"],
        "threatScore": 0.3,
        "isThreat": False,
        "verified": False,
        "verificationScore": 0.7
    })
    
    # CCTV Ingestion
    test_endpoint(f"{ingest_base}/cctv", "POST", {
        "id": "cctv-001",
        "cameraId": "CAM-001",
        "location": "Central Park",
        "latitude": "-1.2864",
        "longitude": "36.8172",
        "streamUrl": "rtmp://example.com/stream",
        "format": "H.264",
        "duration": 300,
        "frameRate": 30,
        "resolution": "1920x1080",
        "fileSize": 1048576,
        "detections": [
            {
                "type": "person",
                "label": "adult",
                "confidence": 0.95,
                "boundingBox": {
                    "x": 100.0,
                    "y": 150.0,
                    "width": 50.0,
                    "height": 100.0
                },
                "detectedAt": datetime.now().isoformat(),
                "attributes": {"age_range": "25-35", "gender": "male"}
            }
        ],
        "crowdDensity": 0.3,
        "hasAnomaly": False,
        "anomalyScore": 0.1,
        "recordedAt": datetime.now().isoformat(),
        "ingestedAt": datetime.now().isoformat(),
        "processingStatus": "completed",
        "metadata": {"source": "ip_camera", "quality": "high"}
    })
    
    # Cyber Feed Ingestion
    test_endpoint(f"{ingest_base}/cyber-feed", "POST", {
        "id": "cyber-001",
        "source": "firewall",
        "sourceType": "cyber",
        "contentType": "log",
        "rawContent": "Multiple failed login attempts detected from IP 192.168.1.100",
        "processedContent": "Suspicious login activity detected",
        "location": {
            "latitude": "-1.2864",
            "longitude": "36.8172",
            "city": "Nairobi",
            "region": "Nairobi County",
            "country": "Kenya",
            "address": "Central Business District",
            "confidence": 0.95
        },
        "timestamp": datetime.now().isoformat(),
        "ingestedAt": datetime.now().isoformat(),
        "status": "processed",
        "metadata": {
            "source_ip": "192.168.1.100",
            "attempt_count": 5,
            "time_window": "10 minutes",
            "severity": "medium"
        },
        "aiThreatScore": 0.7,
        "aiRiskLevel": "medium",
        "aiConfidence": 0.85,
        "aiKeywords": ["failed login", "multiple attempts", "suspicious ip"],
        "aiAnalysis": "Potential brute force attack detected",
        "predictedSeverity": "medium",
        "timeToCritical": 30,
        "evolutionTrend": "increasing"
    })

def test_auth_service():
    print("\n" + "="*60)
    print("🔐 TESTING AUTH SERVICE ENDPOINTS (Port 8085)")
    print("="*60)
    
    auth_base = "http://localhost:8085/api/auth"
    
    print("\n🔑 Authentication:")
    test_endpoint(f"{auth_base}/status", "GET")
    
    # Login
    login_response = test_endpoint(f"{auth_base}/login", "POST", {
        "username": "admin",
        "password": "admin123"
    })
    
    # If login successful, test protected endpoints
    if login_response:
        token = "test-token-123"  # Mock token for testing
        headers = {"Authorization": f"Bearer {token}"}
        
        test_endpoint(f"{auth_base}/validate", "GET", headers=headers)
        test_endpoint(f"{auth_base}/logout", "POST", headers=headers)

def test_api_gateway():
    print("\n" + "="*60)
    print("🌐 TESTING API GATEWAY ENDPOINTS (Port 8080)")
    print("="*60)
    
    gateway_base = "http://localhost:8080"
    
    print("\n🚪 Gateway Routes:")
    # Test gateway routing to each service
    test_endpoint(f"{gateway_base}/api/alerts", "GET")
    test_endpoint(f"{gateway_base}/api/predictions/forecasts", "GET")
    test_endpoint(f"{gateway_base}/api/intelligence/reports", "GET")
    test_endpoint(f"{gateway_base}/api/ingestion/status", "GET")
    test_endpoint(f"{gateway_base}/api/auth/status", "GET")
    
    # Test AI Engine through gateway
    test_endpoint(f"{gateway_base}/analyze", "POST", {
        "id": "gateway-test",
        "content": "Test through gateway"
    })

def main():
    print("🚀 COMPREHENSIVE NTEWS BACKEND API TESTING")
    print("="*60)
    print(f"Started at: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    
    # Test all services
    test_ai_engine()
    time.sleep(1)
    
    test_alert_service()
    time.sleep(1)
    
    test_prediction_service()
    time.sleep(1)
    
    test_intelligence_service()
    time.sleep(1)
    
    test_ingestion_service()
    time.sleep(1)
    
    test_auth_service()
    time.sleep(1)
    
    test_api_gateway()
    
    print("\n" + "="*60)
    print("✅ TESTING COMPLETE")
    print("="*60)
    print(f"Finished at: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("\n📝 Note: Some endpoints may return 404/500 if services are not running")
    print("📝 Ensure all services are started before running tests")

if __name__ == "__main__":
    main()

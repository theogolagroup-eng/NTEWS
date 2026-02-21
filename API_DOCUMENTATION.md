# NTEWS API Documentation

## Overview
Complete API documentation for the National Threat Early Warning System (NTEWS) backend services.

## Architecture

### Services & Ports
- **API Gateway**: `http://localhost:8080` (Main entry point)
- **AI Engine**: `http://localhost:8000` (Python/FastAPI)
- **Auth Service**: `http://localhost:8085` (Java/SpringBoot)
- **Alert Service**: `http://localhost:8081` (Java/SpringBoot)
- **Prediction Service**: `http://localhost:8082` (Java/SpringBoot)
- **Intelligence Service**: `http://localhost:8083` (Java/SpringBoot)
- **Ingestion Service**: `http://localhost:8084` (Java/SpringBoot)

---

## 🤖 AI Engine API (Port 8000)

### Health & Status
```http
GET /health
GET /root
GET /stats
GET /models
GET /capabilities
```

### Core Analysis
```http
POST /analyze
Content-Type: application/json

{
  "id": "threat-001",
  "type": "threat",
  "source": "manual",
  "sourceType": "user_report",
  "content": "Suspicious activity detected",
  "timestamp": "2026-02-19T15:20:00Z",
  "severity": "high"
}
```

```http
POST /predict
Content-Type: application/json

{
  "reports": [],
  "lookback_days": 30,
  "forecast_hours": 24,
  "forecast_type": "risk_trend"
}
```

```http
POST /predict/hotspots
Content-Type: application/json

{
  "center_latitude": -1.2864,
  "center_longitude": 36.8172,
  "radius_km": 50,
  "time_window_hours": 24
}
```

### NLP Analysis
```http
POST /nlp/analyze-text
Content-Type: application/json

{
  "text": "Multiple gunshots heard in the area",
  "context": "security incident"
}
```

```http
POST /nlp/analyze-alert
Content-Type: application/json

{
  "alert_id": "alert-001",
  "title": "Security Threat",
  "description": "Suspicious armed individuals reported",
  "category": "SECURITY",
  "source": "public"
}
```

```http
POST /nlp/batch-analyze
Content-Type: application/json

[
  {"text": "Protest gathering at park", "context": "social unrest"},
  {"text": "Theft reported in area", "context": "criminal activity"}
]
```

---

## 🔐 Auth Service API (Port 8085)

### Authentication
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "status": "success",
  "token": "uuid-token-here",
  "user": {
    "id": "1",
    "username": "admin",
    "role": "admin",
    "permissions": ["read", "write", "delete"]
  },
  "expiresIn": 3600
}
```

```http
POST /api/auth/logout
Authorization: Bearer {token}
```

```http
GET /api/auth/validate
Authorization: Bearer {token}
```

```http
GET /api/auth/status
```

---

## 🚨 Alert Service API (Port 8081)

### Alert Management
```http
GET /api/alerts?page=0&size=20&severity=HIGH&status=ACTIVE
```

```http
POST /api/alerts
Content-Type: application/json

{
  "title": "Security Alert",
  "description": "Suspicious activity detected",
  "severity": "HIGH",
  "status": "ACTIVE",
  "category": "SECURITY",
  "source": "User Report",
  "location": "Nairobi CBD"
}
```

```http
PUT /api/alerts/{id}
Content-Type: application/json

{
  "title": "Updated Alert",
  "description": "Updated description",
  "severity": "MEDIUM"
}
```

### Alert Operations
```http
POST /api/alerts/{id}/acknowledge
POST /api/alerts/{id}/resolve
Content-Type: application/json

{
  "resolutionNotes": "Investigation completed"
}
```

```http
POST /api/alerts/{id}/assign
Content-Type: application/json

{
  "assignedTo": "Officer John"
}
```

### Dashboard & Analytics
```http
GET /api/alerts/dashboard/summary
GET /api/alerts/active
GET /api/alerts/unacknowledged
GET /api/alerts/statistics?days=7
```

### NLP Integration
```http
POST /api/alerts/nlp/analyze-text
Content-Type: application/json

{
  "text": "Armed robbery in progress",
  "context": "criminal activity"
}
```

```http
POST /api/alerts/{alertId}/nlp-analyze
```

---

## 🔮 Prediction Service API (Port 8082)

### Risk Forecasts
```http
GET /api/predictions/forecasts?page=0&size=20
GET /api/predictions/forecasts/{id}
GET /api/predictions/forecasts/current
```

```http
POST /api/predictions/generate-forecast
Content-Type: application/json

{
  "forecastType": "risk_trend",
  "parameters": {
    "hours": 24,
    "location": "Nairobi"
  }
}
```

### Hotspot Predictions
```http
GET /api/predictions/hotspots?page=0&size=20
GET /api/predictions/hotspots/{id}
```

### Analytics
```http
GET /api/predictions/risk-trends?days=30
GET /api/predictions/dashboard/summary
GET /api/predictions/location-risk?lat=-1.2864&lon=36.8172
```

### AI Engine Integration
```http
GET /api/ai-engine/health
GET /api/ai-engine/models
GET /api/ai-engine/stats
GET /api/ai-engine/capabilities
GET /api/ai-engine/prediction-analysis
```

---

## 🕵️ Intelligence Service API (Port 8083)

### Intelligence Reports
```http
GET /api/intelligence/reports?page=0&size=20&threatLevel=HIGH
GET /api/intelligence/reports/{id}
```

```http
POST /api/intelligence/reports
Content-Type: application/json

{
  "title": "Intelligence Brief",
  "threatLevel": "HIGH",
  "category": "SECURITY",
  "content": "Gathering intelligence on suspicious activities",
  "source": "Field Intelligence"
}
```

```http
PUT /api/intelligence/reports/{id}
Content-Type: application/json

{
  "title": "Updated Brief",
  "content": "Updated intelligence content"
}
```

```http
POST /api/intelligence/reports/{id}/verify
Content-Type: application/json

{
  "verified": true,
  "verificationNotes": "Confirmed by multiple sources"
}
```

### Analytics & Visualization
```http
GET /api/intelligence/dashboard/summary
GET /api/intelligence/threat-trends?days=30
GET /api/intelligence/threat-map?region=nairobi
```

---

## 📥 Ingestion Service API (Port 8084)

### Data Ingestion
```http
POST /api/ingestion/social-media
Content-Type: application/json

{
  "id": "sm-001",
  "platform": "twitter",
  "content": "Protest forming at central park #nairobi",
  "author": "user123",
  "timestamp": "2026-02-19T15:20:00Z",
  "likes": 15,
  "shares": 8,
  "geoTagLatitude": -1.2864,
  "geoTagLongitude": 36.8172
}
```

```http
POST /api/ingestion/cctv
Content-Type: application/json

{
  "id": "cctv-001",
  "cameraId": "CAM-001",
  "imageUrl": "http://example.com/image.jpg",
  "timestamp": "2026-02-19T15:20:00Z",
  "latitude": -1.2864,
  "longitude": 36.8172,
  "streamUrl": "rtmp://example.com/stream"
}
```

```http
POST /api/ingestion/cyber-feed
Content-Type: application/json

{
  "id": "cyber-001",
  "source": "firewall",
  "sourceType": "cyber",
  "content": "Multiple failed login attempts detected",
  "contentType": "log",
  "timestamp": "2026-02-19T15:20:00Z"
}
```

### Batch Processing
```http
POST /api/ingestion/start-batch
```

```http
GET /api/ingestion/status
```

---

## 🌐 API Gateway Routes (Port 8080)

The API Gateway routes all requests to appropriate services:

### Service Routes
- `/api/auth/**` → Auth Service (8085)
- `/api/alerts/**` → Alert Service (8081)
- `/api/predictions/**` → Prediction Service (8082)
- `/api/intelligence/**` → Intelligence Service (8083)
- `/api/ingestion/**` → Ingestion Service (8084)
- `/api/ai-engine/**` → AI Engine (8000)

### Direct AI Engine Routes
- `/analyze` → AI Engine (8000)
- `/predict/**` → AI Engine (8000)
- `/nlp/**` → AI Engine (8000)

### Example Gateway Usage
```http
# Through Gateway
POST http://localhost:8080/api/alerts
POST http://localhost:8080/api/auth/login
GET http://localhost:8080/api/predictions/forecasts

# Direct AI Engine
POST http://localhost:8080/analyze
POST http://localhost:8080/nlp/analyze-text
```

---

## 🔒 Authentication

### Token-Based Authentication
Most endpoints require authentication using Bearer tokens:

```http
Authorization: Bearer {uuid-token}
```

### Login Flow
1. POST `/api/auth/login` with credentials
2. Receive UUID token in response
3. Include token in `Authorization` header for protected endpoints

### Default Credentials
- **Username**: `admin`
- **Password**: `admin123`

---

## 📊 Response Formats

### Success Response
```json
{
  "status": "success",
  "data": {...},
  "timestamp": "2026-02-19T15:20:00Z"
}
```

### Error Response
```json
{
  "status": "error",
  "message": "Error description",
  "timestamp": "2026-02-19T15:20:00Z"
}
```

### Paginated Response
```json
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 5,
  "size": 20,
  "number": 0
}
```

---

## 🧪 Testing

### Comprehensive Test Script
Run the comprehensive test script to validate all endpoints:

```bash
cd ai-engine
python simple_test.py
```

This will test:
- All 65+ endpoints across 7 services
- Gateway routing functionality
- Authentication flows
- Data ingestion and processing
- AI/ML analysis capabilities

### Manual Testing Examples
```bash
# Test AI Engine
curl -X GET http://localhost:8000/health

# Test Auth Service
curl -X POST http://localhost:8085/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Test Alert Service through Gateway
curl -X GET http://localhost:8080/api/alerts

# Test NLP Analysis
curl -X POST http://localhost:8000/nlp/analyze-text \
  -H "Content-Type: application/json" \
  -d '{"text":"Suspicious activity detected","context":"security"}'
```

---

## 🚀 Quick Start

1. **Start All Services**
   ```bash
   # AI Engine (Python)
   cd ai-engine
   uvicorn model_integration:app --host 0.0.0.0 --port 8000
   
   # Java Services (in separate terminals)
   cd backend/api-gateway && ./gradlew bootRun
   cd backend/auth-service && ./gradlew bootRun
   cd backend/alert-service && ./gradlew bootRun
   cd backend/prediction-service && ./gradlew bootRun
   cd backend/intelligence-service && ./gradlew bootRun
   cd backend/ingestion-service && ./gradlew bootRun
   ```

2. **Test Services**
   ```bash
   cd ai-engine
   python simple_test.py
   ```

3. **Access Services**
   - API Gateway: http://localhost:8080
   - AI Engine: http://localhost:8000/docs (FastAPI docs)
   - All services are ready for frontend integration

---

## 📝 Notes

- All services support CORS for frontend integration
- Authentication uses UUID-based tokens (simple for MVP)
- API Gateway provides single entry point for all services
- AI Engine provides advanced NLP and ML capabilities
- Comprehensive error handling and logging implemented
- Real-time data ingestion and processing available

For detailed implementation details, see the individual service documentation and source code.

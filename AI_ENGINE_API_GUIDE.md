# NTEWS AI Engine - Complete API Documentation

## 🚀 Available Endpoints

### **Health & Status**
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/health` | GET | Health check and service status |
| `/` | GET | Root endpoint with basic info |
| `/models` | GET | List available AI models |
| `/stats` | GET | Get AI engine statistics |
| `/capabilities` | GET | Get AI engine capabilities |

### **Core Prediction Endpoints**
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/analyze` | POST | Analyze threat data |
| `/predict` | POST | Predict risk trends |
| `/predict/hotspots` | POST | Predict threat hotspots |
| `/prediction-analysis` | POST | Get detailed prediction analysis |

### **MVP Guide Endpoints** (NEWLY ADDED)
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/predict/text` | POST | Text-based threat prediction |
| `/predict/geospatial` | POST | Geospatial threat prediction |
| `/predict/forecast/trend` | POST | Trend forecasting |

---

## 🧪 TESTING COMMANDS

### **1. Health Check**
```powershell
curl -X GET http://localhost:8000/health
```

### **2. Text Prediction** ⭐
```powershell
curl -X POST http://localhost:8000/predict/text `
  -H "Content-Type: application/json" `
  -d '{"text": "BREAKING: Bomb threat reported in Nairobi CBD, evacuations underway", "model": "random_forest"}'
```

### **3. Geospatial Prediction** ⭐
```powershell
curl -X POST http://localhost:8000/predict/geospatial `
  -H "Content-Type: application/json" `
  -d '{"latitude": -1.2864, "longitude": 36.8172}'
```

### **4. Trend Forecast** ⭐
```powershell
curl -X POST http://localhost:8000/predict/forecast/trend `
  -H "Content-Type: application/json" `
  -d '{"historical_data": [], "forecast_hours": 24}'
```

### **5. General Threat Analysis**
```powershell
curl -X POST http://localhost:8000/analyze `
  -H "Content-Type: application/json" `
  -d '{
    "id": "threat_001",
    "type": "terror_threat",
    "source": "social_media",
    "sourceType": "twitter",
    "content": "Suspicious activity reported downtown",
    "timestamp": "2024-02-18T10:00:00Z",
    "location": "Nairobi CBD",
    "severity": "high"
  }'
```

### **6. Risk Prediction**
```powershell
curl -X POST http://localhost:8000/predict `
  -H "Content-Type: application/json" `
  -d '{
    "data": [{"time": "2024-02-18", "risk": 0.5}],
    "hours": 24,
    "type": "trend"
  }'
```

### **7. Hotspot Prediction**
```powershell
curl -X POST http://localhost:8000/predict/hotspots `
  -H "Content-Type: application/json" `
  -d '{
    "data": [],
    "hours": 24,
    "type": "hotspot"
  }'
```

### **8. Model Information**
```powershell
curl -X GET http://localhost:8000/models
```

### **9. Engine Statistics**
```powershell
curl -X GET http://localhost:8000/stats
```

### **10. Capabilities Overview**
```powershell
curl -X GET http://localhost:8000/capabilities
```

---

## 📊 RESPONSE EXAMPLES

### **Text Prediction Response**
```json
{
  "prediction_id": "pred_1234",
  "threat_level": "high",
  "confidence": 0.85,
  "model_used": "random_forest",
  "analysis": {
    "threat_keywords_found": ["bomb", "threat"],
    "sentiment": "negative",
    "urgency": "immediate"
  }
}
```

### **Geospatial Prediction Response**
```json
{
  "location": {"lat": -1.2864, "lng": 36.8172},
  "risk_level": "medium",
  "confidence": 0.78,
  "risk_factors": ["population_density", "critical_infrastructure"],
  "nearby_threats": 3,
  "recommendations": ["increase_surveillance", "maintain_monitoring"]
}
```

### **Trend Forecast Response**
```json
{
  "forecast_id": "forecast_5678",
  "forecast_hours": 24,
  "generated_at": "2024-02-18T13:00:00",
  "trend_analysis": {
    "overall_trend": "increasing",
    "peak_risk_time": "2024-02-18 18:00",
    "average_risk": 0.45
  },
  "forecast_data": [
    {
      "time": "2024-02-18 14:00",
      "risk_level": 0.4,
      "threat_count": 4,
      "confidence": 0.84
    }
  ]
}
```

---

## 🔧 TROUBLESHOOTING

### **405 Method Not Allowed**
- **Cause**: Using GET request for POST endpoints
- **Solution**: Always use `-X POST` for prediction endpoints

### **404 Not Found**
- **Cause**: Endpoint doesn't exist
- **Solution**: Check endpoint spelling and use `/` at the end

### **Connection Refused**
- **Cause**: AI Engine not running
- **Solution**: Start AI Engine first:
  ```bash
  cd ai-engine
  python -m uvicorn model_integration:app --host 0.0.0.0 --port 8000
  ```

---

## 🎯 QUICK TEST SEQUENCE

1. **Start AI Engine**
2. **Test Health**: `curl -X GET http://localhost:8000/health`
3. **Test Text**: `curl -X POST http://localhost:8000/predict/text ...`
4. **Test Geo**: `curl -X POST http://localhost:8000/predict/geospatial ...`
5. **Test Forecast**: `curl -X POST http://localhost:8000/predict/forecast/trend ...`

All endpoints should respond with JSON data and HTTP 200 status!

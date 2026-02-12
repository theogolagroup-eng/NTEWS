#!/bin/bash

echo "📊 Loading demo data into NTEWS MVP..."

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 10

# Load demo intelligence reports
echo "📝 Loading intelligence reports..."
curl -X POST http://localhost:8082/api/intelligence/reports \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Suspicious Activity Detected - Nairobi CBD",
    "summary": "Multiple reports of suspicious gatherings in central business district",
    "description": "Social media monitoring indicates unusual crowd formation near government buildings",
    "threatLevel": "high",
    "category": "social_unrest",
    "threatScore": 0.75,
    "confidence": 0.82,
    "location": {
      "latitude": "-1.2921",
      "longitude": "36.8219",
      "address": "Nairobi CBD, Kenya",
      "city": "Nairobi",
      "region": "Nairobi County",
      "country": "Kenya"
    },
    "recommendations": ["Increase police presence", "Monitor social media", "Prepare contingency plans"]
  }'

curl -X POST http://localhost:8082/api/intelligence/reports \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Cyber Threat Alert - Banking Sector",
    "summary": "Increased phishing attempts targeting Kenyan banks",
    "description": "Intelligence indicates coordinated phishing campaign against major financial institutions",
    "threatLevel": "medium",
    "category": "cyber",
    "threatScore": 0.65,
    "confidence": 0.78,
    "recommendations": ["Issue security alerts", "Update firewall rules", "Educate customers"]
  }'

curl -X POST http://localhost:8082/api/intelligence/reports \
  -H "Content-Type: application/json" \
  -d '{
    "title": "CCTV Anomaly Detection - Mombasa Port",
    "summary": "Unusual vehicle activity detected at port entrance",
    "description": "AI-powered CCTV analysis identified suspicious vehicle patterns during off-hours",
    "threatLevel": "medium",
    "category": "criminal",
    "threatScore": 0.58,
    "confidence": 0.71,
    "location": {
      "latitude": "-4.0547",
      "longitude": "39.6636",
      "address": "Mombasa Port, Kenya",
      "city": "Mombasa",
      "region": "Mombasa County",
      "country": "Kenya"
    },
    "recommendations": ["Increase security patrols", "Review access logs", "Verify vehicle registrations"]
  }'

# Generate demo forecast
echo "🔮 Generating risk forecast..."
curl -X POST http://localhost:8083/api/predictions/generate-forecast \
  -H "Content-Type: application/json" \
  -d '{
    "forecastType": "trend",
    "parameters": {
      "lookback_days": 7,
      "forecast_hours": 24
    }
  }'

curl -X POST http://localhost:8083/api/predictions/generate-forecast \
  -H "Content-Type: application/json" \
  -d '{
    "forecastType": "hotspot",
    "parameters": {
      "lookback_days": 3,
      "forecast_hours": 48
    }
  }'

echo ""
echo "✅ Demo data loaded successfully!"
echo ""
echo "📊 View the data at:"
echo "   Dashboard: http://localhost:3000"
echo "   Intelligence Reports: http://localhost:8082/api/intelligence/reports"
echo "   Risk Forecasts: http://localhost:8083/api/predictions/forecasts"
echo "   Active Alerts: http://localhost:8084/api/alerts"

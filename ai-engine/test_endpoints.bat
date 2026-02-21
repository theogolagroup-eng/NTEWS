@echo off
echo ========================================
echo NTEWS AI Engine - Quick Test Script
echo ========================================
echo.

echo 1. Testing Health Check...
curl -X GET http://localhost:8000/health
echo.
echo.

echo 2. Testing Text Prediction...
curl -X POST http://localhost:8000/predict/text ^
  -H "Content-Type: application/json" ^
  -d "{\"text\": \"BREAKING: Bomb threat reported in Nairobi CBD, evacuations underway\", \"model\": \"random_forest\"}"
echo.
echo.

echo 3. Testing Geospatial Prediction...
curl -X POST http://localhost:8000/predict/geospatial ^
  -H "Content-Type: application/json" ^
  -d "{\"latitude\": -1.2864, \"longitude\": 36.8172}"
echo.
echo.

echo 4. Testing Trend Forecast...
curl -X POST http://localhost:8000/predict/forecast/trend ^
  -H "Content-Type: application/json" ^
  -d "{\"historical_data\": [], \"forecast_hours\": 24}"
echo.
echo.

echo 5. Testing General Analysis...
curl -X POST http://localhost:8000/analyze ^
  -H "Content-Type: application/json" ^
  -d "{\"id\": \"threat_001\", \"type\": \"terror_threat\", \"source\": \"social_media\", \"content\": \"Suspicious activity reported downtown\", \"timestamp\": \"2024-02-18T10:00:00Z\", \"severity\": \"high\"}"
echo.
echo.

echo 6. Testing Models Info...
curl -X GET http://localhost:8000/models
echo.
echo.

echo 7. Testing Engine Stats...
curl -X GET http://localhost:8000/stats
echo.
echo.

echo ========================================
echo AI Engine Testing Complete!
echo ========================================
pause

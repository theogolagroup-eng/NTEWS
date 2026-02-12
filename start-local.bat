@echo off
echo Starting NTEWS MVP Services Locally
echo ===================================

:: Start API Gateway
echo Starting API Gateway (port 8080)...
start "API Gateway" cmd /k "cd /d %~dp0backend\api-gateway && gradlew.bat bootRun"

:: Wait a bit for gateway to start
timeout /t 10 /nobreak >nul

:: Start Auth Service
echo Starting Auth Service (port 8081)...
start "Auth Service" cmd /k "cd /d %~dp0backend\auth-service && gradlew.bat bootRun"

:: Start Intelligence Service
echo Starting Intelligence Service (port 8082)...
start "Intelligence Service" cmd /k "cd /d %~dp0backend\intelligence-service && gradlew.bat bootRun"

:: Start Prediction Service
echo Starting Prediction Service (port 8083)...
start "Prediction Service" cmd /k "cd /d %~dp0backend\prediction-service && gradlew.bat bootRun"

:: Start Alert Service
echo Starting Alert Service (port 8084)...
start "Alert Service" cmd /k "cd /d %~dp0backend\alert-service && gradlew.bat bootRun"

:: Start Ingestion Service
echo Starting Ingestion Service (port 8085)...
start "Ingestion Service" cmd /k "cd /d %~dp0backend\ingestion-service && gradlew.bat bootRun"

:: Wait for services to start
timeout /t 15 /nobreak >nul

:: Start AI Engine
echo Starting AI Engine (port 8000)...
start "AI Engine" cmd /k "cd /d %~dp0ai-engine && .venv\Scripts\Activate.ps1 && uvicorn model_integration:app --host 0.0.0.0 --port 8000"

:: Wait for AI Engine
timeout /t 10 /nobreak >nul

:: Start Frontend
echo Starting Frontend (port 3000)...
start "Frontend" cmd /k "cd /d %~dp0frontend-dashboard && npm run dev"

echo.
echo All services starting up...
echo.
echo Services:
echo - API Gateway: http://localhost:8080
echo - Auth Service: http://localhost:8081
echo - Intelligence Service: http://localhost:8082
echo - Prediction Service: http://localhost:8083
echo - Alert Service: http://localhost:8084
echo - Ingestion Service: http://localhost:8085
echo - AI Engine: http://localhost:8000
echo - Frontend: http://localhost:3000
echo.
echo Press any key to continue...
pause >nul

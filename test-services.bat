@echo off
echo Testing NTEWS Services Startup
echo ===============================

echo.
echo 1. Testing API Gateway (port 8080)...
cd backend\api-gateway
start /min cmd /c "gradlew.bat bootRun > gateway.log 2>&1"
timeout /t 15 /nobreak >nul
curl -s http://localhost:8080/actuator/health >nul 2>&1
if %errorlevel% equ 0 echo [✓] API Gateway is running
if %errorlevel% neq 0 echo [✗] API Gateway failed to start

echo.
echo 2. Testing Auth Service (port 8081)...
cd ..\auth-service
start /min cmd /c "gradlew.bat bootRun > auth.log 2>&1"
timeout /t 15 /nobreak >nul
curl -s http://localhost:8081/actuator/health >nul 2>&1
if %errorlevel% equ 0 echo [✓] Auth Service is running
if %errorlevel% neq 0 echo [✗] Auth Service failed to start

echo.
echo 3. Testing Intelligence Service (port 8082)...
cd ..\intelligence-service
start /min cmd /c "gradlew.bat bootRun > intelligence.log 2>&1"
timeout /t 15 /nobreak >nul
curl -s http://localhost:8082/actuator/health >nul 2>&1
if %errorlevel% equ 0 echo [✓] Intelligence Service is running
if %errorlevel% neq 0 echo [✗] Intelligence Service failed to start

echo.
echo 4. Testing Prediction Service (port 8083)...
cd ..\prediction-service
start /min cmd /c "gradlew.bat bootRun > prediction.log 2>&1"
timeout /t 15 /nobreak >nul
curl -s http://localhost:8083/actuator/health >nul 2>&1
if %errorlevel% equ 0 echo [✓] Prediction Service is running
if %errorlevel% neq 0 echo [✗] Prediction Service failed to start

echo.
echo 5. Testing Alert Service (port 8084)...
cd ..\alert-service
start /min cmd /c "gradlew.bat bootRun > alert.log 2>&1"
timeout /t 15 /nobreak >nul
curl -s http://localhost:8084/actuator/health >nul 2>&1
if %errorlevel% equ 0 echo [✓] Alert Service is running
if %errorlevel% neq 0 echo [✗] Alert Service failed to start

echo.
echo 6. Testing Ingestion Service (port 8085)...
cd ..\ingestion-service
start /min cmd /c "gradlew.bat bootRun > ingestion.log 2>&1"
timeout /t 15 /nobreak >nul
curl -s http://localhost:8085/actuator/health >nul 2>&1
if %errorlevel% equ 0 echo [✓] Ingestion Service is running
if %errorlevel% neq 0 echo [✗] Ingestion Service failed to start

echo.
echo 7. Testing AI Engine (port 8000)...
cd ..\..\ai-engine
start /min cmd /c ".venv\Scripts\Activate.ps1 && uvicorn model_integration:app --host 0.0.0.0 --port 8000 > ai-engine.log 2>&1"
timeout /t 10 /nobreak >nul
curl -s http://localhost:8000/health >nul 2>&1
if %errorlevel% equ 0 echo [✓] AI Engine is running
if %errorlevel% neq 0 echo [✗] AI Engine failed to start

echo.
echo 8. Testing Frontend (port 3000)...
cd ..\frontend-dashboard
start /min cmd /c "npm run dev > frontend.log 2>&1"
timeout /t 10 /nobreak >nul
curl -s http://localhost:3000 >nul 2>&1
if %errorlevel% equ 0 echo [✓] Frontend is running
if %errorlevel% neq 0 echo [✗] Frontend failed to start

echo.
echo Test complete! Check log files for errors:
echo - backend\api-gateway\gateway.log
echo - backend\auth-service\auth.log
echo - backend\intelligence-service\intelligence.log
echo - backend\prediction-service\prediction.log
echo - backend\alert-service\alert.log
echo - backend\ingestion-service\ingestion.log
echo - ai-engine\ai-engine.log
echo - frontend-dashboard\frontend.log
echo.
pause

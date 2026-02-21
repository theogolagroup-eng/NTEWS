# NTEWS AI Engine - PowerShell Test Script
Write-Host "========================================" -ForegroundColor Green
Write-Host "NTEWS AI Engine - Quick Test Script" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

Write-Host "1. Testing Health Check..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8000/health" -Method Get
    $response | ConvertTo-Json -Depth 10
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

Write-Host "2. Testing Text Prediction..." -ForegroundColor Yellow
try {
    $body = @{
        text = "BREAKING: Bomb threat reported in Nairobi CBD, evacuations underway"
        model = "random_forest"
    } | ConvertTo-Json -Depth 10
    
    $response = Invoke-RestMethod -Uri "http://localhost:8000/predict/text" -Method Post -ContentType "application/json" -Body $body
    $response | ConvertTo-Json -Depth 10
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

Write-Host "3. Testing Geospatial Prediction..." -ForegroundColor Yellow
try {
    $body = @{
        latitude = -1.2864
        longitude = 36.8172
    } | ConvertTo-Json -Depth 10
    
    $response = Invoke-RestMethod -Uri "http://localhost:8000/predict/geospatial" -Method Post -ContentType "application/json" -Body $body
    $response | ConvertTo-Json -Depth 10
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

Write-Host "4. Testing Trend Forecast..." -ForegroundColor Yellow
try {
    $body = @{
        historical_data = @()
        forecast_hours = 24
    } | ConvertTo-Json -Depth 10
    
    $response = Invoke-RestMethod -Uri "http://localhost:8000/predict/forecast/trend" -Method Post -ContentType "application/json" -Body $body
    $response | ConvertTo-Json -Depth 10
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

Write-Host "5. Testing General Analysis..." -ForegroundColor Yellow
try {
    $body = @{
        id = "threat_001"
        type = "terror_threat"
        source = "social_media"
        sourceType = "twitter"
        content = "Suspicious activity reported downtown"
        timestamp = "2024-02-18T10:00:00Z"
        severity = "high"
    } | ConvertTo-Json -Depth 10
    
    $response = Invoke-RestMethod -Uri "http://localhost:8000/analyze" -Method Post -ContentType "application/json" -Body $body
    $response | ConvertTo-Json -Depth 10
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

Write-Host "6. Testing Models Info..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8000/models" -Method Get
    $response | ConvertTo-Json -Depth 10
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

Write-Host "7. Testing Engine Stats..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8000/stats" -Method Get
    $response | ConvertTo-Json -Depth 10
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Green
Write-Host "AI Engine Testing Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

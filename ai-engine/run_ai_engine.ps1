# NTEWS AI Engine Startup Script
Write-Host "Starting NTEWS AI Engine..." -ForegroundColor Green

# Deactivate virtual environment if active
if ($env:VIRTUAL_ENV) {
    Write-Host "Deactivating virtual environment..." -ForegroundColor Yellow
    deactivate
}

# Use system Python to start AI Engine
Write-Host "Using system Python to start AI Engine..." -ForegroundColor Yellow
python -m uvicorn model_integration:app --host 0.0.0.0 --port 8000

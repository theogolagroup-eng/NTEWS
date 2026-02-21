@echo off
echo Stopping NTEWS MVP Services
echo ============================

:: Kill all Java processes (backend services)
echo Stopping backend services...
taskkill /F /IM java.exe 2>nul

:: Kill Python process (AI Engine)
echo Stopping AI Engine...
taskkill /F /IM python.exe 2>nul

:: Kill Node processes (frontend)
echo Stopping Frontend...
taskkill /F /IM node.exe 2>nul

:: Wait for processes to terminate
timeout /t 3 /nobreak >nul

echo All services stopped.
echo.
pause

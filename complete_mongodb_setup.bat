@echo off
echo 🚀 MongoDB Shell Setup for NTEWS
echo ================================

echo.
echo 📋 Step 1: Find mongosh installation...

REM Try to find mongosh in PATH
for %%i in (mongosh.exe) do set MONGOSH_PATH=%%~$PATH:i

REM If not found in PATH, search common locations
if "%MONGOSH_PATH%"=="" (
    echo 🔍 Searching in common installation paths...
    
    REM Check Program Files
    if exist "C:\Program Files\mongosh\mongosh.exe" (
        set MONGOSH_PATH=C:\Program Files\mongosh\mongosh.exe
        echo ✅ Found at: %MONGOSH_PATH%
    ) else if exist "C:\Program Files\MongoDB\Server\*\bin\mongosh.exe" (
        for /d %%i in ("C:\Program Files\MongoDB\Server\*") do (
            if exist "%%i\bin\mongosh.exe" (
                set MONGOSH_PATH=%%i\bin\mongosh.exe
                echo ✅ Found at: %MONGOSH_PATH%
            )
        )
    ) else if exist "C:\Users\%USERNAME%\AppData\Local\Programs\mongosh\mongosh.exe" (
        set MONGOSH_PATH=C:\Users\%USERNAME%\AppData\Local\Programs\mongosh\mongosh.exe
        echo ✅ Found at: %MONGOSH_PATH%
    ) else (
        echo ❌ mongosh not found. Please check installation.
        echo 💡 Try running: where mongosh
        pause
        exit /b 1
    )
) else (
    echo ✅ Found in PATH: %MONGOSH_PATH%
)

echo.
echo 📋 Step 2: Test MongoDB connection...
"%MONGOSH_PATH%" --eval "db.runCommand('ping')" --quiet
if %errorlevel% neq 0 (
    echo ❌ Cannot connect to MongoDB
    echo 💡 Make sure MongoDB service is running
    pause
    exit /b 1
) else (
    echo ✅ MongoDB connection successful
)

echo.
echo 📋 Step 3: Initialize NTEWS database...
"%MONGOSH_PATH%" ntews_alerts init_database.js
if %errorlevel% neq 0 (
    echo ❌ Database initialization failed
    echo 💡 Check if init_database.js exists in current directory
    pause
    exit /b 1
) else (
    echo ✅ Database initialized successfully
)

echo.
echo 🎉 MongoDB setup complete!
echo 📊 NTEWS Alert Service database is ready
echo.
echo 🚀 Next steps:
echo    1. Restart alert service: cd backend\alert-service && ./gradlew.bat bootRun
echo    2. Test endpoint: http://localhost:8084/api/alerts/dashboard/summary
echo    3. Check frontend: http://localhost:3000
echo.
pause

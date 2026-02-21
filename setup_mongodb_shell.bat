@echo off
echo 🔍 Finding MongoDB Shell Installation
echo ==================================

echo.
echo 📋 Step 1: Searching for mongosh.exe...

REM Check common installation paths
set FOUND=0

REM Check Program Files
if exist "C:\Program Files\MongoDB\Server\*\bin\mongosh.exe" (
    for /d %%i in ("C:\Program Files\MongoDB\Server\*") do (
        if exist "%%i\bin\mongosh.exe" (
            echo ✅ Found at: %%i\bin\mongosh.exe
            set MONGOSH_PATH=%%i\bin\mongosh.exe
            set FOUND=1
        )
    )
)

REM Check AppData
if %FOUND%==0 (
    if exist "C:\Users\%USERNAME%\AppData\Local\Programs\mongosh\mongosh.exe" (
        echo ✅ Found at: C:\Users\%USERNAME%\AppData\Local\Programs\mongosh\mongosh.exe
        set MONGOSH_PATH=C:\Users\%USERNAME%\AppData\Local\Programs\mongosh\mongosh.exe
        set FOUND=1
    )
)

REM Check chocolatey
if %FOUND%==0 (
    if exist "C:\ProgramData\chocolatey\bin\mongosh.exe" (
        echo ✅ Found at: C:\ProgramData\chocolatey\bin\mongosh.exe
        set MONGOSH_PATH=C:\ProgramData\chocolatey\bin\mongosh.exe
        set FOUND=1
    )
)

if %FOUND%==0 (
    echo ❌ mongosh.exe not found in common locations
    echo.
    echo 💡 Please check if MongoDB Shell was installed correctly
    echo    Download from: https://www.mongodb.com/try/download/shell
    pause
    exit /b 1
)

echo.
echo 📋 Step 2: Testing MongoDB connection...
"%MONGOSH_PATH%" --eval "db.runCommand('ping')" --quiet
if %errorlevel% neq 0 (
    echo ❌ Cannot connect to MongoDB
    pause
    exit /b 1
) else (
    echo ✅ MongoDB connection successful
)

echo.
echo 📋 Step 3: Initializing NTEWS database...
"%MONGOSH_PATH%" ntews_alerts init_database.js
if %errorlevel% neq 0 (
    echo ❌ Database initialization failed
    pause
    exit /b 1
) else (
    echo ✅ Database initialized successfully
)

echo.
echo 🎉 MongoDB setup complete!
echo 📊 Alert service should now work properly
echo.
echo 🚀 Next steps:
echo    1. Restart alert service: cd backend\alert-service && ./gradlew.bat bootRun
echo    2. Test endpoint: http://localhost:8084/api/alerts/dashboard/summary
echo    3. Check frontend: http://localhost:3000
echo.
pause

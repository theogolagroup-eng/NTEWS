@echo off
echo 🚀 Testing MongoDB Connection for NTEWS Alert Service
echo ========================================================

echo.
echo 📋 Step 1: Check if MongoDB is running...
netstat -an | findstr :27017
if %errorlevel% neq 0 (
    echo ❌ MongoDB is not running on port 27017
    echo 💡 Please start MongoDB service first
    echo    - Windows: Start MongoDB service from Services
    echo    - Or run: mongod --dbpath "C:\data\db"
    pause
    exit /b 1
) else (
    echo ✅ MongoDB is running on port 27017
)

echo.
echo 📋 Step 2: Test MongoDB connection...
mongo --eval "db.runCommand('ping')" ntews_alerts
if %errorlevel% neq 0 (
    echo ❌ Cannot connect to MongoDB
    pause
    exit /b 1
) else (
    echo ✅ MongoDB connection successful
)

echo.
echo 📋 Step 3: Initialize database with sample data...
mongo ntews_alerts init_mongodb.js
if %errorlevel% neq 0 (
    echo ❌ Database initialization failed
    pause
    exit /b 1
) else (
    echo ✅ Database initialized with sample data
)

echo.
echo 📋 Step 4: Verify data insertion...
mongo --eval "db.alerts.countDocuments()" ntews_alerts

echo.
echo 🎉 MongoDB setup complete!
echo 📊 Alert service should now work properly
echo.
echo 🚀 Next steps:
echo    1. Restart alert service: ./gradlew.bat bootRun
echo    2. Test endpoint: http://localhost:8084/api/alerts/dashboard/summary
echo    3. Check WebSocket: ws://localhost:8084/ws/alerts
echo.
pause

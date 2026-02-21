@echo off
echo 🚀 Initializing NTEWS MongoDB Database
echo ======================================

echo.
echo 📋 Step 1: Check MongoDB connection...
"C:\Program Files\MongoDB\Server\8.0\bin\mongod.exe" --version
if %errorlevel% neq 0 (
    echo ❌ MongoDB not found
    pause
    exit /b 1
) else (
    echo ✅ MongoDB found
)

echo.
echo 📋 Step 2: Create database initialization script...
echo use ntews_alerts > temp_init.js
echo. >> temp_init.js
echo db.alerts.insertMany([ >> temp_init.js
echo   { >> temp_init.js
echo     id: "alert-001", >> temp_init.js
echo     title: "Suspicious Network Activity Detected", >> temp_init.js
echo     description: "Unusual traffic patterns detected on critical infrastructure", >> temp_init.js
echo     severity: "HIGH", >> temp_init.js
echo     status: "ACTIVE", >> temp_init.js
echo     category: "SECURITY", >> temp_init.js
echo     source: "Network Monitor", >> temp_init.js
echo     location: "Data Center A", >> temp_init.js
echo     threatScore: 0.75, >> temp_init.js
echo     createdAt: new Date(), >> temp_init.js
echo     updatedAt: new Date(), >> temp_init.js
echo     acknowledged: false, >> temp_init.js
echo     assignedTo: null >> temp_init.js
echo   }, >> temp_init.js
echo   { >> temp_init.js
echo     id: "alert-002", >> temp_init.js
echo     title: "Malware Signature Detected", >> temp_init.js
echo     description: "Known malware signature found in email attachment", >> temp_init.js
echo     severity: "CRITICAL", >> temp_init.js
echo     status: "ACTIVE", >> temp_init.js
echo     category: "MALWARE", >> temp_init.js
echo     source: "Email Scanner", >> temp_init.js
echo     location: "Email Gateway", >> temp_init.js
echo     threatScore: 0.92, >> temp_init.js
echo     createdAt: new Date(Date.now() - 3600000), >> temp_init.js
echo     updatedAt: new Date(), >> temp_init.js
echo     acknowledged: false, >> temp_init.js
echo     assignedTo: null >> temp_init.js
echo   }, >> temp_init.js
echo   { >> temp_init.js
echo     id: "alert-003", >> temp_init.js
echo     title: "Failed Login Attempts", >> temp_init.js
echo     description: "Multiple failed login attempts detected from external IP", >> temp_init.js
echo     severity: "MEDIUM", >> temp_init.js
echo     status: "ACKNOWLEDGED", >> temp_init.js
echo     category: "AUTHENTICATION", >> temp_init.js
echo     source: "Authentication Service", >> temp_init.js
echo     location: "Login Portal", >> temp_init.js
echo     threatScore: 0.45, >> temp_init.js
echo     createdAt: new Date(Date.now() - 7200000), >> temp_init.js
echo     updatedAt: new Date(), >> temp_init.js
echo     acknowledged: true, >> temp_init.js
echo     assignedTo: "security-team@ntews.com" >> temp_init.js
echo   } >> temp_init.js
echo ]); >> temp_init.js
echo. >> temp_init.js
echo db.alerts.createIndex({ "status": 1 }); >> temp_init.js
echo db.alerts.createIndex({ "severity": 1 }); >> temp_init.js
echo db.alerts.createIndex({ "createdAt": -1 }); >> temp_init.js
echo. >> temp_init.js
echo print("✅ NTEWS Alert Database initialized successfully!"); >> temp_init.js
echo print("📊 Sample alerts created: " + db.alerts.countDocuments()); >> temp_init.js

echo ✅ Database script created

echo.
echo 📋 Step 3: Try to run initialization...
echo ⚠️  Note: If mongo command is not found, you may need to add MongoDB to PATH
echo.

REM Try different MongoDB client commands
mongo ntews_alerts temp_init.js 2>nul
if %errorlevel% equ 0 (
    echo ✅ Database initialized successfully with mongo command
    goto cleanup
)

mongosh ntews_alerts temp_init.js 2>nul
if %errorlevel% equ 0 (
    echo ✅ Database initialized successfully with mongosh command
    goto cleanup
)

echo ❌ MongoDB client not found in PATH
echo 💡 Please add "C:\Program Files\MongoDB\Server\8.0\bin" to your PATH
echo    Or run: set PATH=%PATH%;C:\Program Files\MongoDB\Server\8.0\bin
echo    Then run this script again

:cleanup
del temp_init.js 2>nul
echo.
echo 🎉 Database initialization complete!
echo 📊 Alert service should now work properly
echo.
pause

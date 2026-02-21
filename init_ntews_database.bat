@echo off
echo 🔍 Finding mongosh and initializing database...
echo ===========================================

REM Try to find mongosh in common locations
set MONGOSH_CMD=

REM Check Program Files
if exist "C:\Program Files\mongosh\mongosh.exe" (
    set MONGOSH_CMD="C:\Program Files\mongosh\mongosh.exe"
    echo ✅ Found: C:\Program Files\mongosh\mongosh.exe
)

REM Check MongoDB Server directories
if "%MONGOSH_CMD%"=="" (
    for /d %%i in ("C:\Program Files\MongoDB\Server\*") do (
        if exist "%%i\bin\mongosh.exe" (
            set MONGOSH_CMD="%%i\bin\mongosh.exe"
            echo ✅ Found: %%i\bin\mongosh.exe
        )
    )
)

REM Check AppData
if "%MONGOSH_CMD%"=="" (
    if exist "C:\Users\%USERNAME%\AppData\Local\Programs\mongosh\mongosh.exe" (
        set MONGOSH_CMD="C:\Users\%USERNAME%\AppData\Local\Programs\mongosh\mongosh.exe"
        echo ✅ Found: C:\Users\%USERNAME%\AppData\Local\Programs\mongosh\mongosh.exe
    )
)

if "%MONGOSH_CMD%"=="" (
    echo ❌ mongosh not found in common locations
    echo 💡 Please run this command to find it:
    echo    Get-ChildItem -Path "C:\" -Recurse -Filter "mongosh.exe" -ErrorAction SilentlyContinue | Select-Object FullName
    pause
    exit /b 1
)

echo.
echo 🚀 Initializing NTEWS database...

%MONGOSH_CMD% ntews_alerts --eval "
db.alerts.insertMany([
  {
    id: 'alert-001',
    title: 'Suspicious Network Activity Detected',
    description: 'Unusual traffic patterns detected on critical infrastructure',
    severity: 'HIGH',
    status: 'ACTIVE',
    category: 'SECURITY',
    source: 'Network Monitor',
    location: 'Data Center A',
    threatScore: 0.75,
    createdAt: new Date(),
    updatedAt: new Date(),
    acknowledged: false,
    assignedTo: null
  },
  {
    id: 'alert-002',
    title: 'Malware Signature Detected',
    description: 'Known malware signature found in email attachment',
    severity: 'CRITICAL',
    status: 'ACTIVE',
    category: 'MALWARE',
    source: 'Email Scanner',
    location: 'Email Gateway',
    threatScore: 0.92,
    createdAt: new Date(Date.now() - 3600000),
    updatedAt: new Date(),
    acknowledged: false,
    assignedTo: null
  },
  {
    id: 'alert-003',
    title: 'Failed Login Attempts',
    description: 'Multiple failed login attempts detected from external IP',
    severity: 'MEDIUM',
    status: 'ACKNOWLEDGED',
    category: 'AUTHENTICATION',
    source: 'Authentication Service',
    location: 'Login Portal',
    threatScore: 0.45,
    createdAt: new Date(Date.now() - 7200000),
    updatedAt: new Date(),
    acknowledged: true,
    assignedTo: 'security-team@ntews.com'
  }
]);

db.alerts.createIndex({ 'status': 1 });
db.alerts.createIndex({ 'severity': 1 });
db.alerts.createIndex({ 'createdAt': -1 });

print('NTEWS Database initialized!');
print('Alerts created: ' + db.alerts.countDocuments());
"

if %errorlevel% equ 0 (
    echo.
    echo ✅ Database initialized successfully!
    echo 📊 Alert service should now work properly
    echo.
    echo 🚀 Next step: Start alert service
    echo    cd backend\alert-service
    echo    ./gradlew.bat bootRun
) else (
    echo ❌ Database initialization failed
)

pause

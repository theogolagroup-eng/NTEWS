# 🚀 Manual MongoDB Setup Instructions

Since mongosh is not being found in the system PATH, here are the manual steps:

## 📋 **STEP 1: Find mongosh Location**

Run this in PowerShell to find where mongosh was installed:
```powershell
Get-ChildItem -Path "C:\" -Recurse -Filter "mongosh.exe" -ErrorAction SilentlyContinue | Select-Object FullName
```

## 📋 **STEP 2: Add to PATH (if needed)**

Once you find the path, add it to your system PATH:
- Press Windows + R, type `sysdm.cpl`
- Go to Advanced → Environment Variables
- Edit PATH → Add the mongosh directory
- Restart PowerShell/Command Prompt

## 📋 **STEP 3: Initialize Database**

Once mongosh is working, run:
```bash
cd C:\Users\opand\OneDrive\Desktop\projects\Ntews-mvp
mongosh ntews_alerts init_database.js
```

## 📋 **STEP 4: Test Alert Service**

```bash
cd backend\alert-service
./gradlew.bat bootRun
```

Then test: http://localhost:8084/api/alerts/dashboard/summary

## 🔧 **ALTERNATIVE: Use Full Path**

If you find mongosh path (e.g., `C:\Program Files\mongosh\mongosh.exe`), you can run:
```bash
"C:\Program Files\mongosh\mongosh.exe" ntews_alerts init_database.js
```

**Can you run the PowerShell command above to find mongosh location?**

# 🚀 Simple MongoDB Initialization

Since mongosh is not in PATH but works interactively, let's use a different approach:

## 📋 **OPTION 1: Use PowerShell to find mongosh**

Run this in PowerShell:
```powershell
Get-ChildItem -Path "C:\" -Recurse -Filter "mongosh.exe" -ErrorAction SilentlyContinue | Select-Object FullName
```

## 📋 **OPTION 2: Create a simple JavaScript file**

Create a file `init_db.js` with this content:

```javascript
// NTEWS Database Initialization
db = db.getSiblingDB('ntews_alerts');

// Clear existing data
db.alerts.drop();

// Insert sample alerts
db.alerts.insertMany([
  {
    id: "alert-001",
    title: "Suspicious Network Activity Detected",
    description: "Unusual traffic patterns detected on critical infrastructure",
    severity: "HIGH",
    status: "ACTIVE",
    category: "SECURITY",
    source: "Network Monitor",
    location: "Data Center A",
    threatScore: 0.75,
    createdAt: new Date(),
    updatedAt: new Date(),
    acknowledged: false,
    assignedTo: null
  },
  {
    id: "alert-002",
    title: "Malware Signature Detected",
    description: "Known malware signature found in email attachment",
    severity: "CRITICAL",
    status: "ACTIVE",
    category: "MALWARE",
    source: "Email Scanner",
    location: "Email Gateway",
    threatScore: 0.92,
    createdAt: new Date(Date.now() - 3600000),
    updatedAt: new Date(),
    acknowledged: false,
    assignedTo: null
  },
  {
    id: "alert-003",
    title: "Failed Login Attempts",
    description: "Multiple failed login attempts detected from external IP",
    severity: "MEDIUM",
    status: "ACKNOWLEDGED",
    category: "AUTHENTICATION",
    source: "Authentication Service",
    location: "Login Portal",
    threatScore: 0.45,
    createdAt: new Date(Date.now() - 7200000),
    updatedAt: new Date(),
    acknowledged: true,
    assignedTo: "security-team@ntews.com"
  }
]);

// Create indexes
db.alerts.createIndex({ "status": 1 });
db.alerts.createIndex({ "severity": 1 });
db.alerts.createIndex({ "createdAt": -1 });

print("✅ NTEWS Database initialized!");
print("📊 Sample alerts created: " + db.alerts.countDocuments());
```

## 📋 **OPTION 3: Run with full path once found**

Once you find the path (e.g., `C:\path\to\mongosh.exe`), run:
```bash
"C:\path\to\mongosh.exe" ntews_alerts init_db.js
```

## 🎯 **QUICKEST SOLUTION:**

Go back to your mongosh interactive session and run the JavaScript code from OPTION 2 directly in the shell!

// MongoDB Initialization Script for NTEWS Alert Service
// Run with: mongosh ntews_alerts init_database.js

// Switch to ntews_alerts database
db = db.getSiblingDB('ntews_alerts');

// Clear existing data (optional)
db.alerts.drop();

// Create alerts collection with sample data
db.alerts.insertMany([
  {
    _id: ObjectId(),
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
    _id: ObjectId(),
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
    _id: ObjectId(),
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
  },
  {
    _id: ObjectId(),
    id: "alert-004",
    title: "Data Access Anomaly",
    description: "Unusual data access patterns detected in database",
    severity: "LOW",
    status: "RESOLVED",
    category: "DATA_ACCESS",
    source: "Database Monitor",
    location: "Database Server",
    threatScore: 0.25,
    createdAt: new Date(Date.now() - 10800000),
    updatedAt: new Date(),
    acknowledged: true,
    assignedTo: "db-admin@ntews.com"
  },
  {
    _id: ObjectId(),
    id: "alert-005",
    title: "Port Scanning Activity",
    description: "External IP scanning multiple network ports",
    severity: "HIGH",
    status: "ACTIVE",
    category: "NETWORK",
    source: "Firewall",
    location: "Network Perimeter",
    threatScore: 0.68,
    createdAt: new Date(Date.now() - 1800000),
    updatedAt: new Date(),
    acknowledged: false,
    assignedTo: null
  }
]);

// Create indexes for better performance
db.alerts.createIndex({ "status": 1 });
db.alerts.createIndex({ "severity": 1 });
db.alerts.createIndex({ "createdAt": -1 });
db.alerts.createIndex({ "category": 1 });
db.alerts.createIndex({ "threatScore": -1 });

// Create a simple view for active alerts
db.createView("active_alerts", "alerts", [
  { $match: { status: "ACTIVE" } }
]);

// Display results
print("✅ NTEWS Alert Database initialized successfully!");
print("📊 Sample alerts created: " + db.alerts.countDocuments());
print("🔢 Active alerts: " + db.alerts.countDocuments({status: "ACTIVE"}));
print("🔢 Critical alerts: " + db.alerts.countDocuments({severity: "CRITICAL"}));
print("🔢 High alerts: " + db.alerts.countDocuments({severity: "HIGH"}));
print("🔢 Medium alerts: " + db.alerts.countDocuments({severity: "MEDIUM"}));
print("🔢 Low alerts: " + db.alerts.countDocuments({severity: "LOW"}));
print("🔍 Indexes created for performance");
print("👁️  Active alerts view created");

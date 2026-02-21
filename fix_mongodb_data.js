// Fix MongoDB data to match Java Alert model
db = db.getSiblingDB('ntews_alerts');

// Clear existing data and insert with correct field names
db.alerts.drop();

db.alerts.insertMany([
  {
    id: "alert-001",
    title: "Suspicious Network Activity Detected",
    description: "Unusual traffic patterns detected on critical infrastructure",
    summary: "Network traffic anomaly detected",
    type: "THREAT",
    severity: "HIGH",
    priority: "HIGH",
    category: "SECURITY",
    sourceId: "net-monitor-001",
    sourceType: "system",
    source: "Network Monitor",
    timestamp: new Date(),
    expiresAt: new Date(Date.now() + 24*60*60*1000),
    location: {
      latitude: 40.7128,
      longitude: -74.0060,
      address: "Data Center A",
      region: "US-East"
    },
    affectedAreas: ["Network Infrastructure", "Critical Systems"],
    content: {
      threatLevel: "HIGH",
      confidence: 0.85,
      indicators: ["unusual_traffic", "critical_infrastructure"]
    },
    tags: ["network", "security", "critical"],
    status: "ACTIVE",
    createdAt: new Date(),
    updatedAt: new Date(),
    createdBy: "system",
    assignedTo: null
  },
  {
    id: "alert-002",
    title: "Malware Signature Detected",
    description: "Known malware signature found in email attachment",
    summary: "Malware detected in email gateway",
    type: "THREAT",
    severity: "CRITICAL",
    priority: "CRITICAL",
    category: "MALWARE",
    sourceId: "email-scanner-001",
    sourceType: "system",
    source: "Email Scanner",
    timestamp: new Date(),
    expiresAt: new Date(Date.now() + 24*60*60*1000),
    location: {
      latitude: 40.7128,
      longitude: -74.0060,
      address: "Email Gateway",
      region: "US-East"
    },
    affectedAreas: ["Email Systems", "Gateway"],
    content: {
      threatLevel: "CRITICAL",
      confidence: 0.95,
      indicators: ["malware_signature", "email_attachment"]
    },
    tags: ["malware", "email", "critical"],
    status: "ACTIVE",
    createdAt: new Date(Date.now() - 3600000),
    updatedAt: new Date(),
    createdBy: "system",
    assignedTo: null
  },
  {
    id: "alert-003",
    title: "Failed Login Attempts",
    description: "Multiple failed login attempts detected from external IP",
    summary: "Brute force attack detected",
    type: "THREAT",
    severity: "MEDIUM",
    priority: "MEDIUM",
    category: "AUTHENTICATION",
    sourceId: "auth-service-001",
    sourceType: "system",
    source: "Authentication Service",
    timestamp: new Date(Date.now() - 7200000),
    expiresAt: new Date(Date.now() + 24*60*60*1000),
    location: {
      latitude: 40.7128,
      longitude: -74.0060,
      address: "Login Portal",
      region: "US-East"
    },
    affectedAreas: ["Authentication Systems"],
    content: {
      threatLevel: "MEDIUM",
      confidence: 0.75,
      indicators: ["failed_attempts", "external_ip"]
    },
    tags: ["authentication", "security"],
    status: "ACKNOWLEDGED",
    createdAt: new Date(Date.now() - 7200000),
    updatedAt: new Date(),
    createdBy: "system",
    assignedTo: "security-team@ntews.com"
  }
]);

// Create indexes
db.alerts.createIndex({ "status": 1 });
db.alerts.createIndex({ "severity": 1 });
db.alerts.createIndex({ "createdAt": -1 });

print("✅ NTEWS Database fixed with correct Alert model!");
print("📊 Sample alerts created: " + db.alerts.countDocuments());
print("🔢 Active alerts: " + db.alerts.countDocuments({status: "ACTIVE"}));
print("🔢 Critical alerts: " + db.alerts.countDocuments({severity: "CRITICAL"}));
print("🔢 High alerts: " + db.alerts.countDocuments({severity: "HIGH"}));

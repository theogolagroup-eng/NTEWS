// MongoDB Initialization Script for NTEWS Alert Service
// Run with: node init_mongodb_node.js

const { MongoClient } = require('mongodb');

async function initializeDatabase() {
  const uri = process.env.MONGODB_URI || 'mongodb://localhost:27017';
  const client = new MongoClient(uri);

  try {
    await client.connect();
    console.log('Connected to MongoDB');

    const db = client.db('ntews_alerts');

    // Create alerts collection with sample data
    const alerts = [
      {
        _id: new require('mongodb').ObjectId(),
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
        _id: new require('mongodb').ObjectId(),
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
        _id: new require('mongodb').ObjectId(),
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
    ];

    // Insert sample data
    const result = await db.collection('alerts').insertMany(alerts);
    console.log(`✅ Inserted ${result.insertedCount} sample alerts`);

    // Create indexes for better performance
    await db.collection('alerts').createIndex({ "status": 1 });
    await db.collection('alerts').createIndex({ "severity": 1 });
    await db.collection('alerts').createIndex({ "createdAt": -1 });
    await db.collection('alerts').createIndex({ "category": 1 });
    await db.collection('alerts').createIndex({ "threatScore": -1 });
    console.log("🔍 Indexes created for performance");

    // Create a simple view for active alerts
    try {
      await db.createCollection("active_alerts", {
        viewOn: "alerts",
        pipeline: [{ $match: { status: "ACTIVE" } }]
      });
      console.log("👁️  Active alerts view created");
    } catch (error) {
      if (error.code === 48) { // Namespace exists
        console.log("👁️  Active alerts view already exists");
      } else {
        throw error;
      }
    }

    console.log("✅ NTEWS Alert Database initialized successfully!");
    console.log(`📊 Sample alerts created: ${alerts.length}`);

  } catch (error) {
    console.error('Error initializing database:', error);
  } finally {
    await client.close();
  }
}

initializeDatabase();

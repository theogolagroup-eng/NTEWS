// NTEWS MongoDB Initialization Script
// Creates collections and indexes for threat intelligence data

// Switch to ntews database
db = db.getSiblingDB('ntews');

// Create collections with schema validation

// Intelligence Reports Collection
db.createCollection('intelligence_reports', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['title', 'threatLevel', 'createdAt'],
            properties: {
                title: { bsonType: 'string' },
                summary: { bsonType: 'string' },
                description: { bsonType: 'string' },
                threatLevel: { enum: ['low', 'medium', 'high', 'critical'] },
                threatScore: { bsonType: 'double', minimum: 0, maximum: 1 },
                confidence: { bsonType: 'double', minimum: 0, maximum: 1 },
                category: { bsonType: 'string' },
                location: {
                    bsonType: 'object',
                    properties: {
                        latitude: { bsonType: 'double' },
                        longitude: { bsonType: 'double' },
                        address: { bsonType: 'string' },
                        city: { bsonType: 'string' },
                        region: { bsonType: 'string' },
                        country: { bsonType: 'string' }
                    }
                },
                sources: { bsonType: 'array' },
                aiAnalysis: { bsonType: 'object' },
                recommendations: { bsonType: 'array' },
                status: { enum: ['draft', 'pending', 'published', 'archived'] },
                verified: { bsonType: 'bool' },
                createdAt: { bsonType: 'date' },
                updatedAt: { bsonType: 'date' }
            }
        }
    }
});

// Alerts Collection
db.createCollection('alerts', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['title', 'severity', 'timestamp'],
            properties: {
                title: { bsonType: 'string' },
                description: { bsonType: 'string' },
                severity: { enum: ['low', 'medium', 'high', 'critical'] },
                priority: { enum: ['low', 'normal', 'high', 'urgent'] },
                alertType: { bsonType: 'string' },
                category: { bsonType: 'string' },
                location: { bsonType: 'object' },
                content: { bsonType: 'object' },
                tags: { bsonType: 'array' },
                status: { enum: ['active', 'acknowledged', 'resolved', 'expired'] },
                timestamp: { bsonType: 'date' },
                expiresAt: { bsonType: 'date' },
                createdAt: { bsonType: 'date' }
            }
        }
    }
});

// Risk Forecasts Collection
db.createCollection('risk_forecasts', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['forecastType', 'generatedAt', 'validFrom', 'validTo'],
            properties: {
                forecastType: { enum: ['trend', 'hotspot', 'immediate'] },
                modelVersion: { bsonType: 'string' },
                generatedAt: { bsonType: 'date' },
                validFrom: { bsonType: 'date' },
                validTo: { bsonType: 'date' },
                overallRiskTrend: { bsonType: 'double' },
                confidenceScore: { bsonType: 'double' },
                forecastPoints: { bsonType: 'array' },
                hotspots: { bsonType: 'array' },
                locationRisks: { bsonType: 'array' },
                modelMetadata: { bsonType: 'object' }
            }
        }
    }
});

// Threat Data Collection (raw ingested data)
db.createCollection('threat_data', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['source', 'sourceType', 'ingestedAt'],
            properties: {
                source: { bsonType: 'string' },
                sourceType: { enum: ['social_media', 'cctv', 'cyber', 'news', 'report'] },
                contentType: { bsonType: 'string' },
                rawContent: { bsonType: 'string' },
                processedContent: { bsonType: 'string' },
                location: { bsonType: 'object' },
                timestamp: { bsonType: 'date' },
                ingestedAt: { bsonType: 'date' },
                status: { enum: ['pending', 'processed', 'failed'] }
            }
        }
    }
});

// Create indexes for performance

// Intelligence Reports indexes
db.intelligence_reports.createIndex({ 'threatLevel': 1 });
db.intelligence_reports.createIndex({ 'createdAt': -1 });
db.intelligence_reports.createIndex({ 'location': '2dsphere' });
db.intelligence_reports.createIndex({ 'category': 1 });
db.intelligence_reports.createIndex({ 'status': 1 });
db.intelligence_reports.createIndex({ 'threatScore': -1 });
db.intelligence_reports.createIndex({ 'title': 'text', 'summary': 'text', 'description': 'text' });

// Alerts indexes
db.alerts.createIndex({ 'severity': 1 });
db.alerts.createIndex({ 'status': 1 });
db.alerts.createIndex({ 'timestamp': -1 });
db.alerts.createIndex({ 'location': '2dsphere' });
db.alerts.createIndex({ 'expiresAt': 1 }, { expireAfterSeconds: 0 });
db.alerts.createIndex({ 'title': 'text', 'description': 'text' });

// Risk Forecasts indexes
db.risk_forecasts.createIndex({ 'forecastType': 1 });
db.risk_forecasts.createIndex({ 'validFrom': 1 });
db.risk_forecasts.createIndex({ 'validTo': 1 });
db.risk_forecasts.createIndex({ 'generatedAt': -1 });

// Threat Data indexes
db.threat_data.createIndex({ 'sourceType': 1 });
db.threat_data.createIndex({ 'ingestedAt': -1 });
db.threat_data.createIndex({ 'status': 1 });
db.threat_data.createIndex({ 'location': '2dsphere' });

print('NTEWS MongoDB initialization complete');
print('Collections created: intelligence_reports, alerts, risk_forecasts, threat_data');

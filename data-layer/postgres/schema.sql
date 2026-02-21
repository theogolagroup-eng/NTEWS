-- NTEWS PostgreSQL Schema
-- This file contains the database schema for the NTEWS threat intelligence platform

-- Enable necessary extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "postgis";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Users table for authentication and authorization
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    role VARCHAR(20) DEFAULT 'analyst',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- User roles and permissions
CREATE TABLE user_roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    permissions JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User role assignments
CREATE TABLE user_role_assignments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID REFERENCES user_roles(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by UUID REFERENCES users(id),
    UNIQUE(user_id, role_id)
);

-- Threat intelligence reports
CREATE TABLE intelligence_reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    summary TEXT,
    description TEXT,
    threat_level VARCHAR(20) NOT NULL,
    threat_category VARCHAR(50),
    threat_score DECIMAL(3,2),
    confidence DECIMAL(3,2),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    location_lat DECIMAL(10,8),
    location_lng DECIMAL(11,8),
    location_address TEXT,
    location_city VARCHAR(100),
    location_region VARCHAR(100),
    location_country VARCHAR(100),
    status VARCHAR(20) DEFAULT 'draft',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),
    assigned_to UUID REFERENCES users(id),
    verified BOOLEAN DEFAULT false,
    verification_notes TEXT,
    verified_at TIMESTAMP,
    verified_by UUID REFERENCES users(id),
    metadata JSONB
);

-- Source intelligence data
CREATE TABLE threat_sources (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    report_id UUID REFERENCES intelligence_reports(id) ON DELETE CASCADE,
    source VARCHAR(100) NOT NULL,
    source_type VARCHAR(50) NOT NULL,
    content TEXT,
    source_timestamp TIMESTAMP,
    relevance_score DECIMAL(3,2),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- AI analysis results
CREATE TABLE ai_analysis (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    report_id UUID REFERENCES intelligence_reports(id) ON DELETE CASCADE,
    nlp_analysis TEXT,
    vision_analysis TEXT,
    predictive_analysis TEXT,
    key_entities JSONB,
    threat_keywords JSONB,
    sentiment_score DECIMAL(3,2),
    sentiment_label VARCHAR(20),
    feature_importance JSONB,
    explanation TEXT,
    model_version VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Alerts
CREATE TABLE alerts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    summary TEXT,
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    category VARCHAR(50),
    source_id UUID,
    source_type VARCHAR(50),
    source VARCHAR(100),
    timestamp TIMESTAMP NOT NULL,
    expires_at TIMESTAMP,
    location_lat DECIMAL(10,8),
    location_lng DECIMAL(11,8),
    location_address TEXT,
    location_city VARCHAR(100),
    location_region VARCHAR(100),
    location_country VARCHAR(100),
    content JSONB,
    tags JSONB,
    keywords JSONB,
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),
    assigned_to UUID REFERENCES users(id),
    verified BOOLEAN DEFAULT false,
    verification_notes TEXT,
    verified_at TIMESTAMP,
    verified_by UUID REFERENCES users(id),
    resolved BOOLEAN DEFAULT false,
    resolution_notes TEXT,
    resolved_at TIMESTAMP,
    resolved_by UUID REFERENCES users(id),
    confidence DECIMAL(3,2),
    threat_level VARCHAR(20),
    metadata JSONB
);

-- Alert notifications
CREATE TABLE alert_notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    alert_id UUID REFERENCES alerts(id) ON DELETE CASCADE,
    channel VARCHAR(20) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    sent_at TIMESTAMP,
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Alert escalations
CREATE TABLE alert_escalations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    alert_id UUID REFERENCES alerts(id) ON DELETE CASCADE,
    escalation_level INTEGER NOT NULL,
    escalated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    escalated_by UUID REFERENCES users(id),
    escalation_reason TEXT,
    next_escalation TIMESTAMP,
    escalation_history JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Risk forecasts
CREATE TABLE risk_forecasts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    forecast_type VARCHAR(50) NOT NULL,
    model_version VARCHAR(20),
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    valid_from TIMESTAMP NOT NULL,
    valid_to TIMESTAMP NOT NULL,
    overall_risk_trend DECIMAL(3,2),
    confidence_score DECIMAL(3,2),
    forecast_points JSONB,
    location_risks JSONB,
    hotspots JSONB,
    model_metadata JSONB,
    forecast_metrics JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Forecast points (for detailed time-series data)
CREATE TABLE forecast_points (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    forecast_id UUID REFERENCES risk_forecasts(id) ON DELETE CASCADE,
    timestamp TIMESTAMP NOT NULL,
    predicted_risk DECIMAL(3,2),
    confidence DECIMAL(3,2),
    feature_contributions JSONB,
    risk_level VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Hotspot predictions
CREATE TABLE hotspot_predictions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    forecast_id UUID REFERENCES risk_forecasts(id) ON DELETE CASCADE,
    hotspot_id VARCHAR(100),
    location_name VARCHAR(255),
    location_lat DECIMAL(10,8),
    location_lng DECIMAL(11,8),
    probability DECIMAL(3,2),
    radius DECIMAL(8,2),
    peak_time TIMESTAMP,
    threat_type VARCHAR(50),
    contributing_factors JSONB,
    confidence DECIMAL(3,2),
    severity VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Location risks
CREATE TABLE location_risks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    forecast_id UUID REFERENCES risk_forecasts(id) ON DELETE CASCADE,
    location_id VARCHAR(100),
    location_name VARCHAR(255),
    location_lat DECIMAL(10,8),
    location_lng DECIMAL(11,8),
    current_risk DECIMAL(3,2),
    predicted_risk DECIMAL(3,2),
    risk_change DECIMAL(3,2),
    trend_direction VARCHAR(20),
    confidence DECIMAL(3,2),
    risk_factors JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- System audit log
CREATE TABLE audit_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50),
    resource_id UUID,
    old_values JSONB,
    new_values JSONB,
    ip_address INET,
    user_agent TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB
);

-- System configuration
CREATE TABLE system_config (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    key VARCHAR(100) UNIQUE NOT NULL,
    value TEXT,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance optimization

-- Users and authentication
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- Intelligence reports
CREATE INDEX idx_intelligence_reports_threat_level ON intelligence_reports(threat_level);
CREATE INDEX idx_intelligence_reports_created_at ON intelligence_reports(created_at);
CREATE INDEX idx_intelligence_reports_location ON intelligence_reports USING GIST(ST_Point(location_lng, location_lat));
CREATE INDEX idx_intelligence_reports_status ON intelligence_reports(status);
CREATE INDEX idx_intelligence_reports_created_by ON intelligence_reports(created_by);

-- Threat sources
CREATE INDEX idx_threat_sources_report_id ON threat_sources(report_id);
CREATE INDEX idx_threat_sources_source_type ON threat_sources(source_type);

-- AI analysis
CREATE INDEX idx_ai_analysis_report_id ON ai_analysis(report_id);

-- Alerts
CREATE INDEX idx_alerts_severity ON alerts(severity);
CREATE INDEX idx_alerts_status ON alerts(status);
CREATE INDEX idx_alerts_timestamp ON alerts(timestamp);
CREATE INDEX idx_alerts_location ON alerts USING GIST(ST_Point(location_lng, location_lat));
CREATE INDEX idx_alerts_source_id ON alerts(source_id);
CREATE INDEX idx_alerts_created_at ON alerts(created_at);

-- Alert notifications
CREATE INDEX idx_alert_notifications_alert_id ON alert_notifications(alert_id);
CREATE INDEX idx_alert_notifications_status ON alert_notifications(status);

-- Risk forecasts
CREATE INDEX idx_risk_forecasts_valid_from ON risk_forecasts(valid_from);
CREATE INDEX idx_risk_forecasts_valid_to ON risk_forecasts(valid_to);
CREATE INDEX idx_risk_forecasts_type ON risk_forecasts(forecast_type);

-- Forecast points
CREATE INDEX idx_forecast_points_forecast_id ON forecast_points(forecast_id);
CREATE INDEX idx_forecast_points_timestamp ON forecast_points(timestamp);

-- Hotspot predictions
CREATE INDEX idx_hotspot_predictions_forecast_id ON hotspot_predictions(forecast_id);
CREATE INDEX idx_hotspot_predictions_location ON hotspot_predictions USING GIST(ST_Point(location_lng, location_lat));
CREATE INDEX idx_hotspot_predictions_peak_time ON hotspot_predictions(peak_time);

-- Location risks
CREATE INDEX idx_location_risks_forecast_id ON location_risks(forecast_id);
CREATE INDEX idx_location_risks_location ON location_risks USING GIST(ST_Point(location_lng, location_lat));

-- Audit log
CREATE INDEX idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX idx_audit_log_timestamp ON audit_log(timestamp);
CREATE INDEX idx_audit_log_action ON audit_log(action);

-- Full-text search indexes
CREATE INDEX idx_intelligence_reports_search ON intelligence_reports USING GIN(to_tsvector('english', title || ' ' || summary || ' ' || description));
CREATE INDEX idx_alerts_search ON alerts USING GIN(to_tsvector('english', title || ' ' || description || ' ' || summary));

-- Triggers for updated_at timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_intelligence_reports_updated_at BEFORE UPDATE ON intelligence_reports
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_alerts_updated_at BEFORE UPDATE ON alerts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_system_config_updated_at BEFORE UPDATE ON system_config
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert default data
INSERT INTO user_roles (name, description, permissions) VALUES
('admin', 'System Administrator', '{"read": true, "write": true, "delete": true, "admin": true}'),
('analyst', 'Threat Intelligence Analyst', '{"read": true, "write": true, "delete": false, "admin": false}'),
('viewer', 'Read-only Viewer', '{"read": true, "write": false, "delete": false, "admin": false}');

INSERT INTO system_config (key, value, description) VALUES
('threat_threshold_critical', '0.8', 'Critical threat level threshold'),
('threat_threshold_high', '0.6', 'High threat level threshold'),
('threat_threshold_medium', '0.4', 'Medium threat level threshold'),
('alert_retention_days', '90', 'Number of days to retain alerts'),
('forecast_horizon_hours', '24', 'Default forecast horizon in hours'),
('ai_confidence_threshold', '0.6', 'Minimum AI confidence threshold');

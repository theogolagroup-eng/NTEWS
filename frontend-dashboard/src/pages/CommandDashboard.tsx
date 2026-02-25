'use client';

import React, { useState, useEffect } from 'react';
import { 
  Card, 
  Row, 
  Col, 
  Statistic, 
  List, 
  Badge, 
  Tag, 
  Space, 
  Progress,
  Alert,
  Spin
} from 'antd';
import {
  SafetyOutlined,
  ExclamationCircleOutlined,
  FireOutlined,
  BellOutlined,
  EnvironmentOutlined,
  RiseOutlined,
  ClockCircleOutlined,
  LoadingOutlined
} from '@ant-design/icons';
import dynamic from 'next/dynamic';
import { API_ENDPOINTS, apiClient } from '@/services/api';
import { useTheme } from '@/contexts/ThemeContext';
import ActionPointsPanel from '@/components/action-points/ActionPointsPanel';

// Dynamically import the map component to avoid SSR issues
const ThreatMap = dynamic(() => import('@/components/maps/ThreatMap'), {
  ssr: false,
  loading: () => <div>Loading map...</div>
});

interface Alert {
  id: string;
  title: string;
  description: string;
  severity: 'critical' | 'high' | 'medium' | 'low';
  priority: 'low' | 'normal' | 'high' | 'urgent';
  status: 'active' | 'acknowledged' | 'investigating' | 'resolved' | 'closed' | 'false_positive';
  category: string;
  location?: {
    address: string;
    city: string;
    region: string;
    country: string;
    latitude: string;
    longitude: string;
  };
  createdAt: string;
  updatedAt: string;
  timestamp?: string;
}

interface DashboardData {
  intelligenceSummary: {
    totalReports: number;
    activeThreats: number;
    criticalThreats: number;
    highThreats: number;
    mediumThreats: number;
    lowThreats: number;
    categoryCounts: Array<{ category: string; count: number }>;
    recentThreats: Array<{
      id: string;
      title: string;
      threatLevel: string;
      timestamp: string;
      location: string;
    }>;
  };
  alertSummary: {
    totalAlerts: number;
    activeAlerts: number;
    unacknowledgedAlerts: number;
    criticalAlerts: number;
    highAlerts: number;
    mediumAlerts: number;
    lowAlerts: number;
    severityCounts: Array<{ severity: string; count: number }>;
    recentAlerts: Array<{
      id: string;
      title: string;
      severity: string;
      priority: string;
      status: string;
      timestamp: string;
      location: string;
    }>;
  };
  predictionSummary: {
    activeHotspots: number;
    highRiskHotspots: number;
    mediumRiskHotspots: number;
    lowRiskHotspots: number;
    currentRiskTrend: number;
    trendDirection: string | undefined;
    topHotspots: Array<{
      id: string;
      locationName: string;
      probability: number;
      severity: string;
      threatType: string;
    }>;
    recentTrends: Array<any>;
  };
}

// Internal component (not exported)
function CommandDashboard() {
  const { isDarkMode, themeStyles } = useTheme();
  const [dashboardData, setDashboardData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedThreat, setSelectedThreat] = useState<any>(null);

  useEffect(() => {
    fetchDashboardData();
    
    // Auto-refresh disabled to prevent UI interference
    // const refreshInterval = setInterval(fetchDashboardData, 30000); // Refresh every 30 seconds
    // return () => clearInterval(refreshInterval);
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Fetch all dashboard data in parallel through API Gateway with timeout
      const timeoutPromise = new Promise((_, reject) => 
        setTimeout(() => reject(new Error('Request timeout')), 10000)
      );
      
      const [intelData, alertData, predictionData, allAlerts] = await Promise.all([
        Promise.race([apiClient.get(API_ENDPOINTS.INTELLIGENCE.DASHBOARD).catch(() => ({ totalReports: 0, activeThreats: 0, criticalThreats: 0, highThreats: 0, mediumThreats: 0, lowThreats: 0, categoryCounts: [], recentThreats: [] })), timeoutPromise]),
        Promise.race([apiClient.get(API_ENDPOINTS.ALERTS.DASHBOARD).catch(() => ({ totalAlerts: 0, activeAlerts: 0, unacknowledgedAlerts: 0, criticalAlerts: 0, highAlerts: 0, mediumAlerts: 0, lowAlerts: 0, severityCounts: [], recentAlerts: [] })), timeoutPromise]),
        Promise.race([apiClient.get(API_ENDPOINTS.PREDICTIONS.DASHBOARD).catch(() => ({ activeHotspots: 0, highRiskHotspots: 0, mediumRiskHotspots: 0, lowRiskHotspots: 0, currentRiskTrend: 0.0, trendDirection: undefined, topHotspots: [], recentTrends: [] })), timeoutPromise]),
        Promise.race([apiClient.get(API_ENDPOINTS.ALERTS.ALL).catch(() => ({ content: [] })), timeoutPromise])
      ]);

      // Process alerts data similar to AlertsPage
      const alertsList = allAlerts?.content || [];
      const activeAlerts = alertsList.filter((alert: Alert) => alert.status === 'active');
      const criticalAlerts = activeAlerts.filter((alert: Alert) => alert.severity === 'critical');
      const highAlerts = activeAlerts.filter((alert: Alert) => alert.severity === 'high');
      const mediumAlerts = activeAlerts.filter((alert: Alert) => alert.severity === 'medium');
      const lowAlerts = activeAlerts.filter((alert: Alert) => alert.severity === 'low');
      
      // Create recent alerts list
      const recentAlerts = alertsList.slice(0, 5).map((alert: Alert) => ({
        id: alert.id,
        title: alert.title,
        severity: alert.severity,
        priority: alert.priority,
        status: alert.status,
        timestamp: alert.createdAt || alert.timestamp,
        location: alert.location?.address || alert.location
      }));

      setDashboardData({
        intelligenceSummary: intelData || { totalReports: 0, activeThreats: 0, criticalThreats: 0, highThreats: 0, mediumThreats: 0, lowThreats: 0, categoryCounts: [], recentThreats: [] },
        alertSummary: {
          totalAlerts: alertsList.length,
          activeAlerts: activeAlerts.length,
          unacknowledgedAlerts: activeAlerts.filter((alert: Alert) => alert.status === 'active').length,
          criticalAlerts: criticalAlerts.length,
          highAlerts: highAlerts.length,
          mediumAlerts: mediumAlerts.length,
          lowAlerts: lowAlerts.length,
          severityCounts: [
            { severity: 'critical', count: criticalAlerts.length },
            { severity: 'high', count: highAlerts.length },
            { severity: 'medium', count: mediumAlerts.length },
            { severity: 'low', count: lowAlerts.length }
          ],
          recentAlerts: recentAlerts
        },
        predictionSummary: predictionData || { activeHotspots: 0, highRiskHotspots: 0, mediumRiskHotspots: 0, lowRiskHotspots: 0, currentRiskTrend: 0.0, trendDirection: undefined, topHotspots: [], recentTrends: [] }
      });
      
    } catch (err) {
      setError('Failed to fetch dashboard data');
      console.error('Dashboard fetch error:', err);
      // Set default data to prevent UI crashes
      setDashboardData({
        intelligenceSummary: { totalReports: 0, activeThreats: 0, criticalThreats: 0, highThreats: 0, mediumThreats: 0, lowThreats: 0, categoryCounts: [], recentThreats: [] },
        alertSummary: { totalAlerts: 0, activeAlerts: 0, unacknowledgedAlerts: 0, criticalAlerts: 0, highAlerts: 0, mediumAlerts: 0, lowAlerts: 0, severityCounts: [], recentAlerts: [] },
        predictionSummary: { activeHotspots: 0, highRiskHotspots: 0, mediumRiskHotspots: 0, lowRiskHotspots: 0, currentRiskTrend: 0.0, trendDirection: undefined, topHotspots: [], recentTrends: [] }
      });
    } finally {
      setLoading(false);
    }
  };

  const getSeverityColor = (severity: string) => {
    const colors = {
      critical: 'red',
      high: 'orange',
      medium: 'gold',
      low: 'green'
    };
    return colors[severity as keyof typeof colors] || 'default';
  };

  const getTrendIcon = (trend: string) => {
    switch (trend) {
      case 'increasing': return <ExclamationCircleOutlined style={{ color: '#ff4d4f' }} />;
      case 'decreasing': return <SafetyOutlined style={{ color: '#52c41a' }} />;
      default: return <ClockCircleOutlined style={{ color: '#1890ff' }} />;
    }
  };

  if (loading) {
    return (
      <div style={{ padding: '24px', textAlign: 'center', background: themeStyles.background, minHeight: '100vh' }}>
        <LoadingOutlined style={{ fontSize: '48px', color: isDarkMode ? '#1890ff' : '#1890ff' }} />
        <h2 style={{ color: themeStyles.textColor, textShadow: themeStyles.textShadow }}>Loading NTEWS Command Dashboard...</h2>
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ padding: '24px', background: themeStyles.background, minHeight: '100vh' }}>
        <Alert
          message="Error"
          description={error}
          type="error"
          showIcon
          closable
        />
      </div>
    );
  }

  if (!dashboardData) return null;

  const { intelligenceSummary, alertSummary, predictionSummary } = dashboardData;

  return (
    <div style={{ 
      padding: '16px', 
      background: themeStyles.background, 
      minHeight: '100vh',
      color: themeStyles.textColor
    }}>
      {/* Header */}
      <div style={{ 
        marginBottom: '20px',
        background: themeStyles.headerBackground,
        padding: '16px 20px',
        borderRadius: '12px',
        backdropFilter: 'blur(10px)',
        border: themeStyles.cardBorder
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <h1 style={{ 
              fontSize: '24px', 
              fontWeight: '700', 
              margin: 0,
              background: isDarkMode 
                ? 'linear-gradient(135deg, #ff6b6b, #4ecdc4)' 
                : 'linear-gradient(135deg, #1890ff, #722ed1)',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
              backgroundClip: 'text',
              textShadow: 'none'
            }}>
              NTEWS COMMAND CENTER
            </h1>
            <p style={{ 
              color: themeStyles.secondaryTextColor, 
              marginTop: '4px',
              fontSize: '12px',
              fontWeight: '500',
              textShadow: themeStyles.textShadow
            }}>
              National Threat Early Warning System • Real-time Intelligence Operations
            </p>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <div style={{
              width: '8px',
              height: '8px',
              borderRadius: '50%',
              backgroundColor: '#52c41a',
              boxShadow: '0 0 8px #52c41a'
            }} />
            <span style={{ fontSize: '12px', color: '#52c41a', fontWeight: '600', textShadow: themeStyles.textShadow }}>
              SYSTEMS ONLINE
            </span>
          </div>
        </div>
      </div>

      {/* Key Metrics - Compact Cards */}
      <Row gutter={[12, 12]} style={{ marginBottom: '20px' }}>
        <Col xs={24} sm={12} lg={6}>
          <Card 
            style={{ 
              background: themeStyles.cardBackground,
              border: themeStyles.cardBorder,
              borderRadius: '8px'
            }}
            bodyStyle={{ padding: '16px' }}
          >
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div>
                <div style={{ fontSize: '24px', fontWeight: '700', color: '#1890ff', textShadow: themeStyles.textShadow }}>
                  {intelligenceSummary.activeThreats}
                </div>
                <div style={{ fontSize: '11px', color: themeStyles.secondaryTextColor, textTransform: 'uppercase', fontWeight: '600', textShadow: themeStyles.textShadow }}>
                  Active Threats
                </div>
                <div style={{ fontSize: '10px', color: '#52c41a', marginTop: '2px', textShadow: themeStyles.textShadow }}>
                  Total: {intelligenceSummary.totalReports}
                </div>
              </div>
              <SafetyOutlined style={{ fontSize: '20px', color: '#1890ff' }} />
            </div>
          </Card>
        </Col>
        
        <Col xs={24} sm={12} lg={6}>
          <Card 
            style={{ 
              background: themeStyles.cardBackground,
              border: themeStyles.cardBorder,
              borderRadius: '8px'
            }}
            bodyStyle={{ padding: '16px' }}
          >
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div>
                <div style={{ fontSize: '24px', fontWeight: '700', color: '#ff4d4f', textShadow: themeStyles.textShadow }}>
                  {alertSummary.criticalAlerts}
                </div>
                <div style={{ fontSize: '11px', color: themeStyles.secondaryTextColor, textTransform: 'uppercase', fontWeight: '600', textShadow: themeStyles.textShadow }}>
                  Critical Alerts
                </div>
                <div style={{ fontSize: '10px', color: '#fa8c16', marginTop: '2px', textShadow: themeStyles.textShadow }}>
                  Unack: {alertSummary.unacknowledgedAlerts}
                </div>
              </div>
              <ExclamationCircleOutlined style={{ fontSize: '20px', color: '#ff4d4f' }} />
            </div>
          </Card>
        </Col>
        
        <Col xs={24} sm={12} lg={6}>
          <Card 
            style={{ 
              background: themeStyles.cardBackground,
              border: themeStyles.cardBorder,
              borderRadius: '8px'
            }}
            bodyStyle={{ padding: '16px' }}
          >
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div>
                <div style={{ fontSize: '24px', fontWeight: '700', color: '#fa8c16', textShadow: themeStyles.textShadow }}>
                  {predictionSummary.activeHotspots}
                </div>
                <div style={{ fontSize: '11px', color: themeStyles.secondaryTextColor, textTransform: 'uppercase', fontWeight: '600', textShadow: themeStyles.textShadow }}>
                  Active Hotspots
                </div>
                <div style={{ fontSize: '10px', color: '#ff4d4f', marginTop: '2px', textShadow: themeStyles.textShadow }}>
                  High Risk: {predictionSummary.highRiskHotspots}
                </div>
              </div>
              <FireOutlined style={{ fontSize: '20px', color: '#fa8c16' }} />
            </div>
          </Card>
        </Col>
        
        <Col xs={24} sm={12} lg={6}>
          <Card 
            style={{ 
              background: themeStyles.cardBackground,
              border: themeStyles.cardBorder,
              borderRadius: '8px'
            }}
            bodyStyle={{ padding: '16px' }}
          >
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div>
                <div style={{ fontSize: '24px', fontWeight: '700', color: '#52c41a', textShadow: themeStyles.textShadow }}>
                  {alertSummary.activeAlerts}
                </div>
                <div style={{ fontSize: '11px', color: themeStyles.secondaryTextColor, textTransform: 'uppercase', fontWeight: '600', textShadow: themeStyles.textShadow }}>
                  Total Alerts
                </div>
                <div style={{ fontSize: '10px', color: '#1890ff', marginTop: '2px', textShadow: themeStyles.textShadow }}>
                  System Active
                </div>
              </div>
              <BellOutlined style={{ fontSize: '20px', color: '#52c41a' }} />
            </div>
          </Card>
        </Col>
      </Row>

      {/* Main Content Area */}
      <Row gutter={[12, 12]}>
        {/* Left Column - Alerts & Hotspots */}
        <Col xs={24} lg={16}>
          {/* Recent Alerts */}
          <Card 
            style={{ 
              background: themeStyles.cardBackground,
              border: themeStyles.cardBorder,
              borderRadius: '8px',
              marginBottom: '12px'
            }}
            title={
              <div style={{ color: themeStyles.textColor, fontSize: '14px', fontWeight: '600', textShadow: themeStyles.textShadow }}>
                <BellOutlined style={{ marginRight: '8px', color: '#52c41a' }} />
                LIVE ALERTS
                <Badge count={alertSummary.activeAlerts} style={{ marginLeft: '8px', backgroundColor: '#52c41a' }} />
              </div>
            }
            bodyStyle={{ padding: '12px' }}
          >
            <List
              dataSource={alertSummary.recentAlerts || []}
              renderItem={(item: any) => (
                <List.Item style={{ 
                  padding: '8px 0',
                  borderBottom: isDarkMode ? '1px solid rgba(255,255,255,0.05)' : '1px solid rgba(0,0,0,0.06)'
                }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
                    <div style={{ flex: 1 }}>
                      <div style={{ 
                        fontSize: '13px', 
                        fontWeight: '600', 
                        color: themeStyles.textColor,
                        marginBottom: '2px',
                        textShadow: themeStyles.textShadow
                      }}>
                        {item.title}
                      </div>
                      <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                        <Tag 
                          color={getSeverityColor(item.severity)}
                          style={{ fontSize: '10px', padding: '2px 6px', border: 'none' }}
                        >
                          {item.severity?.toUpperCase()}
                        </Tag>
                        <Tag 
                          color="blue"
                          style={{ fontSize: '10px', padding: '2px 6px', border: 'none' }}
                        >
                          {item.priority?.toUpperCase()}
                        </Tag>
                        <span style={{ fontSize: '11px', color: themeStyles.secondaryTextColor, textShadow: themeStyles.textShadow }}>
                          {item.location || 'Unknown Location'}
                        </span>
                      </div>
                    </div>
                    <div style={{ fontSize: '11px', color: themeStyles.secondaryTextColor, textShadow: themeStyles.textShadow }}>
                      {item.timestamp ? new Date(item.timestamp).toLocaleTimeString() : 'Unknown Time'}
                    </div>
                  </div>
                </List.Item>
              )}
            />
          </Card>

          {/* Top Hotspots */}
          <Card 
            style={{ 
              background: themeStyles.cardBackground,
              border: themeStyles.cardBorder,
              borderRadius: '8px'
            }}
            title={
              <div style={{ color: themeStyles.textColor, fontSize: '14px', fontWeight: '600', textShadow: themeStyles.textShadow }}>
                <FireOutlined style={{ marginRight: '8px', color: '#fa8c16' }} />
                THREAT HOTSPOTS
                <Badge count={predictionSummary.activeHotspots} style={{ marginLeft: '8px', backgroundColor: '#fa8c16' }} />
              </div>
            }
            bodyStyle={{ padding: '12px' }}
          >
            <Row gutter={[8, 8]}>
              {(predictionSummary.topHotspots || []).slice(0, 4).map((hotspot: any, index: number) => (
                <Col xs={24} sm={12} key={index}>
                  <div style={{
                    background: isDarkMode ? 'rgba(255,255,255,0.02)' : 'rgba(0,0,0,0.02)',
                    border: isDarkMode ? '1px solid rgba(255,255,255,0.05)' : '1px solid rgba(0,0,0,0.06)',
                    borderRadius: '6px',
                    padding: '12px'
                  }}>
                    <div style={{ 
                      fontSize: '12px', 
                      fontWeight: '600', 
                      color: themeStyles.textColor,
                      marginBottom: '4px',
                      textShadow: themeStyles.textShadow
                    }}>
                      {hotspot.locationName}
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <Tag 
                        color={hotspot.severity === 'high' ? 'red' : 'orange'}
                        style={{ fontSize: '9px', padding: '1px 4px', border: 'none' }}
                      >
                        {hotspot.severity?.toUpperCase()}
                      </Tag>
                      <span style={{ fontSize: '11px', color: '#52c41a', fontWeight: '600', textShadow: themeStyles.textShadow }}>
                        {(hotspot.probability * 100).toFixed(0)}%
                      </span>
                    </div>
                    <div style={{ fontSize: '10px', color: themeStyles.secondaryTextColor, marginTop: '2px', textShadow: themeStyles.textShadow }}>
                      {hotspot.threatType}
                    </div>
                  </div>
                </Col>
              ))}
            </Row>
          </Card>
        </Col>

        {/* Right Column - Map, Stats & Actions */}
        <Col xs={24} lg={8}>
          {/* Threat Map */}
          <Card 
            style={{ 
              background: themeStyles.cardBackground,
              border: themeStyles.cardBorder,
              borderRadius: '8px',
              marginBottom: '12px'
            }}
            title={
              <div style={{ color: themeStyles.textColor, fontSize: '14px', fontWeight: '600', textShadow: themeStyles.textShadow }}>
                <EnvironmentOutlined style={{ marginRight: '8px', color: '#1890ff' }} />
                THREAT MAP
              </div>
            }
            bodyStyle={{ padding: '12px', height: '300px' }}
          >
            <div style={{
              height: '100%',
              background: isDarkMode ? 'rgba(0,0,0,0.2)' : 'rgba(0,0,0,0.05)',
              borderRadius: '4px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: themeStyles.secondaryTextColor,
              fontSize: '12px',
              textShadow: themeStyles.textShadow
            }}>
              Interactive threat mapping visualization
            </div>
          </Card>

          {/* Severity Distribution */}
          <Card 
            style={{ 
              background: themeStyles.cardBackground,
              border: themeStyles.cardBorder,
              borderRadius: '8px',
              marginBottom: '12px'
            }}
            title={
              <div style={{ color: themeStyles.textColor, fontSize: '14px', fontWeight: '600', textShadow: themeStyles.textShadow }}>
                <RiseOutlined style={{ marginRight: '8px', color: '#722ed1' }} />
                THREAT LEVELS
              </div>
            }
            bodyStyle={{ padding: '12px' }}
          >
            <Space direction="vertical" style={{ width: '100%' }}>
              {alertSummary.severityCounts?.map((count: any) => (
                <div key={count.severity} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ fontSize: '12px', color: themeStyles.secondaryTextColor, textShadow: themeStyles.textShadow }}>
                    {count.severity?.toUpperCase()}
                  </span>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <Progress 
                      percent={(count.count / Math.max(1, alertSummary.totalAlerts)) * 100} 
                      size="small" 
                      strokeColor={getSeverityColor(count.severity)}
                      showInfo={false}
                      style={{ width: '60px' }}
                    />
                    <Tag 
                      color={getSeverityColor(count.severity)}
                      style={{ fontSize: '10px', padding: '1px 4px', border: 'none' }}
                    >
                      {count.count}
                    </Tag>
                  </div>
                </div>
              ))}
            </Space>
          </Card>

          {/* Action Points Panel */}
          <Card 
            style={{ 
              background: themeStyles.cardBackground,
              border: themeStyles.cardBorder,
              borderRadius: '8px'
            }}
            title={
              <div style={{ color: themeStyles.textColor, fontSize: '14px', fontWeight: '600', textShadow: themeStyles.textShadow }}>
                <BellOutlined style={{ marginRight: '8px', color: '#52c41a' }} />
                ACTION POINTS
              </div>
            }
            bodyStyle={{ padding: '12px' }}
          >
            <ActionPointsPanel />
          </Card>
        </Col>
      </Row>
    </div>
  );
}

// Export the wrapped component as default
export default function CommandDashboardWithTheme() {
  return (
    <React.Fragment>
      <CommandDashboard />
    </React.Fragment>
  );
}

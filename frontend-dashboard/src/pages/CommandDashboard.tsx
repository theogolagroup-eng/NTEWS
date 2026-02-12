'use client';

import React, { useState, useEffect } from 'react';
import { 
  Card, 
  Row, 
  Col, 
  Statistic, 
  Alert, 
  Badge, 
  Table, 
  Button,
  Space,
  Tag,
  Tooltip,
  Progress,
  Timeline,
  List,
  Avatar
} from 'antd';
import {
  WarningOutlined,
  RiseOutlined,
  EnvironmentOutlined,
  ThunderboltOutlined,
  SafetyOutlined,
  ClockCircleOutlined,
  EyeOutlined,
  ReloadOutlined,
  FireOutlined,
  LoadingOutlined
} from '@ant-design/icons';
import dynamic from 'next/dynamic';
import { API_ENDPOINTS, apiClient } from '@/services/api';

// Dynamically import the map component to avoid SSR issues
const ThreatMap = dynamic(() => import('@/components/maps/ThreatMap'), {
  ssr: false,
  loading: () => <div>Loading map...</div>
});

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
    trendDirection: string | null;
    topHotspots: Array<{
      id: string;
      locationName: string;
      probability: number;
      severity: string;
      threatType: string;
    }>;
    recentTrends: Array<{
      timestamp: string;
      trend: number;
      confidence: number;
    }>;
  };
}

export default function CommandDashboard() {
  const [dashboardData, setDashboardData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedThreat, setSelectedThreat] = useState<any>(null);

  useEffect(() => {
    fetchDashboardData();
    
    // Set up periodic refresh
    const refreshInterval = setInterval(fetchDashboardData, 30000); // Refresh every 30 seconds
    
    // Set up WebSocket for real-time updates with reconnection logic
    let ws: WebSocket | null = null;
    let reconnectAttempts = 0;
    const maxReconnectAttempts = 5;
    
    const connectWebSocket = () => {
      try {
        ws = new WebSocket('ws://localhost:8084/ws/alerts');
        
        ws.onopen = () => {
          console.log('WebSocket connected');
          reconnectAttempts = 0;
        };
        
        ws.onmessage = (event) => {
          try {
            const data = JSON.parse(event.data);
            console.log('Real-time alert:', data);
            fetchDashboardData(); // Refresh data when new alert arrives
          } catch (error) {
            console.error('Error parsing WebSocket message:', error);
          }
        };
        
        ws.onclose = () => {
          console.log('WebSocket disconnected');
          // Attempt to reconnect
          if (reconnectAttempts < maxReconnectAttempts) {
            reconnectAttempts++;
            setTimeout(connectWebSocket, 1000 * reconnectAttempts);
          }
        };
        
        ws.onerror = (error) => {
          console.error('WebSocket error:', error);
        };
      } catch (error) {
        console.error('Failed to create WebSocket connection:', error);
      }
    };
    
    connectWebSocket();
    
    return () => {
      if (refreshInterval) clearInterval(refreshInterval);
      if (ws) ws.close();
    };
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Fetch all dashboard data in parallel through API Gateway with timeout
      const timeoutPromise = new Promise((_, reject) => 
        setTimeout(() => reject(new Error('Request timeout')), 10000)
      );
      
      const [intelData, alertData, predictionData] = await Promise.all([
        Promise.race([apiClient.get(API_ENDPOINTS.INTELLIGENCE.DASHBOARD), timeoutPromise]),
        Promise.race([apiClient.get(API_ENDPOINTS.ALERTS.DASHBOARD), timeoutPromise]),
        Promise.race([apiClient.get(API_ENDPOINTS.PREDICTIONS.DASHBOARD), timeoutPromise])
      ]);

      setDashboardData({
        intelligenceSummary: intelData || { totalReports: 0, activeThreats: 0, criticalThreats: 0, highThreats: 0, mediumThreats: 0, lowThreats: 0, categoryCounts: [], recentThreats: [] },
        alertSummary: alertData || { totalAlerts: 0, activeAlerts: 0, unacknowledgedAlerts: 0, criticalAlerts: 0, highAlerts: 0, mediumAlerts: 0, lowAlerts: 0, severityCounts: [], recentAlerts: [] },
        predictionSummary: predictionData || { activeHotspots: 0, highRiskHotspots: 0, mediumRiskHotspots: 0, lowRiskHotspots: 0, currentRiskTrend: 0.0, trendDirection: null, topHotspots: [], recentTrends: [] }
      });
      
    } catch (err) {
      setError('Failed to fetch dashboard data');
      console.error('Dashboard fetch error:', err);
      // Set default data to prevent UI crashes
      setDashboardData({
        intelligenceSummary: { totalReports: 0, activeThreats: 0, criticalThreats: 0, highThreats: 0, mediumThreats: 0, lowThreats: 0, categoryCounts: [], recentThreats: [] },
        alertSummary: { totalAlerts: 0, activeAlerts: 0, unacknowledgedAlerts: 0, criticalAlerts: 0, highAlerts: 0, mediumAlerts: 0, lowAlerts: 0, severityCounts: [], recentAlerts: [] },
        predictionSummary: { activeHotspots: 0, highRiskHotspots: 0, mediumRiskHotspots: 0, lowRiskHotspots: 0, currentRiskTrend: 0.0, trendDirection: null, topHotspots: [], recentTrends: [] }
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

  const getThreatLevelColor = (level: string) => {
    const colors = {
      critical: '#ff4d4f',
      high: '#ff7a45',
      medium: '#ffa940',
      low: '#52c41a'
    };
    return colors[level as keyof typeof colors] || '#d9d9d9';
  };

  const getTrendIcon = (direction: string) => {
    switch (direction) {
      case 'increasing': return <RiseOutlined style={{ color: '#ff4d4f' }} />;
      case 'decreasing': return <RiseOutlined style={{ color: '#52c41a', transform: 'rotate(180deg)' }} />;
      default: return <RiseOutlined style={{ color: '#d9d9d9' }} />;
    }
  };

  const alertColumns = [
    {
      title: 'Alert',
      dataIndex: 'title',
      key: 'title',
      render: (text: string, record: any) => (
        <Space>
          <WarningOutlined style={{ color: getThreatLevelColor(record.severity) }} />
          <span>{text}</span>
        </Space>
      ),
    },
    {
      title: 'Severity',
      dataIndex: 'severity',
      key: 'severity',
      render: (severity: string) => (
        <Tag color={getSeverityColor(severity)}>{severity.toUpperCase()}</Tag>
      ),
    },
    {
      title: 'Location',
      dataIndex: 'location',
      key: 'location',
      render: (location: string) => (
        <Space>
          <EnvironmentOutlined />
          <span>{location}</span>
        </Space>
      ),
    },
    {
      title: 'Time',
      dataIndex: 'timestamp',
      key: 'timestamp',
      render: (timestamp: string) => (
        <Space>
          <ClockCircleOutlined />
          <span suppressHydrationWarning>{new Date(timestamp).toLocaleTimeString()}</span>
        </Space>
      ),
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (record: any) => (
        <Space>
          <Button size="small" icon={<EyeOutlined />} onClick={() => setSelectedThreat(record)}>
            View
          </Button>
        </Space>
      ),
    },
  ];

  if (loading) {
    return (
      <div style={{ padding: '24px', textAlign: 'center' }}>
        <LoadingOutlined style={{ fontSize: '48px', color: '#1890ff' }} />
        <h2>Loading NTEWS Command Dashboard...</h2>
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ padding: '24px' }}>
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
    <div style={{ padding: '24px', background: '#f0f2f5', minHeight: '100vh' }}>
      {/* Header */}
      <div style={{ marginBottom: '24px' }}>
        <h1 style={{ fontSize: '28px', fontWeight: 'bold', margin: 0 }}>
          NTEWS Command Dashboard
        </h1>
        <p style={{ color: '#666', marginTop: '8px' }}>
          National Threat Early Warning System - Real-time Intelligence
        </p>
      </div>

      {/* Threat Level Banner */}
      <Alert
        message={
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <div 
                  style={{ 
                    width: 12, 
                    height: 12, 
                    borderRadius: '50%', 
                    backgroundColor: getThreatLevelColor(
                      intelligenceSummary.criticalThreats > 0 ? 'critical' :
                      intelligenceSummary.highThreats > 5 ? 'high' :
                      intelligenceSummary.activeThreats > 10 ? 'medium' : 'low'
                    )
                  }}
                />
                <strong style={{ 
                  color: getThreatLevelColor(
                    intelligenceSummary.criticalThreats > 0 ? 'critical' :
                    intelligenceSummary.highThreats > 5 ? 'high' :
                    intelligenceSummary.activeThreats > 10 ? 'medium' : 'low'
                  )
                }}>
                  {intelligenceSummary.criticalThreats > 0 ? 'CRITICAL' :
                   intelligenceSummary.highThreats > 5 ? 'HIGH' :
                   intelligenceSummary.activeThreats > 10 ? 'MEDIUM' : 'LOW'} THREAT LEVEL
                </strong>
              </div>
              
              <div style={{ display: 'flex', alignItems: 'center', gap: '24px' }}>
                <span>
                  TPI: <strong>{Math.min(95, Math.max(5, intelligenceSummary.activeThreats * 5 + intelligenceSummary.criticalThreats * 20))}</strong>
                </span>
                <span>
                  Active Alerts: <strong>{alertSummary.activeAlerts}</strong>
                </span>
                <span style={{ color: '#999', fontSize: '12px' }} suppressHydrationWarning>
                  Last Update: {new Date().toLocaleTimeString()}
                </span>
              </div>
            </div>
            
            <Button type="primary" onClick={fetchDashboardData} icon={<ThunderboltOutlined />}>
              Refresh
            </Button>
          </div>
        }
        type={intelligenceSummary.criticalThreats > 0 ? 'error' : 
              intelligenceSummary.highThreats > 5 ? 'warning' : 
              intelligenceSummary.activeThreats > 10 ? 'info' : 'success'}
        showIcon={false}
        style={{ marginBottom: '24px' }}
      />

      {/* Key Metrics */}
      <Row gutter={[16, 16]} style={{ marginBottom: '24px' }}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Active Threats"
              value={intelligenceSummary.activeThreats}
              prefix={<SafetyOutlined />}
              valueStyle={{ color: '#1890ff' }}
              suffix={`/ ${intelligenceSummary.totalReports}`}
            />
          </Card>
        </Col>
        
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Critical Alerts"
              value={alertSummary.criticalAlerts}
              prefix={<ClockCircleOutlined />}
              valueStyle={{ color: '#ff4d4f' }}
              suffix={`Unack: ${alertSummary.unacknowledgedAlerts}`}
            />
          </Card>
        </Col>
        
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Active Hotspots"
              value={predictionSummary.activeHotspots}
              prefix={<FireOutlined />}
              valueStyle={{ color: '#fa8c16' }}
              suffix={`High: ${predictionSummary.highRiskHotspots}`}
            />
          </Card>
        </Col>
        
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Risk Trend"
              value={predictionSummary.currentRiskTrend ? 
                `${(predictionSummary.currentRiskTrend * 100).toFixed(1)}%` : '0%'}
              prefix={getTrendIcon(predictionSummary.trendDirection || 'stable')}
              valueStyle={{ 
                color: predictionSummary.trendDirection === 'increasing' ? '#ff4d4f' : 
                       predictionSummary.trendDirection === 'decreasing' ? '#52c41a' : '#1890ff'
              }}
              suffix={predictionSummary.trendDirection || 'stable'}
            />
          </Card>
        </Col>
      </Row>

      {/* Main Content */}
      <Row gutter={[16, 16]}>
        {/* Threat Map */}
        <Col xs={24} lg={16}>
          <Card title="Threat Map - Real-time Intelligence" style={{ height: '500px' }}>
            <ThreatMap />
          </Card>
        </Col>
        
        {/* Top Alerts Panel */}
        <Col xs={24} lg={8}>
          <Card 
            title="Top Priority Alerts" 
            extra={<Badge count={alertSummary.activeAlerts} overflowCount={99} />}
            style={{ height: '500px', overflow: 'auto' }}
          >
            <List
              dataSource={alertSummary.recentAlerts?.slice(0, 8) || []}
              renderItem={(item: any) => (
                <List.Item
                  actions={[
                    <Button size="small" type="link" onClick={() => setSelectedThreat(item)}>
                      View
                    </Button>
                  ]}
                >
                  <List.Item.Meta
                    avatar={
                      <Avatar 
                        icon={<WarningOutlined />} 
                        style={{ backgroundColor: getThreatLevelColor(item.severity) }}
                      />
                    }
                    title={
                      <Space>
                        <span style={{ fontSize: '14px' }}>{item.title}</span>
                        <Tag color={getSeverityColor(item.severity)} style={{ fontSize: '12px' }}>
                          {item.severity}
                        </Tag>
                      </Space>
                    }
                    description={
                      <Space direction="vertical" size="small" style={{ width: '100%' }}>
                        <span style={{ fontSize: '12px', color: '#666' }}>
                          <EnvironmentOutlined style={{ fontSize: '10px' }} /> {item.location}
                        </span>
                        <span style={{ fontSize: '12px', color: '#999' }} suppressHydrationWarning>
                          <ClockCircleOutlined style={{ fontSize: '10px' }} /> {new Date(item.timestamp).toLocaleString()}
                        </span>
                      </Space>
                    }
                  />
                </List.Item>
              )}
            />
          </Card>
        </Col>
      </Row>

      {/* Bottom Section */}
      <Row gutter={[16, 16]} style={{ marginTop: '16px' }}>
        {/* Threat Level Distribution */}
        <Col xs={24} lg={8}>
          <Card title="Threat Level Distribution">
            <Space direction="vertical" style={{ width: '100%' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span>Critical</span>
                <Space>
                  <Progress 
                    percent={(intelligenceSummary.criticalThreats / Math.max(1, intelligenceSummary.activeThreats)) * 100} 
                    size="small" 
                    strokeColor="#ff4d4f"
                    showInfo={false}
                    style={{ width: '100px' }}
                  />
                  <Tag color="red">{intelligenceSummary.criticalThreats}</Tag>
                </Space>
              </div>
              
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span>High</span>
                <Space>
                  <Progress 
                    percent={(intelligenceSummary.highThreats / Math.max(1, intelligenceSummary.activeThreats)) * 100} 
                    size="small" 
                    strokeColor="#ff7a45"
                    showInfo={false}
                    style={{ width: '100px' }}
                  />
                  <Tag color="orange">{intelligenceSummary.highThreats}</Tag>
                </Space>
              </div>
              
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span>Medium</span>
                <Space>
                  <Progress 
                    percent={(intelligenceSummary.mediumThreats / Math.max(1, intelligenceSummary.activeThreats)) * 100} 
                    size="small" 
                    strokeColor="#ffa940"
                    showInfo={false}
                    style={{ width: '100px' }}
                  />
                  <Tag color="gold">{intelligenceSummary.mediumThreats}</Tag>
                </Space>
              </div>
              
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span>Low</span>
                <Space>
                  <Progress 
                    percent={(intelligenceSummary.lowThreats / Math.max(1, intelligenceSummary.activeThreats)) * 100} 
                    size="small" 
                    strokeColor="#52c41a"
                    showInfo={false}
                    style={{ width: '100px' }}
                  />
                  <Tag color="green">{intelligenceSummary.lowThreats}</Tag>
                </Space>
              </div>
            </Space>
          </Card>
        </Col>

        {/* Recent Threat Intelligence */}
        <Col xs={24} lg={16}>
          <Card title="Recent Threat Intelligence">
            <Table
              columns={alertColumns}
              dataSource={intelligenceSummary.recentThreats?.slice(0, 5) || []}
              pagination={false}
              size="small"
              rowKey="id"
            />
          </Card>
        </Col>
      </Row>

      {/* Top Hotspots */}
      <Row gutter={[16, 16]} style={{ marginTop: '16px' }}>
        <Col span={24}>
          <Card title="Top Threat Hotspots">
            <Row gutter={[16, 16]}>
              {predictionSummary.topHotspots?.slice(0, 6).map((hotspot) => (
                <Col xs={24} sm={12} lg={8} xl={6} key={hotspot.id}>
                  <Card size="small" style={{ textAlign: 'center' }}>
                    <div style={{ marginBottom: '12px' }}>
                      <FireOutlined style={{ fontSize: '24px', color: getThreatLevelColor(hotspot.severity) }} />
                    </div>
                    <h4 style={{ margin: '8px 0' }}>{hotspot.locationName}</h4>
                    <Tag color={getSeverityColor(hotspot.severity)} style={{ marginBottom: '8px' }}>
                      {hotspot.severity.toUpperCase()}
                    </Tag>
                    <div style={{ fontSize: '12px', color: '#666', marginBottom: '8px' }}>
                      {hotspot.threatType}
                    </div>
                    <Progress
                      type="circle"
                      percent={Math.round(hotspot.probability * 100)}
                      size={60}
                      strokeColor={getThreatLevelColor(hotspot.severity)}
                    />
                  </Card>
                </Col>
              ))}
            </Row>
          </Card>
        </Col>
      </Row>
    </div>
  );
}

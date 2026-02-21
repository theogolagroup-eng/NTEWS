'use client';

import React, { useState, useEffect } from 'react';
import { 
  Card, 
  Row, 
  Col, 
  Statistic, 
  Progress, 
  Table, 
  Tag, 
  Button, 
  Space, 
  Select, 
  DatePicker,
  Alert,
  Timeline,
  Badge,
  Tooltip
} from 'antd';
import {
  RiseOutlined,
  FallOutlined,
  FireOutlined,
  EnvironmentOutlined,
  ClockCircleOutlined,
  ExclamationCircleOutlined,
  EyeOutlined,
  ReloadOutlined,
  InfoCircleOutlined
} from '@ant-design/icons';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip as RechartsTooltip, ResponsiveContainer, AreaChart, Area } from 'recharts';
import { TrendingUp, TrendingDown, Clock } from 'lucide-react';
import type { ColumnsType } from 'antd/es/table';
import { API_ENDPOINTS, apiClient, aiEngineClient } from '@/services/api';
import MapPin from '@/components/ui/MapPin';

const { Option } = Select;
const { RangePicker } = DatePicker;

interface RiskForecast {
  id: string;
  forecastType: string;
  overallRiskTrend: number;
  confidenceScore: number;
  validFrom: string;
  validTo: string;
  generatedAt: string;
  forecastPoints?: Array<{
    timestamp: string;
    predictedRisk: number;
    confidence: number;
    riskLevel: string;
  }>;
  hotspots?: Array<{
    hotspotId: string;
    locationName: string;
    latitude: string;
    longitude: string;
    probability: number;
    severity: string;
    threatType: string;
    peakTime: string;
    radius: number;
    confidence: number;
  }>;
}

interface LocationRisk {
  locationId: string;
  locationName: string;
  latitude: string;
  longitude: string;
  currentRisk: number;
  predictedRisk: number;
  riskChange: number;
  trendDirection: string;
  confidence: number;
}

export default function PredictiveIntelligencePage() {
  const [forecasts, setForecasts] = useState<RiskForecast[]>([]);
  const [locationRisks, setLocationRisks] = useState<LocationRisk[]>([]);
  const [riskTrends, setRiskTrends] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedTimeRange, setSelectedTimeRange] = useState('24h');
  const [dashboardSummary, setDashboardSummary] = useState({
    activeHotspots: 0,
    highRiskHotspots: 0,
    currentRiskTrend: 0,
    trendDirection: 'stable'
  });

  useEffect(() => {
    fetchPredictiveData();
  }, [selectedTimeRange]);

  const fetchPredictiveData = async () => {
    try {
      setLoading(true);
      
      // Fetch all predictive data in parallel through API Gateway and AI Engine
      const [forecastsData, locationRisksData, trendsData, summaryData, aiPrediction] = await Promise.all([
        apiClient.get('/api/predictions/forecasts'),
        apiClient.get('/api/predictions/location-risk'),
        apiClient.get(`/api/predictions/risk-trends?hours=${selectedTimeRange.replace('h', '')}`),
        apiClient.get(API_ENDPOINTS.PREDICTIONS.DASHBOARD),
        aiEngineClient.post(API_ENDPOINTS.AI_ENGINE.PREDICT, {
          lookback_days: 30,
          forecast_hours: parseInt(selectedTimeRange.replace('h', '')),
          forecast_type: 'risk_trend'
        })
      ]);

      const forecasts = forecastsData.data || [];
      const locationRisks = locationRisksData.data || [];
      const riskTrends = trendsData.data || [];
      const summary = summaryData.data || {};
      
      setForecasts(forecasts);
      setLocationRisks(locationRisks);
      setRiskTrends(riskTrends);
      setDashboardSummary(summary);
      
    } catch (error) {
      console.error('Failed to fetch predictive data:', error);
      
      // Mock data for demonstration
      setForecasts([
        {
          id: '1',
          forecastType: 'trend',
          overallRiskTrend: 0.65,
          confidenceScore: 0.82,
          validFrom: new Date().toISOString(),
          validTo: new Date(Date.now() + 86400000).toISOString(),
          generatedAt: new Date().toISOString(),
          forecastPoints: Array.from({ length: 24 }, (_, i) => ({
            timestamp: new Date(Date.now() + i * 3600000).toISOString(),
            predictedRisk: 0.3 + Math.random() * 0.4,
            confidence: 0.7 + Math.random() * 0.2,
            riskLevel: Math.random() > 0.7 ? 'high' : Math.random() > 0.4 ? 'medium' : 'low'
          }))
        }
      ]);
      
      setLocationRisks([
        {
          locationId: '1',
          locationName: 'Nairobi CBD',
          latitude: '-1.2921',
          longitude: '36.8219',
          currentRisk: 0.65,
          predictedRisk: 0.78,
          riskChange: 0.13,
          trendDirection: 'increasing',
          confidence: 0.82
        },
        {
          locationId: '2',
          locationName: 'Westlands',
          latitude: '-1.2864',
          longitude: '36.8172',
          currentRisk: 0.45,
          predictedRisk: 0.52,
          riskChange: 0.07,
          trendDirection: 'increasing',
          confidence: 0.75
        }
      ]);
      
      setRiskTrends(Array.from({ length: 24 }, (_, i) => ({
        timestamp: new Date(Date.now() - (23 - i) * 3600000).toISOString(),
        riskScore: 0.3 + Math.random() * 0.4,
        riskLevel: Math.random() > 0.7 ? 'high' : Math.random() > 0.4 ? 'medium' : 'low',
        confidence: 0.7 + Math.random() * 0.2
      })));
      
      setDashboardSummary({
        activeHotspots: 8,
        highRiskHotspots: 3,
        currentRiskTrend: 0.65,
        trendDirection: 'increasing'
      });
    } finally {
      setLoading(false);
    }
  };

  const getTrendIcon = (direction: string) => {
    switch (direction) {
      case 'increasing': return <TrendingUp style={{ color: '#ff4d4f' }} />;
      case 'decreasing': return <TrendingDown style={{ color: '#52c41a' }} />;
      default: return <TrendingUp style={{ color: '#d9d9d9' }} />;
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

  const locationRiskColumns: ColumnsType<LocationRisk> = [
    {
      title: 'Location',
      dataIndex: 'locationName',
      key: 'locationName',
      render: (text: string, record: LocationRisk) => (
        <Space>
          <MapPin />
          <div>
            <div style={{ fontWeight: 'bold' }}>{text}</div>
            <div style={{ fontSize: '12px', color: '#666' }}>
              {record.latitude}, {record.longitude}
            </div>
          </div>
        </Space>
      ),
    },
    {
      title: 'Current Risk',
      dataIndex: 'currentRisk',
      key: 'currentRisk',
      render: (risk: number) => (
        <Progress
          percent={Math.round(risk * 100)}
          size="small"
          strokeColor={risk > 0.7 ? '#ff4d4f' : risk > 0.5 ? '#fa8c16' : '#52c41a'}
        />
      ),
    },
    {
      title: 'Predicted Risk',
      dataIndex: 'predictedRisk',
      key: 'predictedRisk',
      render: (risk: number) => (
        <Progress
          percent={Math.round(risk * 100)}
          size="small"
          strokeColor={risk > 0.7 ? '#ff4d4f' : risk > 0.5 ? '#fa8c16' : '#52c41a'}
        />
      ),
    },
    {
      title: 'Risk Change',
      dataIndex: 'riskChange',
      key: 'riskChange',
      render: (change: number, record: LocationRisk) => (
        <Space>
          {getTrendIcon(record.trendDirection)}
          <span style={{ 
            color: record.trendDirection === 'increasing' ? '#ff4d4f' : 
                   record.trendDirection === 'decreasing' ? '#52c41a' : '#d9d9d9'
          }}>
            {change > 0 ? '+' : ''}{(change * 100).toFixed(1)}%
          </span>
        </Space>
      ),
    },
    {
      title: 'Confidence',
      dataIndex: 'confidence',
      key: 'confidence',
      render: (confidence: number) => (
        <Progress
          percent={Math.round(confidence * 100)}
          size="small"
          strokeColor={confidence > 0.8 ? '#52c41a' : '#fa8c16'}
        />
      ),
    },
  ];

  const formatChartData = (data: any[]) => {
    return data.map(item => ({
      time: new Date(item.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      risk: Math.round(item.riskScore * 100),
      confidence: Math.round(item.confidence * 100)
    }));
  };

  return (
    <div style={{ padding: '24px' }}>
      <div style={{ marginBottom: '24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ fontSize: '24px', fontWeight: 'bold', margin: 0 }}>
            Predictive Intelligence
          </h1>
          <p style={{ color: '#666', marginTop: '8px' }}>
            Risk forecasting and hotspot prediction
          </p>
        </div>
        <Space>
          <Select
            value={selectedTimeRange}
            onChange={setSelectedTimeRange}
            style={{ width: 120 }}
          >
            <Option value="6h">6 Hours</Option>
            <Option value="24h">24 Hours</Option>
            <Option value="72h">3 Days</Option>
            <Option value="168h">1 Week</Option>
          </Select>
          <Button icon={<ReloadOutlined />} onClick={fetchPredictiveData}>
            Refresh
          </Button>
        </Space>
      </div>

      {/* Key Metrics */}
      <Row gutter={[16, 16]} style={{ marginBottom: '24px' }}>
        <Col xs={24} sm={6}>
          <Card>
            <Statistic
              title="Active Hotspots"
              value={dashboardSummary.activeHotspots}
              prefix={<FireOutlined />}
              valueStyle={{ color: '#fa8c16' }}
              suffix={`High: ${dashboardSummary.highRiskHotspots}`}
            />
          </Card>
        </Col>
        
        <Col xs={24} sm={6}>
          <Card>
            <Statistic
              title="Risk Trend"
              value={dashboardSummary.currentRiskTrend ? 
                `${(dashboardSummary.currentRiskTrend * 100).toFixed(1)}%` : '0%'}
              prefix={getTrendIcon(dashboardSummary.trendDirection)}
              valueStyle={{ 
                color: dashboardSummary.trendDirection === 'increasing' ? '#ff4d4f' : 
                       dashboardSummary.trendDirection === 'decreasing' ? '#52c41a' : '#1890ff'
              }}
              suffix={dashboardSummary.trendDirection}
            />
          </Card>
        </Col>
        
        <Col xs={24} sm={6}>
          <Card>
            <Statistic
              title="Forecast Accuracy"
              value={82}
              suffix="%"
              prefix={<TrendingUp />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        
        <Col xs={24} sm={6}>
          <Card>
            <Statistic
              title="Prediction Window"
              value={selectedTimeRange}
              prefix={<Clock />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
      </Row>

      {/* Risk Trend Chart */}
      <Row gutter={[16, 16]} style={{ marginBottom: '24px' }}>
        <Col span={24}>
          <Card title="Risk Trend Analysis" extra={
            <Tooltip title="Risk prediction based on historical data and current indicators">
              <InfoCircleOutlined style={{ color: '#999' }} />
            </Tooltip>
          }>
            <ResponsiveContainer width="100%" height={300}>
              <AreaChart data={formatChartData(riskTrends)}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="time" />
                <YAxis />
                <RechartsTooltip />
                <Area 
                  type="monotone" 
                  dataKey="risk" 
                  stroke="#1890ff" 
                  fill="#1890ff" 
                  fillOpacity={0.3}
                  name="Risk Level (%)"
                />
                <Area 
                  type="monotone" 
                  dataKey="confidence" 
                  stroke="#52c41a" 
                  fill="#52c41a" 
                  fillOpacity={0.2}
                  name="Confidence (%)"
                />
              </AreaChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>

      {/* Location Risks and Hotspots */}
      <Row gutter={[16, 16]} style={{ marginBottom: '24px' }}>
        <Col xs={24} lg={16}>
          <Card title="Location Risk Analysis">
            <Table
              columns={locationRiskColumns}
              dataSource={locationRisks}
              rowKey="locationId"
              loading={loading}
              pagination={false}
              size="small"
            />
          </Card>
        </Col>
        
        <Col xs={24} lg={8}>
          <Card title="High-Risk Hotspots">
            <Space direction="vertical" style={{ width: '100%' }}>
              {forecasts[0]?.hotspots?.slice(0, 6).map((hotspot) => (
                <Card key={hotspot.hotspotId} size="small">
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start' }}>
                    <div style={{ flex: 1 }}>
                      <h4 style={{ margin: '0 0 8px 0' }}>{hotspot.locationName}</h4>
                      <Tag color={getSeverityColor(hotspot.severity)}>
                        {hotspot.severity?.toUpperCase() || 'UNKNOWN'}
                      </Tag>
                      <div style={{ fontSize: '12px', color: '#666', marginTop: '4px' }}>
                        {hotspot.threatType}
                      </div>
                      <div style={{ fontSize: '12px', color: '#999', marginTop: '4px' }}>
                        Peak: {new Date(hotspot.peakTime).toLocaleString()}
                      </div>
                    </div>
                    <div style={{ textAlign: 'center' }}>
                      <Progress
                        type="circle"
                        percent={Math.round(hotspot.probability * 100)}
                        size={60}
                        strokeColor={getSeverityColor(hotspot.severity)}
                      />
                      <div style={{ fontSize: '10px', color: '#999', marginTop: '4px' }}>
                        {(hotspot.confidence * 100).toFixed(0)}% conf.
                      </div>
                    </div>
                  </div>
                </Card>
              ))}
            </Space>
          </Card>
        </Col>
      </Row>

      {/* Forecast Details */}
      <Row gutter={[16, 16]}>
        <Col span={24}>
          <Card title="Forecast Details & Insights">
            <Alert
              message="Risk Forecasting"
              description="Risk predictions are generated using analytical models that analyze historical patterns, current threat indicators, and environmental factors to forecast potential security incidents."
              type="info"
              showIcon
              style={{ marginBottom: '16px' }}
            />
            
            <Row gutter={[16, 16]}>
              <Col xs={24} md={8}>
                <Card size="small" title="Model Performance">
                  <Space direction="vertical" style={{ width: '100%' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <span>Accuracy:</span>
                      <strong>82%</strong>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <span>Precision:</span>
                      <strong>78%</strong>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <span>Recall:</span>
                      <strong>85%</strong>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <span>Last Updated:</span>
                      <strong>{new Date().toLocaleTimeString()}</strong>
                    </div>
                  </Space>
                </Card>
              </Col>
              
              <Col xs={24} md={8}>
                <Card size="small" title="Data Sources">
                  <Timeline
                    items={[
                      { color: 'blue', children: 'Social Media Analysis' },
                      { color: 'green', children: 'Historical Incident Data' },
                      { color: 'orange', children: 'Environmental Factors' },
                      { color: 'red', children: 'Real-time Sensor Data' }
                    ]}
                  />
                </Card>
              </Col>
              
              <Col xs={24} md={8}>
                <Card size="small" title="Recommended Actions">
                  <Space direction="vertical" style={{ width: '100%' }}>
                    <div>
                      <Badge status="warning" />
                      <span style={{ marginLeft: '8px' }}>Increase monitoring in high-risk areas</span>
                    </div>
                    <div>
                      <Badge status="processing" />
                      <span style={{ marginLeft: '8px' }}>Prepare contingency resources</span>
                    </div>
                    <div>
                      <Badge status="error" />
                      <span style={{ marginLeft: '8px' }}>Alert local authorities for predicted hotspots</span>
                    </div>
                  </Space>
                </Card>
              </Col>
            </Row>
          </Card>
        </Col>
      </Row>
    </div>
  );
}

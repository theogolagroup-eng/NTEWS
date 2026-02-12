'use client';

import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Statistic, Alert, Button, Spin, Typography, Space, Progress, Tag, Table, Badge } from 'antd';
import { 
  AlertTriangleOutlined, 
  RiseOutlined, 
  EnvironmentOutlined, 
  ActivityOutlined,
  SafetyOutlined,
  ClockCircleOutlined,
  UserOutlined
} from '@ant-design/icons';
import { API_ENDPOINTS, apiClient } from '@/services/api';

interface DashboardSummary {
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
}

interface AlertSummary {
  totalAlerts: number;
  activeAlerts: number;
  unacknowledgedAlerts: number;
  criticalAlerts: number;
  highAlerts: number;
  mediumAlerts: number;
  lowAlerts: number;
  recentAlerts: Array<{
    id: string;
    title: string;
    severity: string;
    status: string;
    timestamp: string;
    location: string;
  }>;
}

interface PredictionSummary {
  activeHotspots: number;
  highRiskHotspots: number;
  mediumRiskHotspots: number;
  lowRiskHotspots: number;
  currentRiskTrend: number;
  trendDirection: string;
  topHotspots: Array<{
    id: string;
    locationName: string;
    probability: number;
    severity: string;
    threatType: string;
  }>;
}

export default function Dashboard() {
  const [intelligenceData, setIntelligenceData] = useState<DashboardSummary | null>(null);
  const [alertData, setAlertData] = useState<AlertSummary | null>(null);
  const [predictionData, setPredictionData] = useState<PredictionSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchDashboardData();
    
    // Set up WebSocket for real-time updates
    const ws = new WebSocket('ws://localhost:8084/ws/alerts');
    ws.onmessage = (event) => {
      const data = JSON.parse(event.data);
      console.log('Real-time alert:', data);
      // Refresh dashboard data when new alert arrives
      fetchDashboardData();
    };
    
    return () => {
      ws.close();
    };
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      
      // Fetch all data through API Gateway
      const [intelData, alertData, predictionData] = await Promise.all([
        apiClient.get(API_ENDPOINTS.INTELLIGENCE.DASHBOARD),
        apiClient.get(API_ENDPOINTS.ALERTS.DASHBOARD),
        apiClient.get(API_ENDPOINTS.PREDICTIONS.DASHBOARD)
      ]);
      
      setIntelligenceData(intelData);
      setAlertData(alertData);
      setPredictionData(predictionData);
      
    } catch (err) {
      setError('Failed to fetch dashboard data');
      console.error('Dashboard fetch error:', err);
    } finally {
      setLoading(false);
    }
  };

  const getSeverityColor = (severity: string) => {
    switch (severity.toLowerCase()) {
      case 'critical': return 'bg-red-500';
      case 'high': return 'bg-orange-500';
      case 'medium': return 'bg-yellow-500';
      case 'low': return 'bg-green-500';
      default: return 'bg-gray-500';
    }
  };

  const getThreatLevelColor = (level: string) => {
    switch (level.toLowerCase()) {
      case 'critical': return 'text-red-600 bg-red-50';
      case 'high': return 'text-orange-600 bg-orange-50';
      case 'medium': return 'text-yellow-600 bg-yellow-50';
      case 'low': return 'text-green-600 bg-green-50';
      default: return 'text-gray-600 bg-gray-50';
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <Activity className="h-8 w-8 animate-spin mx-auto mb-4" />
          <p>Loading NTEWS Dashboard...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <Alert className="max-w-md">
          <AlertTriangle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">NTEWS Command Dashboard</h1>
          <p className="text-gray-600">National Threat Early Warning System - Real-time Intelligence</p>
        </div>

        {/* Main Metrics Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          {/* Total Threats */}
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Active Threats</CardTitle>
              <Shield className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{intelligenceData?.activeThreats || 0}</div>
              <p className="text-xs text-muted-foreground">
                Total reports: {intelligenceData?.totalReports || 0}
              </p>
            </CardContent>
          </Card>

          {/* Critical Alerts */}
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Critical Alerts</CardTitle>
              <AlertTriangle className="h-4 w-4 text-red-500" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-red-600">{alertData?.criticalAlerts || 0}</div>
              <p className="text-xs text-muted-foreground">
                Unacknowledged: {alertData?.unacknowledgedAlerts || 0}
              </p>
            </CardContent>
          </Card>

          {/* Active Hotspots */}
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Active Hotspots</CardTitle>
              <MapPin className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{predictionData?.activeHotspots || 0}</div>
              <p className="text-xs text-muted-foreground">
                High risk: {predictionData?.highRiskHotspots || 0}
              </p>
            </CardContent>
          </Card>

          {/* Risk Trend */}
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Risk Trend</CardTitle>
              <TrendingUp className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">
                {predictionData?.currentRiskTrend ? 
                  `${(predictionData.currentRiskTrend * 100).toFixed(1)}%` : '0%'
                }
              </div>
              <p className="text-xs text-muted-foreground">
                Direction: {predictionData?.trendDirection || 'stable'}
              </p>
            </CardContent>
          </Card>
        </div>

        {/* Threat Level Distribution */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
          <Card className="lg:col-span-1">
            <CardHeader>
              <CardTitle>Threat Level Distribution</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                <div className="flex justify-between items-center">
                  <span className="text-sm">Critical</span>
                  <Badge className="bg-red-500">{intelligenceData?.criticalThreats || 0}</Badge>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-sm">High</span>
                  <Badge className="bg-orange-500">{intelligenceData?.highThreats || 0}</Badge>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-sm">Medium</span>
                  <Badge className="bg-yellow-500">{intelligenceData?.mediumThreats || 0}</Badge>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-sm">Low</span>
                  <Badge className="bg-green-500">{intelligenceData?.lowThreats || 0}</Badge>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Recent Threats */}
          <Card className="lg:col-span-2">
            <CardHeader>
              <CardTitle>Recent Threat Intelligence</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {intelligenceData?.recentThreats?.slice(0, 5).map((threat) => (
                  <div key={threat.id} className="flex items-center justify-between p-3 border rounded-lg">
                    <div className="flex-1">
                      <p className="font-medium text-sm">{threat.title}</p>
                      <p className="text-xs text-gray-500">{threat.location}</p>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Badge className={getThreatLevelColor(threat.threatLevel)}>
                        {threat.threatLevel}
                      </Badge>
                      <div className="flex items-center text-xs text-gray-500">
                        <Clock className="h-3 w-3 mr-1" />
                        {new Date(threat.timestamp).toLocaleTimeString()}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Top Hotspots */}
        <Card>
          <CardHeader>
            <CardTitle>Top Threat Hotspots</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {predictionData?.topHotspots?.slice(0, 6).map((hotspot) => (
                <div key={hotspot.id} className="border rounded-lg p-4">
                  <div className="flex justify-between items-start mb-2">
                    <h4 className="font-medium">{hotspot.locationName}</h4>
                    <Badge className={getSeverityColor(hotspot.severity)}>
                      {hotspot.severity}
                    </Badge>
                  </div>
                  <p className="text-sm text-gray-600 mb-2">{hotspot.threatType}</p>
                  <div className="flex justify-between items-center">
                    <span className="text-sm">
                      Risk: {(hotspot.probability * 100).toFixed(1)}%
                    </span>
                    <MapPin className="h-4 w-4 text-gray-400" />
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* Refresh Button */}
        <div className="mt-8 text-center">
          <Button onClick={fetchDashboardData} className="mr-4">
            <Activity className="h-4 w-4 mr-2" />
            Refresh Data
          </Button>
          <Button variant="outline" onClick={() => window.location.reload()}>
            Reload Dashboard
          </Button>
        </div>
      </div>
    </div>
  );
}

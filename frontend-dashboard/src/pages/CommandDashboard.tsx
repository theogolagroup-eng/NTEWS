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

  Spin,

  Tabs

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
import BlueskyTweetsPanel from '@/components/BlueskyTweetsPanel';



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

  }, []);



  // Add manual refresh capability

  const handleManualRefresh = () => {

    fetchDashboardData();

  };



  const fetchDashboardData = async () => {

    try {

      setLoading(true);

      setError(null);

      // Fetch all dashboard data in parallel through API Gateway with timeout
      const timeoutPromise = new Promise((_, reject) => 
        setTimeout(() => reject(new Error('Request timeout')), 10000)
      );
      
      // Fetch data from API endpoints
      const [intelData, alertData, predictionData, allAlerts] = await Promise.all([
        Promise.race([apiClient.get(API_ENDPOINTS.INTELLIGENCE.DASHBOARD), timeoutPromise]),
        Promise.race([apiClient.get(API_ENDPOINTS.ALERTS.DASHBOARD), timeoutPromise]),
        Promise.race([apiClient.get(API_ENDPOINTS.PREDICTIONS.DASHBOARD), timeoutPromise]),
        Promise.race([apiClient.get(API_ENDPOINTS.ALERTS.ALL), timeoutPromise])
        // Temporarily commented out - hotspots endpoint not available yet
        // Promise.race([apiClient.get(API_ENDPOINTS.PREDICTIONS.HOTSPOTS), timeoutPromise])
      ]);
      
      // Mock hotspots data temporarily until endpoint is available
      const hotspotsData = [
        {
          id: "hotspot-001",
          locationName: "Nairobi Central Business District",
          location: "Nairobi, Kenya",
          latitude: -1.2921,
          longitude: 36.8219,
          severity: "high",
          riskLevel: "high",
          probability: 0.92,
          confidence: 0.92,
          threatType: "Terrorism Threat",
          type: "terrorism",
          radius: 2000
        },
        {
          id: "hotspot-002",
          locationName: "Mombasa Port Area",
          location: "Mombasa, Kenya",
          latitude: -4.0435,
          longitude: 39.6682,
          severity: "medium",
          riskLevel: "medium",
          probability: 0.75,
          confidence: 0.75,
          threatType: "Cyber Attack",
          type: "cyber",
          radius: 1500
        },
        {
          id: "hotspot-003",
          locationName: "Kisumu City Center",
          location: "Kisumu, Kenya",
          latitude: -0.1022,
          longitude: 34.7617,
          severity: "medium",
          riskLevel: "medium",
          probability: 0.68,
          confidence: 0.68,
          threatType: "Civil Unrest",
          type: "civil_unrest",
          radius: 1800
        },
        {
          id: "hotspot-004",
          locationName: "Eldoret Town",
          location: "Eldoret, Kenya",
          latitude: 0.5143,
          longitude: 35.2698,
          severity: "low",
          riskLevel: "low",
          probability: 0.45,
          confidence: 0.45,
          threatType: "Organized Crime",
          type: "organized_crime",
          radius: 1200
        },
        {
          id: "hotspot-005",
          locationName: "Garissa County",
          location: "Garissa, Kenya",
          latitude: -0.4528,
          longitude: 39.6460,
          severity: "high",
          riskLevel: "high",
          probability: 0.88,
          confidence: 0.88,
          threatType: "Border Security",
          type: "border_security",
          radius: 2500
        }
      ];

      // Process alerts data similar to AlertsPage - using the same successful pattern
      const alertsList = allAlerts?.content || [];
      const activeAlerts = alertsList.filter((alert: Alert) => alert.status === 'active');
      const criticalAlerts = activeAlerts.filter((alert: Alert) => alert.severity === 'critical');
      const highAlerts = activeAlerts.filter((alert: Alert) => alert.severity === 'high');
      const mediumAlerts = activeAlerts.filter((alert: Alert) => alert.severity === 'medium');
      const lowAlerts = activeAlerts.filter((alert: Alert) => alert.severity === 'low');
      
      // Create recent alerts list with proper field mapping
      const recentAlerts = alertsList.slice(0, 5).map((alert: Alert) => ({
        id: alert.id,
        title: alert.title,
        severity: alert.severity,
        priority: alert.priority,
        status: alert.status,
        timestamp: alert.createdAt || alert.timestamp,
        location: alert.location?.address || `${alert.location?.city || 'Unknown'}, ${alert.location?.country || 'Kenya'}`
      }));

      setDashboardData({
        intelligenceSummary: {
          totalReports: intelData?.totalReports || 0,
          activeThreats: intelData?.activeThreats || 0,
          criticalThreats: intelData?.criticalThreats || 0,
          highThreats: intelData?.highThreats || 0,
          mediumThreats: intelData?.mediumThreats || 0,
          lowThreats: intelData?.lowThreats || 0,
          categoryCounts: intelData?.categoryCounts || [],
          recentThreats: intelData?.recentThreats || []
        },
        alertSummary: {
          totalAlerts: alertData?.totalAlerts || alertsList.length,
          activeAlerts: alertData?.activeAlerts || activeAlerts.length,
          unacknowledgedAlerts: alertData?.unacknowledgedAlerts || activeAlerts.filter((alert: Alert) => alert.status === 'active').length,
          criticalAlerts: alertData?.criticalAlerts || criticalAlerts.length,
          highAlerts: alertData?.highAlerts || highAlerts.length,
          mediumAlerts: alertData?.mediumAlerts || mediumAlerts.length,
          lowAlerts: alertData?.lowAlerts || lowAlerts.length,
          severityCounts: alertData?.severityCounts && Object.keys(alertData.severityCounts).length > 0 
            ? alertData.severityCounts 
            : [
                { severity: 'critical', count: alertData?.criticalAlerts || 0 },
                { severity: 'high', count: alertData?.highAlerts || 0 },
                { severity: 'medium', count: alertData?.mediumAlerts || 0 },
                { severity: 'low', count: alertData?.lowAlerts || 0 }
              ],
          recentAlerts: recentAlerts
        },
        predictionSummary: {
          activeHotspots: hotspotsData?.length || predictionData?.highRiskAreas || 0,
          highRiskHotspots: hotspotsData?.filter((h: any) => h.severity === 'high' || h.riskLevel === 'high')?.length || predictionData?.highRiskAreas || 0,
          mediumRiskHotspots: hotspotsData?.filter((h: any) => h.severity === 'medium' || h.riskLevel === 'medium')?.length || 0,
          lowRiskHotspots: hotspotsData?.filter((h: any) => h.severity === 'low' || h.riskLevel === 'low')?.length || 0,
          currentRiskTrend: predictionData?.averageConfidence || 0.0,
          trendDirection: predictionData?.averageConfidence > 0.7 ? 'increasing' : 'stable',
          topHotspots: hotspotsData?.slice(0, 4).map((hotspot: any, index: number) => ({
            id: hotspot.id || `hotspot-${index}`,
            locationName: hotspot.locationName || hotspot.location || hotspot.area || `Hotspot ${index + 1}`,
            severity: hotspot.severity || hotspot.riskLevel || 'medium',
            probability: hotspot.probability || hotspot.confidence || predictionData?.averageConfidence || 0.5,
            threatType: hotspot.threatType || hotspot.type || 'Unknown',
            latitude: hotspot.latitude || hotspot.coordinates?.lat,
            longitude: hotspot.longitude || hotspot.coordinates?.lng,
            radius: hotspot.radius || 1500
          })) || intelData?.recentThreats?.slice(0, 4).map((threat: any, index: number) => ({
            id: threat.id,
            locationName: threat.location || `Location ${index + 1}`,
            severity: threat.threatLevel,
            probability: predictionData?.averageConfidence || 0.5,
            threatType: threat.title?.split(' - ')[1] || 'Unknown'
          })) || [],
          recentTrends: []
        }
      });

    } catch (err) {
      setError('Failed to fetch dashboard data');
      console.error('Dashboard fetch error:', err);
      // Set default data to prevent UI crashes
      setDashboardData({
        intelligenceSummary: { totalReports: 0, activeThreats: 0, criticalThreats: 0, highThreats: 0, mediumThreats: 0, lowThreats: 0, categoryCounts: [], recentThreats: [] },
        alertSummary: { totalAlerts: 0, activeAlerts: 0, unacknowledgedAlerts: 0, criticalAlerts: 0, highAlerts: 0, mediumAlerts: 0, lowAlerts: 0, severityCounts: [
            { severity: 'critical', count: 0 },
            { severity: 'high', count: 0 },
            { severity: 'medium', count: 0 },
            { severity: 'low', count: 0 }
          ], recentAlerts: [] },
        predictionSummary: { activeHotspots: 0, highRiskHotspots: 0, mediumRiskHotspots: 0, lowRiskHotspots: 0, currentRiskTrend: 0.0, trendDirection: 'stable', topHotspots: [], recentTrends: [] }
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

  const getStatusColor = (status: string) => {
    const colors = {
      active: 'red',
      acknowledged: 'orange',
      investigating: 'blue',
      resolved: 'green',
      closed: 'default',
      false_positive: 'purple'
    };
    return colors[status as keyof typeof colors] || 'default';
  };

  const getTrendIcon = (trend: string) => {
    switch (trend) {
      case 'increasing': return <ExclamationCircleOutlined style={{ color: themeStyles.errorColor }} />;
      case 'decreasing': return <SafetyOutlined style={{ color: themeStyles.successColor }} />;
      default: return <ClockCircleOutlined style={{ color: themeStyles.infoColor }} />;
    }
  };

  if (loading) {

    return (

      <div style={{ padding: '24px', textAlign: 'center', background: themeStyles.background, minHeight: '100vh' }}>

        <LoadingOutlined style={{ fontSize: '48px', color: themeStyles.infoColor }} />

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

              fontWeight: 'bold', 

              color: themeStyles.textColor, 

              textShadow: themeStyles.textShadow,
              background: `linear-gradient(135deg, ${themeStyles.kenyanRed}, ${themeStyles.kenyanGreen})`,

              WebkitBackgroundClip: 'text',

              WebkitTextFillColor: 'transparent',

              backgroundClip: 'text'

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

              backgroundColor: themeStyles.successColor,

              boxShadow: `0 0 8px ${themeStyles.successColor}`

            }} />

            <span style={{ fontSize: '12px', color: themeStyles.successColor, fontWeight: '600', textShadow: themeStyles.textShadow }}>

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

            styles={{ body: { padding: '16px' } }}

          >

            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>

              <div>

                <div style={{ fontSize: '24px', fontWeight: '700', color: themeStyles.infoColor, textShadow: themeStyles.textShadow }}>

                  {intelligenceSummary.activeThreats}

                </div>

                <div style={{ fontSize: '14px', color: themeStyles.textColor, textTransform: 'uppercase', fontWeight: 'bold' }}>

                  Active Threats

                </div>

                <div style={{ fontSize: '12px', color: themeStyles.infoColor, fontWeight: '600' }}>

                  Total: {intelligenceSummary.totalReports}

                </div>

              </div>

              <SafetyOutlined style={{ fontSize: '20px', color: themeStyles.infoColor }} />

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

            styles={{ body: { padding: '16px' } }}

          >

            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>

              <div>

                <div style={{ fontSize: '24px', fontWeight: '700', color: themeStyles.errorColor, textShadow: themeStyles.textShadow }}>

                  {alertSummary.criticalAlerts}

                </div>

                <div style={{ fontSize: '14px', color: themeStyles.textColor, textTransform: 'uppercase', fontWeight: 'bold' }}>

                  Critical Alerts

                </div>

                <div style={{ fontSize: '12px', color: themeStyles.warningColor, fontWeight: '600' }}>

                  Unack: {alertSummary.unacknowledgedAlerts}

                </div>

              </div>

              <ExclamationCircleOutlined style={{ fontSize: '20px', color: themeStyles.errorColor }} />

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

            styles={{ body: { padding: '16px' } }}

          >

            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>

              <div>

                <div style={{ fontSize: '24px', fontWeight: '700', color: themeStyles.warningColor, textShadow: themeStyles.textShadow }}>

                  {predictionSummary.activeHotspots}

                </div>

                <div style={{ fontSize: '14px', color: themeStyles.textColor, textTransform: 'uppercase', fontWeight: 'bold' }}>

                  Active Hotspots

                </div>

                <div style={{ fontSize: '12px', color: themeStyles.errorColor, fontWeight: '600' }}>

                  High Risk: {predictionSummary.highRiskHotspots}

                </div>

              </div>

              <FireOutlined style={{ fontSize: '20px', color: themeStyles.warningColor }} />

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

            styles={{ body: { padding: '16px' } }}

          >

            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>

              <div>

                <div style={{ fontSize: '24px', fontWeight: '700', color: themeStyles.successColor, textShadow: themeStyles.textShadow }}>

                  {alertSummary.activeAlerts}

                </div>

                <div style={{ fontSize: '14px', color: themeStyles.textColor, textTransform: 'uppercase', fontWeight: 'bold' }}>

                  Active Alerts

                </div>

                <div style={{ fontSize: '12px', color: themeStyles.successColor, fontWeight: '600' }}>

                  System Active

                </div>

              </div>

              <BellOutlined style={{ fontSize: '20px', color: themeStyles.successColor }} />

            </div>

          </Card>

        </Col>

      </Row>



      {/* Main Content Area */}

      <Row gutter={[12, 12]}>

        {/* Left Column - Alerts & Hotspots */}

        <Col xs={24} lg={16}>

          {/* Live Alerts and Tweets */}

          <Card 

            style={{ 

              background: themeStyles.cardBackground,

              border: themeStyles.cardBorder,

              borderRadius: '8px',

              marginBottom: '12px'

            }}

            styles={{ body: { padding: '8px' } }}

          >

            <Tabs

              defaultActiveKey="alerts"

              size="small"

              style={{ 

                color: themeStyles.textColor,

              }}

              items={ [

                {

                  key: 'alerts',

                  label: (

                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>

                      <BellOutlined style={{ color: themeStyles.successColor }} />

                      <span style={{ color: themeStyles.textColor }}>LIVE ALERTS</span>

                      <Badge count={alertSummary.activeAlerts} style={{ backgroundColor: themeStyles.successColor }} />

                    </div>

                  ),

                  children: (

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

                            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '4px' }}>

                              <span style={{ fontSize: '11px', color: themeStyles.secondaryTextColor, textShadow: themeStyles.textShadow }}>

                                {new Date(item.createdAt || item.timestamp).toLocaleTimeString()}

                              </span>

                              {item.status && (

                                <Tag 

                                  color={getStatusColor(item.status)}

                                  style={{ fontSize: '9px', padding: '1px 4px', border: 'none' }}

                                >

                                  {item.status?.replace('_', ' ').toUpperCase()}

                                </Tag>

                              )}

                            </div>

                          </div>

                        </List.Item>

                      )}

                    />

                  ),

                },


                {
                  key: 'tweets',
                  label: (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <span style={{ 
                        color: '#1DA1F2',
                        fontSize: '16px',
                        marginRight: '8px',
                        display: 'inline-block',
                        verticalAlign: 'middle'
                      }}>𝕏</span>
                      <span style={{ color: themeStyles.textColor }}>TWEETS</span>
                    </div>
                  ),
                  children: <BlueskyTweetsPanel isDarkMode={isDarkMode} themeStyles={themeStyles} />
                }
              ]}

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

                <FireOutlined style={{ marginRight: '8px', color: themeStyles.warningColor }} />

                THREAT HOTSPOTS

                <Badge count={predictionSummary.activeHotspots} style={{ marginLeft: '8px', backgroundColor: themeStyles.warningColor }} />

              </div>

            }

            styles={{ body: { padding: '12px' } }}

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

                      <span style={{ fontSize: '11px', color: themeStyles.successColor, fontWeight: '600', textShadow: themeStyles.textShadow }}>

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

                <EnvironmentOutlined style={{ marginRight: '8px', color: themeStyles.infoColor }} />

                THREAT MAP

              </div>

            }

            styles={{ body: { padding: '12px', height: '300px' } }}

          >

            <div style={{
              height: '100%',
              position: 'relative'
            }}>
              <ThreatMap 
                threats={dashboardData?.alertSummary?.recentAlerts?.map((alert: any) => {
                  // Extract coordinates from location object with multiple fallbacks
                  let lat = -1.2921; // Default Nairobi
                  let lng = 36.8219;
                  
                  if (alert.location?.latitude) {
                    lat = parseFloat(alert.location.latitude);
                  } else if (alert.location?.coordinates?.lat) {
                    lat = parseFloat(alert.location.coordinates.lat);
                  }
                  
                  if (alert.location?.longitude) {
                    lng = parseFloat(alert.location.longitude);
                  } else if (alert.location?.coordinates?.lng) {
                    lng = parseFloat(alert.location.coordinates.lng);
                  }
                  
                  return {
                    id: alert.id,
                    latitude: lat,
                    longitude: lng,
                    title: alert.title || 'Unknown Threat',
                    threatLevel: alert.severity || 'medium',
                    threatScore: alert.threatScore || 0.5,
                    timestamp: alert.createdAt || alert.timestamp || new Date().toISOString(),
                    location: alert.location?.description || alert.location?.address || `${alert.location?.city || 'Unknown'}, ${alert.location?.country || 'Kenya'}`,
                    category: alert.category || 'unknown'
                  };
                }) || []}
                hotspots={dashboardData?.predictionSummary?.topHotspots?.map((hotspot: any) => {
                  // Extract coordinates with fallbacks
                  let lat = -1.2921;
                  let lng = 36.8219;
                  
                  if (hotspot.latitude) {
                    lat = parseFloat(hotspot.latitude);
                  } else if (hotspot.coordinates?.lat) {
                    lat = parseFloat(hotspot.coordinates.lat);
                  }
                  
                  if (hotspot.longitude) {
                    lng = parseFloat(hotspot.longitude);
                  } else if (hotspot.coordinates?.lng) {
                    lng = parseFloat(hotspot.coordinates.lng);
                  }
                  
                  return {
                    id: hotspot.id,
                    latitude: lat,
                    longitude: lng,
                    locationName: hotspot.locationName || hotspot.location || 'Unknown Area',
                    probability: hotspot.probability || 0.5,
                    severity: hotspot.severity || 'medium',
                    threatType: hotspot.threatType || 'unknown',
                    radius: hotspot.radius || 1500
                  };
                }) || []}
                onThreatClick={(threat) => {
                  console.log('Threat clicked:', threat);
                  // Navigate to threat details or show modal
                }}
                onHotspotClick={(hotspot) => {
                  console.log('Hotspot clicked:', hotspot);
                  // Navigate to hotspot details or show modal
                }}
              />
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

                <RiseOutlined style={{ marginRight: '8px', color: themeStyles.kenyanRed }} />

                THREAT LEVELS

              </div>

            }

            styles={{ body: { padding: '12px' } }}

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

                <BellOutlined style={{ marginRight: '8px', color: themeStyles.successColor }} />

                ACTION POINTS

              </div>

            }

            styles={{ body: { padding: '12px' } }}

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


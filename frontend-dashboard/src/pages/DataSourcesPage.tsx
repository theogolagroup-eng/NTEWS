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
  Button,
  Switch,
  Table,
  Tooltip
} from 'antd';
import {
  DatabaseOutlined,
  CloudSyncOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined,
  CloseCircleOutlined,
  ReloadOutlined,
  ApiOutlined,
  SafetyOutlined,
  FireOutlined,
  BellOutlined
} from '@ant-design/icons';
import { API_ENDPOINTS, apiClient } from '@/services/api';
import { useTheme } from '@/contexts/ThemeContext';

// Theme styles
const getThemeStyles = (isDarkMode: boolean) => ({
  background: isDarkMode 
    ? 'linear-gradient(135deg, #0f1419 0%, #1a1f2e 100%)' 
    : 'linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%)',
  textColor: isDarkMode ? '#ffffff' : '#1e293b',
  secondaryTextColor: isDarkMode ? '#94a3b8' : '#64748b',
  cardBackground: isDarkMode 
    ? 'rgba(255,255,255,0.04)' 
    : 'rgba(255,255,255,0.95)',
  cardBorder: isDarkMode 
    ? '1px solid rgba(255,255,255,0.08)' 
    : '1px solid rgba(0,0,0,0.08)',
  headerBackground: isDarkMode 
    ? 'rgba(255,255,255,0.06)' 
    : 'rgba(255,255,255,0.98)',
  textShadow: isDarkMode 
    ? '0 1px 2px rgba(0,0,0,0.4)' 
    : '0 1px 2px rgba(255,255,255,0.8)',
  successColor: '#52c41a',
  warningColor: '#fa8c16',
  errorColor: '#ff4d4f',
  infoColor: '#1890ff'
});

interface DataSource {
  name: string;
  type: 'intelligence' | 'alerts' | 'predictions' | 'ingestion';
  status: 'healthy' | 'degraded' | 'down';
  endpoint: string;
  lastChecked: string;
  responseTime: number;
  errorRate: number;
  uptime: number;
}

interface ServiceHealth {
  service: string;
  status: 'healthy' | 'degraded' | 'down';
  message: string;
  lastCheck: string;
  metrics: {
    responseTime: number;
    successRate: number;
    errorCount: number;
    totalRequests: number;
  };
}

// Internal component (not exported)
function DataSourcesPage() {
  const { isDarkMode, themeStyles } = useTheme();
  const [loading, setLoading] = useState(true);
  const [dataSources, setDataSources] = useState<DataSource[]>([]);
  const [serviceHealth, setServiceHealth] = useState<ServiceHealth[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    checkAllServices();
    // Auto-refresh disabled to prevent UI interference
    // const interval = setInterval(checkAllServices, 30000); // Check every 30 seconds
    // return () => clearInterval(interval);
  }, []);

  const checkAllServices = async () => {
    try {
      setLoading(true);
      setError(null);

      const services = [
        { name: 'Intelligence Service', type: 'intelligence' as const, endpoint: API_ENDPOINTS.INTELLIGENCE.DASHBOARD },
        { name: 'Alert Service', type: 'alerts' as const, endpoint: API_ENDPOINTS.ALERTS.DASHBOARD },
        { name: 'Prediction Service', type: 'predictions' as const, endpoint: API_ENDPOINTS.PREDICTIONS.DASHBOARD },
        { name: 'All Alerts', type: 'alerts' as const, endpoint: API_ENDPOINTS.ALERTS.ALL },
      ];

      const healthChecks = await Promise.allSettled(
        services.map(async (service) => {
          const startTime = Date.now();
          try {
            await apiClient.get(service.endpoint);
            const responseTime = Date.now() - startTime;
            return {
              ...service,
              status: 'healthy' as const,
              lastChecked: new Date().toISOString(),
              responseTime,
              errorRate: 0,
              uptime: 100
            };
          } catch (err) {
            const responseTime = Date.now() - startTime;
            return {
              ...service,
              status: 'down' as const,
              lastChecked: new Date().toISOString(),
              responseTime,
              errorRate: 100,
              uptime: 0
            };
          }
        })
      );

      const sources = healthChecks.map(result => 
        result.status === 'fulfilled' ? result.value : null
      ).filter(Boolean) as DataSource[];

      setDataSources(sources);
      
      const healthData: ServiceHealth[] = sources.map(source => ({
        service: source.name,
        status: source.status,
        message: source.status === 'healthy' 
          ? 'Service is operating normally' 
          : source.status === 'degraded' 
          ? 'Service is experiencing issues' 
          : 'Service is unavailable',
        lastCheck: source.lastChecked,
        metrics: {
          responseTime: source.responseTime,
          successRate: 100 - source.errorRate,
          errorCount: source.errorRate > 0 ? 1 : 0,
          totalRequests: 1
        }
      }));

      setServiceHealth(healthData);

    } catch (err) {
      setError('Failed to check service health');
      console.error('Health check error:', err);
    } finally {
      setLoading(false);
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'healthy': return <CheckCircleOutlined style={{ color: themeStyles.successColor }} />;
      case 'degraded': return <ExclamationCircleOutlined style={{ color: themeStyles.warningColor }} />;
      case 'down': return <CloseCircleOutlined style={{ color: themeStyles.errorColor }} />;
      default: return <DatabaseOutlined style={{ color: themeStyles.secondaryTextColor }} />;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'healthy': return themeStyles.successColor;
      case 'degraded': return themeStyles.warningColor;
      case 'down': return themeStyles.errorColor;
      default: return themeStyles.secondaryTextColor;
    }
  };

  const getServiceIcon = (type: string) => {
    switch (type) {
      case 'intelligence': return <SafetyOutlined />;
      case 'alerts': return <BellOutlined />;
      case 'predictions': return <FireOutlined />;
      case 'ingestion': return <CloudSyncOutlined />;
      default: return <DatabaseOutlined />;
    }
  };

  const columns = [
    {
      title: 'Service',
      dataIndex: 'service',
      key: 'service',
      render: (text: string, record: ServiceHealth) => (
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          {getStatusIcon(record.status)}
          <span style={{ color: themeStyles.textColor, fontWeight: '600' }}>{text}</span>
        </div>
      ),
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Tag color={getStatusColor(status)} style={{ textTransform: 'capitalize' }}>
          {status}
        </Tag>
      ),
    },
    {
      title: 'Response Time',
      dataIndex: ['metrics', 'responseTime'],
      key: 'responseTime',
      render: (time: number) => (
        <span style={{ color: themeStyles.textColor }}>
          {time}ms
        </span>
      ),
    },
    {
      title: 'Success Rate',
      dataIndex: ['metrics', 'successRate'],
      key: 'successRate',
      render: (rate: number) => (
        <Progress 
          percent={rate} 
          size="small" 
          strokeColor={rate >= 90 ? themeStyles.successColor : rate >= 70 ? themeStyles.warningColor : themeStyles.errorColor}
          style={{ width: '80px' }}
        />
      ),
    },
    {
      title: 'Last Check',
      dataIndex: 'lastCheck',
      key: 'lastCheck',
      render: (time: string) => (
        <span style={{ color: themeStyles.secondaryTextColor, fontSize: '12px' }}>
          {new Date(time).toLocaleTimeString()}
        </span>
      ),
    },
  ];

  if (loading) {
    return (
      <div style={{ padding: '24px', textAlign: 'center', background: themeStyles.background, minHeight: '100vh' }}>
        <Spin size="large" />
        <h2 style={{ color: themeStyles.textColor, marginTop: '16px' }}>Checking Data Sources...</h2>
      </div>
    );
  }

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
            }}>
              NTEWS DATA SOURCES
            </h1>
            <p style={{ 
              color: themeStyles.secondaryTextColor, 
              marginTop: '4px',
              fontSize: '12px',
              fontWeight: '500'
            }}>
              Service Health Monitoring • Real-time Data Sources Status
            </p>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <Button
              type="primary"
              icon={<ReloadOutlined />}
              onClick={checkAllServices}
              loading={loading}
            />
          </div>
        </div>
      </div>

      {error && (
        <Alert
          message="Error"
          description={error}
          type="error"
          showIcon
          closable
          style={{ marginBottom: '20px' }}
        />
      )}

      {/* Summary Cards */}
      <Row gutter={[16, 16]} style={{ marginBottom: '20px' }}>
        <Col xs={24} sm={12} lg={6}>
          <Card style={{ background: themeStyles.cardBackground, border: themeStyles.cardBorder }}>
            <Statistic
              title="Total Services"
              value={dataSources.length}
              prefix={<DatabaseOutlined />}
              valueStyle={{ color: themeStyles.infoColor }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card style={{ background: themeStyles.cardBackground, border: themeStyles.cardBorder }}>
            <Statistic
              title="Healthy Services"
              value={dataSources.filter(s => s.status === 'healthy').length}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: themeStyles.successColor }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card style={{ background: themeStyles.cardBackground, border: themeStyles.cardBorder }}>
            <Statistic
              title="Degraded Services"
              value={dataSources.filter(s => s.status === 'degraded').length}
              prefix={<ExclamationCircleOutlined />}
              valueStyle={{ color: themeStyles.warningColor }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card style={{ background: themeStyles.cardBackground, border: themeStyles.cardBorder }}>
            <Statistic
              title="Down Services"
              value={dataSources.filter(s => s.status === 'down').length}
              prefix={<CloseCircleOutlined />}
              valueStyle={{ color: themeStyles.errorColor }}
            />
          </Card>
        </Col>
      </Row>

      {/* Service Health Table */}
      <Card 
        style={{ 
          background: themeStyles.cardBackground,
          border: themeStyles.cardBorder,
          borderRadius: '8px'
        }}
        title={
          <div style={{ color: themeStyles.textColor, fontSize: '16px', fontWeight: '600' }}>
            <ApiOutlined style={{ marginRight: '8px', color: themeStyles.infoColor }} />
            Service Health Status
          </div>
        }
      >
        <Table
          columns={columns}
          dataSource={serviceHealth}
          rowKey="service"
          pagination={false}
          style={{ background: 'transparent' }}
        />
      </Card>
    </div>
  );
}

// Export the wrapped component with ThemeProvider
export default function DataSourcesPageWithTheme() {
  return (
    <React.Fragment>
      <DataSourcesPage />
    </React.Fragment>
  );
}

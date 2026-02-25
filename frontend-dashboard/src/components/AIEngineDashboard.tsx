import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Statistic, Progress, Tag, List, Button, Space, Tooltip, Badge } from 'antd';
import { 
  RobotOutlined, 
  ThunderboltOutlined, 
  DatabaseOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined,
  InfoCircleOutlined,
  ReloadOutlined
} from '@ant-design/icons';
import { apiClient } from '../services/api';

interface AIEngineStats {
  engine_status: string;
  models_loaded: number;
  historical_data_points: number;
  model_performance: Record<string, any>;
  last_training?: string;
  capabilities: Record<string, any>;
  prediction_accuracy: Record<string, number>;
  system_health: Record<string, any>;
}

interface ModelInfo {
  [key: string]: {
    type: string;
    status: string;
    performance: Record<string, any>;
    last_updated?: string;
  };
}

const AIEngineDashboard: React.FC = () => {
  const [stats, setStats] = useState<AIEngineStats | null>(null);
  const [models, setModels] = useState<ModelInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchAIEngineData = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const [statsResponse, modelsResponse] = await Promise.all([
        apiClient.get('/api/ai-engine/stats'),
        apiClient.get('/api/ai-engine/models')
      ]);

      setStats(statsResponse.data);
      setModels(modelsResponse.data);
    } catch (err) {
      setError('Failed to fetch AI Engine data');
      console.error('AI Engine fetch error:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAIEngineData();
    
    // Auto-refresh disabled to prevent UI interference
    // const interval = setInterval(fetchAIEngineData, 30000);
    // return () => clearInterval(interval);
  }, []);

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'operational':
      case 'loaded':
      case 'active':
        return '#52c41a';
      case 'warning':
      case 'not_implemented':
        return '#faad14';
      case 'error':
      case 'critical':
        return '#ff4d4f';
      default:
        return '#d9d9d9';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'operational':
      case 'loaded':
      case 'active':
        return <CheckCircleOutlined style={{ color: '#52c41a' }} />;
      case 'warning':
      case 'not_implemented':
        return <ExclamationCircleOutlined style={{ color: '#faad14' }} />;
      case 'error':
      case 'critical':
        return <ExclamationCircleOutlined style={{ color: '#ff4d4f' }} />;
      default:
        return <InfoCircleOutlined style={{ color: '#d9d9d9' }} />;
    }
  };

  if (loading && !stats) {
    return <Card loading={true}>Loading AI Engine Status...</Card>;
  }

  if (error) {
    return (
      <Card>
        <div style={{ textAlign: 'center', padding: '20px' }}>
          <ExclamationCircleOutlined style={{ fontSize: '48px', color: '#ff4d4f', marginBottom: '16px' }} />
          <div>{error}</div>
          <Button type="primary" onClick={fetchAIEngineData} style={{ marginTop: 16 }}>
            Retry
          </Button>
        </div>
      </Card>
    );
  }

  return (
    <div>
      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col span={24}>
          <Card
            title={
              <Space>
                <RobotOutlined />
                <span>AI Engine Status</span>
                <Badge 
                  status={stats?.engine_status === 'operational' ? 'success' : 'error'} 
                  text={stats?.engine_status?.toUpperCase() || 'UNKNOWN'}
                />
                <Button 
                  icon={<ReloadOutlined />} 
                  size="small" 
                  onClick={fetchAIEngineData}
                  loading={loading}
                >
                  Refresh
                </Button>
              </Space>
            }
          >
            <Row gutter={[16, 16]}>
              <Col xs={24} sm={12} md={6}>
                <Statistic
                  title="Models Loaded"
                  value={stats?.models_loaded || 0}
                  prefix={<ThunderboltOutlined />}
                  valueStyle={{ color: '#1890ff' }}
                />
              </Col>
              <Col xs={24} sm={12} md={6}>
                <Statistic
                  title="Data Points"
                  value={stats?.historical_data_points || 0}
                  prefix={<DatabaseOutlined />}
                  valueStyle={{ color: '#52c41a' }}
                  formatter={(value) => `${Number(value).toLocaleString()}`}
                />
              </Col>
              <Col xs={24} sm={12} md={6}>
                <Statistic
                  title="Response Time"
                  value={stats?.system_health?.response_time_ms || 0}
                  suffix="ms"
                  valueStyle={{ 
                    color: (stats?.system_health?.response_time_ms || 0) < 200 ? '#52c41a' : '#faad14' 
                  }}
                />
              </Col>
              <Col xs={24} sm={12} md={6}>
                <Statistic
                  title="Error Rate"
                  value={((stats?.system_health?.error_rate || 0) * 100).toFixed(1)}
                  suffix="%"
                  valueStyle={{ 
                    color: (stats?.system_health?.error_rate || 0) < 0.05 ? '#52c41a' : '#ff4d4f' 
                  }}
                />
              </Col>
            </Row>
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card title="Model Performance" size="small">
            {stats?.model_performance && Object.entries(stats.model_performance).map(([model, perf]) => (
              <div key={model} style={{ marginBottom: 16 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                  <span style={{ fontWeight: 'bold' }}>{model.replace('_', ' ').toUpperCase()}</span>
                  <span>{Math.round((perf.accuracy || 0) * 100)}%</span>
                </div>
                <Progress
                  percent={Math.round((perf.accuracy || 0) * 100)}
                  status="active"
                  strokeColor={perf.accuracy > 0.8 ? '#52c41a' : perf.accuracy > 0.6 ? '#faad14' : '#ff4d4f'}
                  size="small"
                />
              </div>
            ))}
          </Card>
        </Col>

        <Col xs={24} lg={12}>
          <Card title="AI Capabilities" size="small">
            {stats?.capabilities && Object.entries(stats.capabilities).map(([capability, info]) => (
              <div key={capability} style={{ marginBottom: 12 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ fontWeight: 'bold', textTransform: 'capitalize' }}>
                    {capability.replace('_', ' ')}
                  </span>
                  <Tag color={info ? '#52c41a' : '#ff4d4f'}>
                    {info ? 'Active' : 'Inactive'}
                  </Tag>
                </div>
                {typeof info === 'object' && info.description && (
                  <div style={{ fontSize: '12px', color: '#666', marginTop: 2 }}>
                    {info.description}
                  </div>
                )}
              </div>
            ))}
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col span={24}>
          <Card title="Loaded Models" size="small">
            {models && (
              <List
                dataSource={Object.entries(models)}
                renderItem={(item: any) => {
                  const [modelName, modelInfo] = item;
                  return (
                    <List.Item>
                      <List.Item.Meta
                        avatar={getStatusIcon(modelInfo.status)}
                        title={
                          <Space>
                            <span>{modelName.replace('_', ' ').toUpperCase()}</span>
                            <Tag color={getStatusColor(modelInfo.status)}>
                              {modelInfo.status}
                            </Tag>
                          </Space>
                        }
                        description={
                          <div>
                            <div>Type: {modelInfo.type}</div>
                            {modelInfo.performance.accuracy && (
                              <div>Accuracy: {Math.round(modelInfo.performance.accuracy * 100)}%</div>
                            )}
                            {modelInfo.last_updated && (
                              <div>Last Updated: {new Date(modelInfo.last_updated).toLocaleDateString()}</div>
                            )}
                          </div>
                        }
                      />
                    </List.Item>
                  );
                }}
              />
            )}
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col span={24}>
          <Card title="Prediction Accuracy" size="small">
            <Row gutter={[16, 16]}>
              {stats?.prediction_accuracy && Object.entries(stats.prediction_accuracy).map(([type, accuracy]) => (
                <Col xs={24} sm={8} key={type}>
                  <Statistic
                    title={type.replace('_', ' ').toUpperCase()}
                    value={Math.round(accuracy * 100)}
                    suffix="%"
                    valueStyle={{ 
                      color: accuracy > 0.8 ? '#52c41a' : accuracy > 0.6 ? '#faad14' : '#ff4d4f' 
                    }}
                  />
                </Col>
              ))}
            </Row>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default AIEngineDashboard;

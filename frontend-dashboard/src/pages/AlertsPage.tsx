'use client';



import React, { useState, useEffect } from 'react';

import { 

  Table, 

  Card, 

  Button, 

  Space, 

  Tag, 

  Input, 

  Select, 

  DatePicker, 

  Modal, 

  Descriptions, 

  Badge, 

  Tooltip,

  Row,

  Col,

  Statistic,

  Alert,

  Timeline,

  Tabs,

  Progress

} from 'antd';

import {

  ExclamationCircleOutlined,

  EyeOutlined,

  CheckCircleOutlined,

  CloseCircleOutlined,

  UserOutlined,

  ClockCircleOutlined,

  EnvironmentOutlined,

  SearchOutlined,

  FilterOutlined,

  InfoCircleOutlined,

  FireOutlined

} from '@ant-design/icons';

import type { ColumnsType } from 'antd/es/table';

import { API_ENDPOINTS, apiClient } from '@/services/api';

import { useTheme } from '@/contexts/ThemeContext';



const { Search: SearchInput } = Input;

const { Option } = Select;

const { RangePicker } = DatePicker;

const { TabPane } = Tabs;



interface Alert {

  id: string;

  title: string;

  description: string;

  severity: 'critical' | 'high' | 'medium' | 'low';

  status: 'active' | 'acknowledged' | 'resolved' | 'closed';

  priority: 'urgent' | 'high' | 'medium' | 'low';

  category: string;

  location: {

    address: string;

    latitude: string;

    longitude: string;

  };

  timestamp: string;

  confidence: number;

  threatLevel: string;

  assignedTo?: string;

  acknowledgedAt?: string;

  resolvedAt?: string;

  resolutionNotes?: string;

  aiAnalysis?: {

    keyFactors: string[];

    confidence: number;

    explanation: string;

    recommendedActions: string[];

  };

}



export default function AlertsPage() {

  const { themeStyles } = useTheme();

  const [alerts, setAlerts] = useState<Alert[]>([]);

  const [loading, setLoading] = useState(true);

  const [error, setError] = useState<string | null>(null);

  const [selectedAlert, setSelectedAlert] = useState<Alert | null>(null);

  const [detailModalVisible, setDetailModalVisible] = useState(false);

  const [filters, setFilters] = useState({

    severity: '',

    status: '',

    category: '',

    search: '',

    dateRange: null as any

  });

  const [statistics, setStatistics] = useState({

    total: 0,

    active: 0,

    critical: 0,

    unacknowledged: 0

  });



  useEffect(() => {

    fetchAlerts();

    fetchStatistics();

    

    // Set up periodic refresh

    const refreshInterval = setInterval(() => {

      fetchAlerts();

      fetchStatistics();

    }, 30000); // Refresh every 30 seconds

    

    return () => clearInterval(refreshInterval);

  }, []);



  const fetchAlerts = async () => {

    try {

      setLoading(true);

      setError(null);

      const timeoutPromise = new Promise((_, reject) => 

        setTimeout(() => reject(new Error('Request timeout')), 10000)

      );

      

      const data = await Promise.race([apiClient.get(API_ENDPOINTS.ALERTS.ALL), timeoutPromise]);

      setAlerts(data.content || []);

    } catch (error) {

      console.error('Failed to fetch alerts:', error);

      setError('Failed to fetch alerts');

      // Mock data for demonstration

      setAlerts([

        {

          id: '1',

          title: 'Suspicious Activity Detected - Nairobi CBD',

          description: 'Multiple reports of suspicious gatherings in central business district',

          severity: 'high',

          status: 'active',

          priority: 'urgent',

          category: 'social_unrest',

          location: {

            address: 'Nairobi CBD, Kenya',

            latitude: '-1.2921',

            longitude: '36.8219'

          },

          timestamp: new Date().toISOString(),

          confidence: 0.82,

          threatLevel: 'high',

          aiAnalysis: {

            keyFactors: ['crowd formation', 'government buildings', 'social media activity'],

            confidence: 0.82,

            explanation: 'AI analysis indicates unusual crowd patterns near sensitive infrastructure',

            recommendedActions: ['Increase police presence', 'Monitor social media', 'Prepare contingency plans']

          }

        },

        {

          id: '2',

          title: 'Cyber Threat Alert - Banking Sector',

          description: 'Increased phishing attempts targeting Kenyan banks',

          severity: 'medium',

          status: 'acknowledged',

          priority: 'high',

          category: 'cyber',

          location: {

            address: 'Westlands, Nairobi',

            latitude: '-1.2864',

            longitude: '36.8172'

          },

          timestamp: new Date(Date.now() - 3600000).toISOString(),

          confidence: 0.78,

          threatLevel: 'medium',

          assignedTo: 'John Doe',

          acknowledgedAt: new Date(Date.now() - 1800000).toISOString()

        }

      ]);

    } finally {

      setLoading(false);

    }

  };



  const fetchStatistics = async () => {

    try {

      const timeoutPromise = new Promise((_, reject) => 

        setTimeout(() => reject(new Error('Request timeout')), 10000)

      );

      

      const data = await Promise.race([apiClient.get(API_ENDPOINTS.ALERTS.DASHBOARD), timeoutPromise]);

      setStatistics({

        total: data.totalAlerts || 0,

        active: data.activeAlerts || 0,

        critical: data.criticalAlerts || 0,

        unacknowledged: data.unacknowledgedAlerts || 0

      });

    } catch (error) {

      console.error('Failed to fetch statistics:', error);

      // Set default values to prevent UI crashes

      setStatistics({

        total: 0,

        active: 0,

        critical: 0,

        unacknowledged: 0

      });

    }

  };



  const handleViewDetails = (alert: Alert) => {

    setSelectedAlert(alert);

    setDetailModalVisible(true);

  };



  const handleAcknowledge = async (alertId: string) => {

    try {

      await apiClient.post(`${API_ENDPOINTS.ALERTS.UPDATE(alertId)}/acknowledge`);

      fetchAlerts();

      fetchStatistics();

    } catch (error) {

      console.error('Failed to acknowledge alert:', error);

    }

  };



  const handleResolve = async (alertId: string, notes: string) => {

    try {

      await apiClient.post(`${API_ENDPOINTS.ALERTS.UPDATE(alertId)}/resolve`, { resolutionNotes: notes });

      fetchAlerts();

      fetchStatistics();

      setDetailModalVisible(false);

    } catch (error) {

      console.error('Failed to resolve alert:', error);

    }

  };



  const getSeverityColor = (severity: string) => {

    const colors = {

      critical: themeStyles.kenyanRed,

      high: themeStyles.warningColor,

      medium: themeStyles.kenyanGreen,

      low: themeStyles.successColor

    };

    return colors[severity as keyof typeof colors] || 'default';

  };



  const getStatusColor = (status: string) => {

    const colors = {

      active: themeStyles.kenyanRed,

      acknowledged: themeStyles.warningColor,

      resolved: themeStyles.kenyanGreen,

      closed: themeStyles.mutedTextColor

    };

    return colors[status as keyof typeof colors] || 'default';

  };



  const getPriorityColor = (priority: string) => {

    const colors = {

      urgent: themeStyles.kenyanRed,

      high: themeStyles.warningColor,

      medium: themeStyles.kenyanGreen,

      low: themeStyles.successColor

    };

    return colors[priority as keyof typeof colors] || 'default';

  };



  const columns: ColumnsType<Alert> = [

    {

      title: 'Alert',

      dataIndex: 'title',

      key: 'title',

      render: (text: string, record: Alert) => (

        <Space>

          <ExclamationCircleOutlined style={{ color: record.severity === 'critical' ? themeStyles.kenyanRed : themeStyles.warningColor }} />

          <div>

            <div style={{ fontWeight: 'bold' }}>{text}</div>

            <div style={{ fontSize: '12px', color: themeStyles.secondaryTextColor }}>{record.category}</div>

          </div>

        </Space>

      ),

    },

    {

      title: 'Severity',

      dataIndex: 'severity',

      key: 'severity',

      render: (severity: string) => (

        <Tag color={getSeverityColor(severity)}>{severity?.toUpperCase() || 'UNKNOWN'}</Tag>

      ),

      filters: [

        { text: 'Critical', value: 'critical' },

        { text: 'High', value: 'high' },

        { text: 'Medium', value: 'medium' },

        { text: 'Low', value: 'low' },

      ],

      onFilter: (value, record) => record.severity === value,

    },

    {

      title: 'Status',

      dataIndex: 'status',

      key: 'status',

      render: (status: string) => (

        <Tag color={getStatusColor(status)}>{status?.toUpperCase() || 'UNKNOWN'}</Tag>

      ),

      filters: [

        { text: 'Active', value: 'active' },

        { text: 'Acknowledged', value: 'acknowledged' },

        { text: 'Resolved', value: 'resolved' },

        { text: 'Closed', value: 'closed' },

      ],

      onFilter: (value, record) => record.status === value,

    },

    {

      title: 'Priority',

      dataIndex: 'priority',

      key: 'priority',

      render: (priority: string) => (

        <Tag color={getPriorityColor(priority)}>{priority?.toUpperCase() || 'UNKNOWN'}</Tag>

      ),

    },

    {

      title: 'Location',

      dataIndex: 'location',

      key: 'location',

      render: (location: any) => (

        <Space>

          <EnvironmentOutlined />

          <span>{location.address}</span>

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

          strokeColor={confidence > 0.8 ? themeStyles.kenyanGreen : confidence > 0.6 ? themeStyles.warningColor : themeStyles.kenyanRed}

        />

      ),

    },

    {

      title: 'Time',

      dataIndex: 'timestamp',

      key: 'timestamp',

      render: (timestamp: string) => (

        <Space>

          <ClockCircleOutlined />

          <span>{new Date(timestamp).toLocaleString()}</span>

        </Space>

      ),

    },

    {

      title: 'Actions',

      key: 'actions',

      render: (record: Alert) => (

        <Space>

          <Button size="small" icon={<EyeOutlined />} onClick={() => handleViewDetails(record)}>

            View

          </Button>

          {record.status === 'active' && (

            <Button 

              size="small" 

              type="primary" 

              icon={<CheckCircleOutlined />}

              onClick={() => handleAcknowledge(record.id)}

            >

              Acknowledge

            </Button>

          )}

        </Space>

      ),

    },

  ];



  const filteredAlerts = alerts.filter(alert => {

    if (filters.severity && alert.severity !== filters.severity) return false;

    if (filters.status && alert.status !== filters.status) return false;

    if (filters.category && alert.category !== filters.category) return false;

    if (filters.search && !alert.title.toLowerCase().includes(filters.search.toLowerCase())) return false;

    return true;

  });



  return (

    <div style={{ padding: '24px' }}>

      <div style={{ marginBottom: '24px' }}>

        <h1 style={{ fontSize: '24px', fontWeight: 'bold', margin: 0 }}>

          Alert Management

        </h1>

        <p style={{ color: '#666', marginTop: '8px' }}>

          Monitor and manage security alerts with intelligent analysis

        </p>

      </div>



      {/* Statistics Cards */}

      <Row gutter={[16, 16]} style={{ marginBottom: '24px' }}>

        <Col xs={24} sm={6}>

          <Card>

            <Statistic

              title="Total Alerts"

              value={statistics.total}

              prefix={<ExclamationCircleOutlined />}

              valueStyle={{ color: '#1890ff' }}

            />

          </Card>

        </Col>

        <Col xs={24} sm={6}>

          <Card>

            <Statistic

              title="Active Alerts"

              value={statistics.active}

              prefix={<ExclamationCircleOutlined />}

              valueStyle={{ color: '#ff4d4f' }}

            />

          </Card>

        </Col>

        <Col xs={24} sm={6}>

          <Card>

            <Statistic

              title="Critical Alerts"

              value={statistics.critical}

              prefix={<FireOutlined />}

              valueStyle={{ color: '#ff7a45' }}

            />

          </Card>

        </Col>

        <Col xs={24} sm={6}>

          <Card>

            <Statistic

              title="Unacknowledged"

              value={statistics.unacknowledged}

              prefix={<ClockCircleOutlined />}

              valueStyle={{ color: '#fa8c16' }}

            />

          </Card>

        </Col>

      </Row>



      {/* Filters */}

      <Card style={{ marginBottom: '24px' }}>

        <Row gutter={[16, 16]} align="middle">

          <Col xs={24} sm={8}>

            <SearchInput

              placeholder="Search alerts..."

              prefix={<SearchOutlined />}

              value={filters.search}

              onChange={(e) => setFilters({ ...filters, search: e.target.value })}

              allowClear

            />

          </Col>

          <Col xs={24} sm={4}>

            <Select

              placeholder="Severity"

              value={filters.severity}

              onChange={(value) => setFilters({ ...filters, severity: value })}

              style={{ width: '100%' }}

              allowClear

            >

              <Option value="critical">Critical</Option>

              <Option value="high">High</Option>

              <Option value="medium">Medium</Option>

              <Option value="low">Low</Option>

            </Select>

          </Col>

          <Col xs={24} sm={4}>

            <Select

              placeholder="Status"

              value={filters.status}

              onChange={(value) => setFilters({ ...filters, status: value })}

              style={{ width: '100%' }}

              allowClear

            >

              <Option value="active">Active</Option>

              <Option value="acknowledged">Acknowledged</Option>

              <Option value="resolved">Resolved</Option>

              <Option value="closed">Closed</Option>

            </Select>

          </Col>

          <Col xs={24} sm={4}>

            <Select

              placeholder="Category"

              value={filters.category}

              onChange={(value) => setFilters({ ...filters, category: value })}

              style={{ width: '100%' }}

              allowClear

            >

              <Option value="social_unrest">Social Unrest</Option>

              <Option value="cyber">Cyber</Option>

              <Option value="criminal">Criminal</Option>

              <Option value="terror">Terror</Option>

            </Select>

          </Col>

          <Col xs={24} sm={4}>

            <Button icon={<FilterOutlined />} onClick={() => setFilters({ severity: '', status: '', category: '', search: '', dateRange: null })}>

              Clear Filters

            </Button>

          </Col>

        </Row>

      </Card>



      {/* Alerts Table */}

      <Card>

        <Table

          columns={columns}

          dataSource={filteredAlerts}

          rowKey="id"

          loading={loading}

          pagination={{

            total: filteredAlerts.length,

            pageSize: 10,

            showSizeChanger: true,

            showQuickJumper: true,

            showTotal: (total, range) => `${range[0]}-${range[1]} of ${total} alerts`,

          }}

          scroll={{ x: 1200 }}

        />

      </Card>



      {/* Alert Detail Modal */}

      <Modal

        title={

          <Space>

            <ExclamationCircleOutlined style={{ color: selectedAlert?.severity === 'critical' ? '#ff4d4f' : '#fa8c16' }} />

            <span>{selectedAlert?.title}</span>

          </Space>

        }

        open={detailModalVisible}

        onCancel={() => setDetailModalVisible(false)}

        footer={[

          <Button key="close" onClick={() => setDetailModalVisible(false)}>

            Close

          </Button>,

          selectedAlert?.status === 'active' && (

            <Button 

              key="acknowledge" 

              type="primary" 

              icon={<CheckCircleOutlined />}

              onClick={() => selectedAlert && handleAcknowledge(selectedAlert.id)}

            >

              Acknowledge

            </Button>

          ),

          selectedAlert?.status !== 'resolved' && (

            <Button 

              key="resolve" 

              type="primary" 

              danger

              icon={<CloseCircleOutlined />}

              onClick={() => {

                Modal.confirm({

                  title: 'Resolve Alert',

                  content: 'Please provide resolution notes:',

                  okText: 'Resolve',

                  cancelText: 'Cancel',

                  onOk: () => {

                    // In a real implementation, you'd show an input field for notes

                    selectedAlert && handleResolve(selectedAlert.id, 'Resolved by analyst');

                  }

                });

              }}

            >

              Resolve

            </Button>

          ),

        ]}

        width={800}

      >

        {selectedAlert && (

          <Tabs defaultActiveKey="details">

            <TabPane tab="Alert Details" key="details">

              <Descriptions bordered column={2}>

                <Descriptions.Item label="Severity">

                  <Tag color={getSeverityColor(selectedAlert.severity)}>

                    {selectedAlert.severity?.toUpperCase() || 'UNKNOWN'}

                  </Tag>

                </Descriptions.Item>

                <Descriptions.Item label="Status">

                  <Tag color={getStatusColor(selectedAlert.status)}>

                    {selectedAlert.status?.toUpperCase() || 'UNKNOWN'}

                  </Tag>

                </Descriptions.Item>

                <Descriptions.Item label="Priority">

                  <Tag color={getPriorityColor(selectedAlert.priority)}>

                    {selectedAlert.priority?.toUpperCase() || 'UNKNOWN'}

                  </Tag>

                </Descriptions.Item>

                <Descriptions.Item label="Category">

                  {selectedAlert.category}

                </Descriptions.Item>

                <Descriptions.Item label="Location" span={2}>

                  <Space>

                    <EnvironmentOutlined />

                    {selectedAlert.location.address}

                  </Space>

                </Descriptions.Item>

                <Descriptions.Item label="Confidence">

                  <Progress

                    percent={Math.round(selectedAlert.confidence * 100)}

                    size="small"

                    strokeColor={selectedAlert.confidence > 0.8 ? '#52c41a' : '#fa8c16'}

                  />

                </Descriptions.Item>

                <Descriptions.Item label="Threat Level">

                  {selectedAlert.threatLevel}

                </Descriptions.Item>

                <Descriptions.Item label="Timestamp" span={2}>

                  <Space>

                    <ClockCircleOutlined />

                    {new Date(selectedAlert.timestamp).toLocaleString()}

                  </Space>

                </Descriptions.Item>

                <Descriptions.Item label="Assigned To">

                  {selectedAlert.assignedTo || <span style={{ color: '#999' }}>Unassigned</span>}

                </Descriptions.Item>

                <Descriptions.Item label="Acknowledged At">

                  {selectedAlert.acknowledgedAt ? 

                    new Date(selectedAlert.acknowledgedAt).toLocaleString() : 

                    <span style={{ color: '#999' }}>Not acknowledged</span>

                  }

                </Descriptions.Item>

                <Descriptions.Item label="Description" span={2}>

                  {selectedAlert.description}

                </Descriptions.Item>

                {selectedAlert.resolutionNotes && (

                  <Descriptions.Item label="Resolution Notes" span={2}>

                    {selectedAlert.resolutionNotes}

                  </Descriptions.Item>

                )}

              </Descriptions>

            </TabPane>

            

            {selectedAlert.aiAnalysis && (

              <TabPane tab="Analysis" key="ai">

                <Alert

                  message="Threat Analysis"

                  description={selectedAlert.aiAnalysis.explanation}

                  type="info"

                  showIcon

                  style={{ marginBottom: '16px' }}

                />

                

                <Card title="Key Risk Factors" size="small" style={{ marginBottom: '16px' }}>

                  <Space wrap>

                    {selectedAlert.aiAnalysis.keyFactors.map((factor, index) => (

                      <Tag key={index} color="blue">{factor}</Tag>

                    ))}

                  </Space>

                </Card>

                

                <Card title="Recommended Actions" size="small">

                  <Timeline>

                    {selectedAlert.aiAnalysis.recommendedActions.map((action, index) => (

                      <Timeline.Item key={index} color="blue">

                        {action}

                      </Timeline.Item>

                    ))}

                  </Timeline>

                </Card>

              </TabPane>

            )}

          </Tabs>

        )}

      </Modal>

    </div>

  );

}


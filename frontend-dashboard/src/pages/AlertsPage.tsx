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
  Progress,
  message,
  notification
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
  createdAt: string;
  updatedAt: string;
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
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedAlert, setSelectedAlert] = useState<Alert | null>(null);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [assignModalVisible, setAssignModalVisible] = useState(false);
  const [assignTo, setAssignTo] = useState('');
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
    
    // Auto-refresh disabled to prevent UI interference
    // const refreshInterval = setInterval(() => {
    //   fetchAlerts();
    //   fetchStatistics();
    // }, 30000); // Refresh every 30 seconds
    // return () => clearInterval(refreshInterval);
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
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
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
          createdAt: new Date(Date.now() - 3600000).toISOString(),
          updatedAt: new Date(Date.now() - 1800000).toISOString(),
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
      message.success('Alert acknowledged successfully');
    } catch (error) {
      console.error('Failed to acknowledge alert:', error);
      message.error('Failed to acknowledge alert');
    }
  };

  const handleResolve = async (alertId: string) => {
    const notes = prompt('Please enter resolution notes:');
    if (notes === null) return; // User cancelled
    
    try {
      setLoading(true);
      const response = await apiClient.post(`${API_ENDPOINTS.ALERTS.UPDATE(alertId)}/resolve`, { resolutionNotes: notes });
      
      // Handle response data properly - check if response is the alert itself or wrapped
      const resolvedAlert = response.data || response;
      
      // Show success notification with more details
      message.success('Alert resolved successfully!', 4);
      
      // Show resolved alert in a more prominent way
      if (resolvedAlert) {
        showResolvedAlertNotification(resolvedAlert, notes);
      } else {
        // Fallback if no alert data returned
        message.info(`Alert ${alertId} has been resolved with notes: ${notes}`, 4);
      }
      
    } catch (error) {
      console.error('Failed to resolve alert:', error);
      
      // Graceful error handling
      const errorMessage = error?.response?.data?.message || error?.message || 'Failed to resolve alert';
      
      message.error(`Resolution Failed: ${errorMessage}`, 5);
    } finally {
      setLoading(false);
    }
  };

  const handleUnresolve = async (alertId: string) => {
    const confirm = window.confirm('Are you sure you want to mark this alert as unresolved? This will change its status back to ACTIVE.');
    if (!confirm) return;
    
    try {
      setLoading(true);
      const response = await apiClient.post(`${API_ENDPOINTS.ALERTS.UPDATE(alertId)}/unresolve`);
      
      // Handle response data properly
      const unresolvedAlert = response.data || response;
      
      // Show success notification
      message.success('Alert unresolved successfully!', 4);
      
      // Refresh data
      await Promise.all([fetchAlerts(), fetchStatistics()]);
      
      // Show unresolve notification
      showUnresolvedAlertNotification(unresolvedAlert);
      
    } catch (error) {
      console.error('Failed to unresolve alert:', error);
      
      // Graceful error handling
      const errorMessage = error?.response?.data?.message || error?.message || 'Failed to unresolve alert';
      
      message.error(`Unresolve Failed: ${errorMessage}`, 5);
    } finally {
      setLoading(false);
    }
  };

  const showResolvedAlertNotification = (resolvedAlert: Alert, notes: string) => {
    // Create a temporary notification for the resolved alert
    if (!resolvedAlert || !resolvedAlert.id) {
      console.error('Invalid resolved alert data:', resolvedAlert);
      message.error('Unable to show resolution notification - invalid alert data');
      return;
    }
    
    const notificationKey = `resolved-${resolvedAlert.id}`;
    
    notification.success({
      message: '✅ Alert Resolved',
      description: (
        <div>
          <div><strong>{resolvedAlert.title || 'Unknown Alert'}</strong></div>
          <div style={{ fontSize: '12px', color: '#666', marginTop: '4px' }}>
            Notes: {notes || 'No notes provided'}
          </div>
          <div style={{ fontSize: '12px', color: '#52c41a', marginTop: '4px' }}>
            Status: RESOLVED
          </div>
        </div>
      ),
      duration: 6,
      key: notificationKey,
      placement: 'topRight',
    });
  };

  const showUnresolvedAlertNotification = (unresolvedAlert: Alert) => {
    // Create a temporary notification for the unresolved alert
    if (!unresolvedAlert || !unresolvedAlert.id) {
      console.error('Invalid unresolved alert data:', unresolvedAlert);
      message.error('Unable to show unresolve notification - invalid alert data');
      return;
    }
    
    const notificationKey = `unresolved-${unresolvedAlert.id}`;
    
    notification.info({
      message: '↩️ Alert Unresolved',
      description: (
        <div>
          <div><strong>{unresolvedAlert.title || 'Unknown Alert'}</strong></div>
          <div style={{ fontSize: '12px', color: '#666', marginTop: '4px' }}>
            Alert has been marked as active again
          </div>
          <div style={{ fontSize: '12px', color: '#1890ff', marginTop: '4px' }}>
            Status: ACTIVE
          </div>
        </div>
      ),
      duration: 5,
      key: notificationKey,
      placement: 'topRight',
    });
  };

  const showResolutionDetails = (alert: Alert) => {
    Modal.info({
      title: 'Resolution Details',
      width: 600,
      content: (
        <div>
          <p><strong>Alert:</strong> {alert.title}</p>
          <p><strong>Status:</strong> <Tag color="green">RESOLVED</Tag></p>
          <p><strong>Severity:</strong> <Tag color={getSeverityColor(alert.severity)}>{alert.severity?.toUpperCase()}</Tag></p>
          <p><strong>Category:</strong> {alert.category}</p>
          <p><strong>Location:</strong> {alert.location?.address}</p>
          <p><strong>Confidence:</strong> <Progress percent={Math.round((alert.confidence || 0) * 100)} size="small" strokeColor="#52c41a" /></p>
          <p><strong>AI Analysis:</strong></p>
          <div style={{ 
            backgroundColor: '#f6ffed', 
            border: '1px solid #b7eb8f', 
            borderRadius: '4px', 
            padding: '12px',
            marginTop: '8px'
          }}>
            <p><strong>Key Factors:</strong></p>
            <ul>
              {alert.aiAnalysis?.keyFactors?.map((factor, index) => (
                <li key={index}>{factor}</li>
              ))}
            </ul>
            <p><strong>Recommended Actions:</strong></p>
            <ul>
              {alert.aiAnalysis?.recommendedActions?.map((action, index) => (
                <li key={index}>{action}</li>
              ))}
            </ul>
          </div>
          {alert.resolvedAt && (
            <p><strong>Resolved At:</strong> {new Date(alert.resolvedAt).toLocaleString()}</p>
          )}
        </div>
      ),
    });
  };

  const handleAssign = async (alertId: string) => {
    setSelectedAlert(alerts.find(alert => alert.id === alertId) || null);
    setAssignModalVisible(true);
  };

  const handleAssignSubmit = async () => {
    if (!selectedAlert || !assignTo.trim()) {
      message.error('Please enter a valid assignee');
      return;
    }
    
    try {
      setLoading(true);
      const response = await apiClient.post(`${API_ENDPOINTS.ALERTS.UPDATE(selectedAlert.id)}/assign`, { assignedTo: assignTo.trim() });
      
      // Show success notification with more details
      message.success(`Alert assigned successfully to ${assignTo.trim()}!`, 4);
      
      // Refresh data
      await Promise.all([fetchAlerts(), fetchStatistics()]);
      
      // Show assigned alert notification
      showAssignedAlertNotification(response.data, assignTo.trim());
      
      // Close modal
      setAssignModalVisible(false);
      setAssignTo('');
      setSelectedAlert(null);
      
    } catch (error) {
      console.error('Failed to assign alert:', error);
      
      // Graceful error handling
      const errorMessage = error?.response?.data?.message || error?.message || 'Failed to assign alert';
      
      message.error(`Assignment Failed: ${errorMessage}`, 5);
    } finally {
      setLoading(false);
    }
  };

  const showAssignedAlertNotification = (assignedAlert: Alert, assignedTo: string) => {
    const notificationKey = `assigned-${assignedAlert.id}`;
    
    notification.success({
      message: '👤 Alert Assigned',
      description: (
        <div>
          <div><strong>{assignedAlert.title}</strong></div>
          <div style={{ fontSize: '12px', color: '#666', marginTop: '4px' }}>
            Assigned to: {assignedTo}
          </div>
          <div style={{ fontSize: '12px', color: '#1890ff', marginTop: '4px' }}>
            Status: ASSIGNED
          </div>
        </div>
      ),
      duration: 5,
      key: notificationKey,
      placement: 'topRight',
    });
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
      resolved: 'green',
      closed: 'default'
    };
    return colors[status as keyof typeof colors] || 'default';
  };

  const getPriorityColor = (priority: string) => {
    const colors = {
      urgent: 'red',
      high: 'orange',
      medium: 'gold',
      low: 'green'
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
          {record.status === 'resolved' && (
            <Badge 
              count="RESOLVED" 
              style={{ 
                backgroundColor: '#52c41a',
                color: 'white',
                fontSize: '10px',
                padding: '2px 6px',
                borderRadius: '4px'
              }} 
            />
          )}
          <ExclamationCircleOutlined style={{ color: record.severity === 'critical' ? '#ff4d4f' : '#fa8c16' }} />
          <div>
            <div style={{ fontWeight: 'bold' }}>{text}</div>
            <div style={{ fontSize: '12px', color: '#666' }}>{record.category}</div>
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
      title: 'Threat Level',
      dataIndex: 'threatLevel',
      key: 'threatLevel',
      render: (threatLevel: string) => {
        if (!threatLevel) return <Tag color="default">UNKNOWN</Tag>;
        
        const threatColors: Record<string, string> = {
          'critical': 'red',
          'high': 'orange', 
          'medium': 'gold',
          'low': 'green',
          'unknown': 'default'
        };
        
        const color = threatColors[threatLevel.toLowerCase()] || 'default';
        return (
          <Tag color={color} style={{ fontWeight: 'bold', fontSize: '12px' }}>
            {threatLevel.toUpperCase()}
          </Tag>
        );
      },
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: string, record: Alert) => (
        <Space>
          {status === 'resolved' && (
            <Badge 
              count="✅ RESOLVED" 
              style={{ 
                backgroundColor: '#52c41a',
                color: 'white',
                fontSize: '11px',
                padding: '3px 8px',
                borderRadius: '4px',
                fontWeight: 'bold'
              }} 
            />
          )}
          <Tag color={getStatusColor(status)}>{status?.toUpperCase() || 'UNKNOWN'}</Tag>
        </Space>
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
          strokeColor={confidence > 0.8 ? '#52c41a' : confidence > 0.6 ? '#fa8c16' : '#ff4d4f'}
        />
      ),
    },
    {
      title: 'Time',
      dataIndex: 'timestamp',
      key: 'timestamp',
      render: (timestamp: string, record: Alert) => {
        // Use timestamp if available, otherwise use createdAt
        const dateToUse = timestamp || record.createdAt;
        if (!dateToUse) {
          return <span>No Time</span>;
        }
        try {
          const date = new Date(dateToUse);
          if (isNaN(date.getTime())) {
            return <span>Invalid Date</span>;
          }
          return (
            <Space>
              <ClockCircleOutlined />
              <span>{date.toLocaleString()}</span>
            </Space>
          );
        } catch (error) {
          return <span>Date Error</span>;
        }
      },
    },
    {
      title: 'Assigned To',
      dataIndex: 'assignedTo',
      key: 'assignedTo',
      render: (assignedTo: string) => (
        <span style={{ color: assignedTo ? '#1890ff' : '#8c8c8c' }}>
          {assignedTo || <Tag color="default">UNASSIGNED</Tag>}
        </span>
      ),
    },
    {
      title: 'Acknowledged',
      dataIndex: 'acknowledgedAt',
      key: 'acknowledgedAt',
      render: (acknowledgedAt: string, record: Alert) => {
        if (record.status === 'acknowledged' && acknowledgedAt) {
          return (
            <Space>
              <CheckCircleOutlined style={{ color: '#52c41a' }} />
              <span>{new Date(acknowledgedAt).toLocaleString()}</span>
            </Space>
          );
        }
        return <Tag color="orange">PENDING</Tag>;
      },
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (record: Alert) => (
        <Space>
          <Button size="small" icon={<EyeOutlined />} onClick={() => handleViewDetails(record)}>
            View Details
          </Button>
          {record.status === 'resolved' ? (
            // Show different actions for resolved alerts
            <Space>
              <Button 
                size="small" 
                type="default"
                icon={<CheckCircleOutlined />}
                style={{ backgroundColor: '#f6ffed', borderColor: '#b7eb8f', color: '#52c41a' }}
              >
                View Resolution
              </Button>
              <Button 
                size="small" 
                type="default"
                icon={<InfoCircleOutlined />}
                onClick={() => showResolutionDetails(record)}
              >
                Details
              </Button>
              <Button 
                size="small" 
                type="default"
                icon={<CloseCircleOutlined />}
                onClick={() => handleUnresolve(record.id)}
                style={{ backgroundColor: '#fff1f0', borderColor: '#ffccc7', color: '#ff4d4f' }}
              >
                Unresolve
              </Button>
            </Space>
          ) : (
            // Show normal actions for active/acknowledged alerts
            <>
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
              <Button 
                size="small" 
                type="default"
                icon={<UserOutlined />}
                onClick={() => handleAssign(record.id)}
              >
                Assign
              </Button>
              {(record.status === 'active' || record.status === 'acknowledged') && (
                <Button 
                  size="small" 
                  type="default"
                  icon={<CheckCircleOutlined />}
                  onClick={() => handleResolve(record.id)}
                >
                  Resolve
                </Button>
              )}
            </>
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
                    selectedAlert && handleResolve(selectedAlert.id);
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
          <div>
            <Descriptions column={2} bordered>
              <Descriptions.Item label="Severity">
                <Tag color={getSeverityColor(selectedAlert.severity)}>
                  {selectedAlert.severity?.toUpperCase()}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Status">
                <Tag color={getStatusColor(selectedAlert.status)}>
                  {selectedAlert.status?.toUpperCase()}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Priority">
                <Tag color={getPriorityColor(selectedAlert.priority)}>
                  {selectedAlert.priority?.toUpperCase()}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Threat Level">
                <Tag color={(() => {
                  const threatColors: Record<string, string> = {
                    'critical': 'red',
                    'high': 'orange', 
                    'medium': 'gold',
                    'low': 'green',
                    'unknown': 'default'
                  };
                  return threatColors[selectedAlert.threatLevel?.toLowerCase()] || 'default';
                })()}>
                  {selectedAlert.threatLevel?.toUpperCase() || 'UNKNOWN'}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Category">
                {selectedAlert.category}
              </Descriptions.Item>
              <Descriptions.Item label="Confidence">
                <Progress 
                  percent={Math.round((selectedAlert.confidence || 0) * 100)} 
                  size="small" 
                  strokeColor={selectedAlert.confidence > 0.8 ? '#52c41a' : selectedAlert.confidence > 0.6 ? '#fa8c16' : '#ff4d4f'}
                />
              </Descriptions.Item>
              <Descriptions.Item label="Location">
                <Space>
                  <EnvironmentOutlined />
                  <span>{selectedAlert.location?.address}</span>
                </Space>
              </Descriptions.Item>
              <Descriptions.Item label="Created">
                {new Date(selectedAlert.createdAt).toLocaleString()}
              </Descriptions.Item>
              <Descriptions.Item label="Assigned To" span={2}>
                {selectedAlert.assignedTo || <Tag color="default">UNASSIGNED</Tag>}
              </Descriptions.Item>
              <Descriptions.Item label="Description" span={2}>
                {selectedAlert.description}
              </Descriptions.Item>
            </Descriptions>
            
            {selectedAlert.aiAnalysis && (
              <div style={{ marginTop: '16px' }}>
                <h4>AI Analysis</h4>
                <Alert
                  message="AI Analysis Results"
                  type="info"
                  description={
                    <div>
                      <p><strong>Confidence:</strong> {Math.round((selectedAlert.aiAnalysis?.confidence || 0) * 100)}%</p>
                      <p><strong>Explanation:</strong> {selectedAlert.aiAnalysis?.explanation || 'No analysis available'}</p>
                      <p><strong>Recommended Actions:</strong></p>
                      <ul>
                        {selectedAlert.aiAnalysis?.recommendedActions?.map((action, index) => (
                          <li key={index}>{action}</li>
                        )) || <li>No recommendations available</li>}
                      </ul>
                    </div>
                  }
                  type="info"
                />
              </div>
            )}
          </div>
        )}
      </Modal>

      {/* Assign Modal */}
      <Modal
        title="Assign Alert"
        open={assignModalVisible}
        onOk={handleAssignSubmit}
        onCancel={() => {
          setAssignModalVisible(false);
          setAssignTo('');
        }}
        okText="Assign"
        cancelText="Cancel"
      >
        <div style={{ marginBottom: '16px' }}>
          <p><strong>Alert:</strong> {selectedAlert?.title}</p>
          <p><strong>Current Assignee:</strong> {selectedAlert?.assignedTo || 'Unassigned'}</p>
        </div>
        <Input
          placeholder="Enter assignee name or email"
          value={assignTo}
          onChange={(e) => setAssignTo(e.target.value)}
          prefix={<UserOutlined />}
        />
      </Modal>
    </div>
  );
}

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

  message

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

  const [assignModalVisible, setAssignModalVisible] = useState(false);

  const [assignee, setAssignee] = useState('');

  const [resolutionNotes, setResolutionNotes] = useState('');

  const [assignmentNotes, setAssignmentNotes] = useState('');

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



  const handleViewDetails = async (alert: Alert) => {
    setSelectedAlert(alert);
    
    // Load existing resolution notes
    try {
      const existingNotes = await loadResolutionNotes(alert.id);
      setResolutionNotes(existingNotes);
    } catch (error) {
      setResolutionNotes('');
    }
    
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


  const handleAssign = async () => {
    try {
      if (!selectedAlert || !assignee.trim()) {
        return;
      }

      await apiClient.post(API_ENDPOINTS.ALERTS.ASSIGN(selectedAlert.id), { 
        assignedTo: assignee.trim()
      });

      // Save assignment notes if provided
      if (resolutionNotes.trim()) {
        await apiClient.post(API_ENDPOINTS.ALERTS.SAVE_ASSIGNMENT_NOTES(selectedAlert.id), {
          notes: resolutionNotes.trim(),
          assignedTo: assignee.trim(),
          timestamp: new Date().toISOString()
        });
      }

      fetchAlerts();
      fetchStatistics();

      setAssignModalVisible(false);
      setAssignee('');
      setResolutionNotes('');
      setDetailModalVisible(false);

    } catch (error) {
      console.error('Failed to assign alert:', error);
    }
  };

  // Notes Management Functions
  const handleSaveAssignmentNotes = async () => {
    try {
      if (!selectedAlert || !assignmentNotes.trim()) {
        return;
      }

      await apiClient.post(API_ENDPOINTS.ALERTS.SAVE_ASSIGNMENT_NOTES(selectedAlert.id), {
        notes: assignmentNotes.trim(),
        timestamp: new Date().toISOString(),
        lastModified: new Date().toISOString()
      });

      // Show success feedback
      message.success('Assignment notes saved successfully');
      // Notes remain in the input field after successful save

    } catch (error) {
      console.error('Failed to save assignment notes:', error);
    }
  };

  const handleSaveResolutionNotes = async () => {
    try {
      if (!selectedAlert || !resolutionNotes.trim()) {
        return;
      }

      await apiClient.post(API_ENDPOINTS.ALERTS.SAVE_RESOLUTION_NOTES(selectedAlert.id), {
        notes: resolutionNotes.trim(),
        timestamp: new Date().toISOString(),
        lastModified: new Date().toISOString()
      });

      // Show success feedback
      message.success('Resolution notes saved successfully');
      
    } catch (error) {
      // Don't log error for 404 - it just means no notes exist yet
      if ((error as any).response?.status !== 404) {
        console.error('Failed to save resolution notes:', error);
      }
    }
  };

  const loadAssignmentNotes = async (alertId: string) => {
    try {
      const response = await apiClient.get(API_ENDPOINTS.ALERTS.ASSIGNMENT_NOTES(alertId));
      return response.data?.notes || '';
    } catch (error) {
      // Don't log error for 404 - it just means no notes exist yet
      if ((error as any).response?.status !== 404) {
        console.error('Failed to load assignment notes:', error);
      }
      return '';
    }
  };

  const loadResolutionNotes = async (alertId: string) => {
    try {
      const response = await apiClient.get(API_ENDPOINTS.ALERTS.RESOLUTION_NOTES(alertId));
      return response.data?.notes || '';
    } catch (error) {
      // Don't log error for 404 - it just means no notes exist yet
      if ((error as any).response?.status !== 404) {
        console.error('Failed to load resolution notes:', error);
      }
      return '';
    }
  };


  const showAssignModal = async (alert: Alert) => {

    setSelectedAlert(alert);

    setAssignee(alert.assignedTo || '');

    // Load existing assignment notes
    try {
      const existingNotes = await loadAssignmentNotes(alert.id);
      setAssignmentNotes(existingNotes);
    } catch (error) {
      setAssignmentNotes('');
    }

    setAssignModalVisible(true);

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

      title: 'Assigned To',

      dataIndex: 'assignedTo',

      key: 'assignedTo',

      render: (assignedTo: string) => (

        <Space>

          <UserOutlined />

          <span>{assignedTo || <span style={{ color: '#999' }}>Unassigned</span>}</span>

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

          <Button 

            size="small" 

            type="primary" 

            icon={<UserOutlined />}

            onClick={() => showAssignModal(record)}

          >

            Assign

          </Button>

          {record.status === 'active' && (

            <Button 

              size="small" 

              type="primary" 

              icon={<CheckCircleOutlined />}

              onClick={() => handleAcknowledge(record.id)}

            >

              Ack

            </Button>

          )}

        </Space>

      ),

    },

];



const filteredAlerts = alerts.filter(alert => {
  if (filters.severity && alert.severity !== filters.severity) return false;
  if (filters.status && alert.status !== filters.status) return false;
  if (filters.search && !alert.title.toLowerCase().includes(filters.search.toLowerCase())) return false;
  return true;
});

return (
  <div style={{ backgroundColor: themeStyles.kenyanBlack, minHeight: '100vh', padding: '20px' }}>
    {/* Header Banner */}
    <div style={{
      backgroundColor: '#000000',
      color: themeStyles.kenyanWhite,
      padding: '20px',
      marginBottom: '20px',
      borderRadius: '8px',
      border: `2px solid ${themeStyles.kenyanRed}`,
      textAlign: 'center',
      fontWeight: 'bold',
      fontSize: '24px',
      textShadow: '2px 2px 4px rgba(0,0,0,0.8)'
    }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '15px' }}>
        <div style={{ width: '40px', height: '40px', backgroundColor: themeStyles.kenyanBlack, border: '1px solid #fff' }}></div>
        <div style={{ width: '40px', height: '40px', backgroundColor: themeStyles.kenyanRed, border: '1px solid #fff' }}></div>
        <div style={{ width: '40px', height: '40px', backgroundColor: themeStyles.kenyanGreen, border: '1px solid #fff' }}></div>
        <span>NTEWS SECURITY ALERTS MONITOR</span>
        <div style={{ width: '40px', height: '40px', backgroundColor: themeStyles.kenyanGreen, border: '1px solid #fff' }}></div>
        <div style={{ width: '40px', height: '40px', backgroundColor: themeStyles.kenyanRed, border: '1px solid #fff' }}></div>
        <div style={{ width: '40px', height: '40px', backgroundColor: themeStyles.kenyanBlack, border: '1px solid #fff' }}></div>
      </div>
    </div>

    {/* Alert Statistics Banner */}
    <div style={{
      backgroundColor: '#000000',
      color: themeStyles.kenyanWhite,
      padding: '15px',
      marginBottom: '20px',
      borderRadius: '8px',
      border: `1px solid ${themeStyles.kenyanGreen}`,
      overflowX: 'auto'
    }}>
      <div style={{ display: 'flex', gap: '30px', minWidth: '600px' }}>
        <div>
          <span style={{ color: themeStyles.kenyanRed, fontWeight: 'bold' }}>Total Alerts: </span>
          <span>{statistics.total}</span>
        </div>
        <div>
          <span style={{ color: themeStyles.kenyanRed, fontWeight: 'bold' }}>Active: </span>
          <span>{statistics.active}</span>
        </div>
        <div>
          <span style={{ color: themeStyles.kenyanRed, fontWeight: 'bold' }}>Critical: </span>
          <span>{statistics.critical}</span>
        </div>
        <div>
          <span style={{ color: themeStyles.kenyanRed, fontWeight: 'bold' }}>Unacknowledged: </span>
          <span>{statistics.unacknowledged}</span>
        </div>
      </div>
    </div>

    {/* Main Table Container with Horizontal Scrolling */}
    <div style={{
      backgroundColor: '#000000',
      padding: '20px',
      borderRadius: '8px',
      border: `2px solid ${themeStyles.kenyanGreen}`,
      overflowX: 'auto'
    }}>
      <div style={{ minWidth: '1200px' }}>
        <Table
          columns={columns}
          dataSource={filteredAlerts}
          rowKey="id"
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => `${range[0]}-${range[1]} of ${total} alerts`,
          }}
          style={{
            backgroundColor: themeStyles.kenyanBlack,
            color: themeStyles.kenyanWhite
          }}
        />
      </div>
    </div>

    {/* Alert Details Modal */}
    <Modal
      title={
        <div style={{ color: themeStyles.kenyanWhite, textAlign: 'center' }}>
          <span style={{ color: themeStyles.kenyanRed }}>●</span> ALERT DETAILS <span style={{ color: themeStyles.kenyanRed }}>●</span>
        </div>
      }
      open={detailModalVisible}
      onCancel={() => setDetailModalVisible(false)}
      footer={[
        <Button key="close" onClick={() => setDetailModalVisible(false)}>
          Close
        </Button>,
        <Button 
          key="resolve" 
          type="primary" 
          style={{ backgroundColor: themeStyles.kenyanGreen, borderColor: themeStyles.kenyanGreen }}
          onClick={() => {
            if (selectedAlert) {
              handleResolve(selectedAlert.id, resolutionNotes);
            }
          }}
        >
          Resolve
        </Button>
      ]}
      width={800}
      style={{
        backgroundColor: themeStyles.kenyanBlack,
        color: themeStyles.kenyanWhite
      }}
    >
      {selectedAlert && (
        <div style={{ color: themeStyles.kenyanWhite }}>
          <Descriptions column={2} bordered>
            <Descriptions.Item label="Alert ID">{selectedAlert.id}</Descriptions.Item>
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
            <Descriptions.Item label="Title" span={2}>{selectedAlert.title}</Descriptions.Item>
            <Descriptions.Item label="Description" span={2}>{selectedAlert.description}</Descriptions.Item>
            <Descriptions.Item label="Location" span={2}>
              {selectedAlert.location?.address}
            </Descriptions.Item>
            <Descriptions.Item label="Confidence">
              <Progress
                percent={Math.round((selectedAlert.confidence || 0) * 100)}
                size="small"
                strokeColor={(selectedAlert.confidence || 0) > 0.8 ? themeStyles.kenyanGreen : (selectedAlert.confidence || 0) > 0.6 ? themeStyles.warningColor : themeStyles.kenyanRed}
              />
            </Descriptions.Item>
            <Descriptions.Item label="Time">
              {new Date(selectedAlert.timestamp).toLocaleString()}
            </Descriptions.Item>
            <Descriptions.Item label="Assigned To">
              {selectedAlert.assignedTo || <span style={{ color: '#999' }}>Unassigned</span>}
            </Descriptions.Item>
            <Descriptions.Item label="Resolution Notes" span={2}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                <Input.TextArea
                  value={resolutionNotes}
                  onChange={(e) => setResolutionNotes(e.target.value)}
                  placeholder="Enter resolution notes..."
                  rows={3}
                  style={{ backgroundColor: themeStyles.cardBackground, color: themeStyles.textColor }}
                />
                <Button
                  size="small"
                  style={{ 
                    backgroundColor: themeStyles.kenyanGreen, 
                    borderColor: themeStyles.kenyanGreen,
                    color: themeStyles.kenyanWhite,
                    alignSelf: 'flex-end'
                  }}
                  onClick={handleSaveResolutionNotes}
                  disabled={!resolutionNotes.trim()}
                >
                  Save Notes
                </Button>
              </div>
            </Descriptions.Item>
          </Descriptions>
        </div>
      )}
    </Modal>

    {/* Assign Modal */}
    <Modal
      title={
        <div style={{ color: themeStyles.kenyanWhite, textAlign: 'center' }}>
          <span style={{ color: themeStyles.kenyanRed }}>●</span> ASSIGN ALERT <span style={{ color: themeStyles.kenyanRed }}>●</span>
        </div>
      }
      open={assignModalVisible}
      onCancel={() => {
        setAssignModalVisible(false);
        setAssignee('');
        setResolutionNotes('');
      }}
      footer={[
        <Button key="cancel" onClick={() => {
          setAssignModalVisible(false);
          setAssignee('');
          setResolutionNotes('');
        }}>
          Cancel
        </Button>,
        <Button 
          key="assign" 
          type="primary" 
          style={{ backgroundColor: themeStyles.kenyanGreen, borderColor: themeStyles.kenyanGreen }}
          onClick={handleAssign}
          disabled={!assignee.trim()}
        >
          Assign Alert
        </Button>
      ]}
      style={{
        backgroundColor: themeStyles.kenyanBlack,
        color: themeStyles.kenyanWhite
      }}
    >
      {selectedAlert && (
        <div style={{ color: themeStyles.kenyanWhite }}>
          <Descriptions column={1} bordered>
            <Descriptions.Item label="Alert ID">{selectedAlert.id}</Descriptions.Item>
            <Descriptions.Item label="Title">{selectedAlert.title}</Descriptions.Item>
            <Descriptions.Item label="Severity">
              <Tag color={getSeverityColor(selectedAlert.severity)}>
                {selectedAlert.severity?.toUpperCase()}
              </Tag>
            </Descriptions.Item>
          </Descriptions>
          
          <div style={{ marginTop: '20px' }}>
            <label style={{ display: 'block', marginBottom: '8px', color: themeStyles.kenyanWhite }}>
              Assign To:
            </label>
            <Input
              value={assignee}
              onChange={(e) => setAssignee(e.target.value)}
              placeholder="Enter assignee name or ID..."
              style={{ backgroundColor: themeStyles.cardBackground, color: themeStyles.textColor }}
            />
          </div>
          
          <div style={{ marginTop: '20px' }}>
            <label style={{ display: 'block', marginBottom: '8px', color: themeStyles.kenyanWhite }}>
              Notes:
            </label>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              <Input.TextArea
                value={resolutionNotes}
                onChange={(e) => setResolutionNotes(e.target.value)}
                placeholder="Enter assignment notes..."
                rows={3}
                style={{ backgroundColor: themeStyles.cardBackground, color: themeStyles.textColor }}
              />
              <Button
                size="small"
                style={{ 
                  backgroundColor: themeStyles.kenyanGreen, 
                  borderColor: themeStyles.kenyanGreen,
                  color: themeStyles.kenyanWhite,
                  alignSelf: 'flex-end'
                }}
                onClick={handleSaveAssignmentNotes}
                disabled={!resolutionNotes.trim()}
              >
                Save Notes
              </Button>
            </div>
          </div>
        </div>
      )}
    </Modal>
  </div>
);
}
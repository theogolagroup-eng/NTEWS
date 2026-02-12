import React, { useState } from 'react';
import { Card, List, Button, Tag, Checkbox, Tooltip, Progress, Space } from 'antd';
import { 
  CheckCircleOutlined, 
  ClockCircleOutlined, 
  ExclamationCircleOutlined,
  InfoCircleOutlined,
  PlayCircleOutlined
} from '@ant-design/icons';

interface PreventiveAction {
  id: string;
  action: string;
  priority: 'critical' | 'high' | 'medium' | 'low';
  timeRequired: number; // in minutes
  resources: string[];
  status: 'pending' | 'in_progress' | 'completed';
  aiRecommendation: boolean;
  confidence?: number;
}

interface PreventiveActionsProps {
  actions: PreventiveAction[];
  onActionUpdate?: (actionId: string, status: string) => void;
  showAIInsights?: boolean;
}

const PreventiveActions: React.FC<PreventiveActionsProps> = ({
  actions,
  onActionUpdate,
  showAIInsights = true
}) => {
  const [completedActions, setCompletedActions] = useState<Set<string>>(new Set());

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'critical': return '#ff4d4f';
      case 'high': return '#ff7a45';
      case 'medium': return '#faad14';
      case 'low': return '#52c41a';
      default: return '#d9d9d9';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'completed': return <CheckCircleOutlined style={{ color: '#52c41a' }} />;
      case 'in_progress': return <PlayCircleOutlined style={{ color: '#1890ff' }} />;
      default: return <ClockCircleOutlined style={{ color: '#d9d9d9' }} />;
    }
  };

  const handleActionToggle = (actionId: string, checked: boolean) => {
    const newCompleted = new Set(completedActions);
    if (checked) {
      newCompleted.add(actionId);
      onActionUpdate?.(actionId, 'completed');
    } else {
      newCompleted.delete(actionId);
      onActionUpdate?.(actionId, 'pending');
    }
    setCompletedActions(newCompleted);
  };

  const sortedActions = [...actions].sort((a, b) => {
    const priorityOrder = { critical: 0, high: 1, medium: 2, low: 3 };
    return priorityOrder[a.priority] - priorityOrder[b.priority];
  });

  const completionRate = actions.length > 0 ? (completedActions.size / actions.length) * 100 : 0;

  return (
    <Card
      title={
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span>Preventive Actions</span>
          {showAIInsights && (
            <Tooltip title="AI-recommended preventive actions based on threat analysis">
              <InfoCircleOutlined style={{ color: '#1890ff' }} />
            </Tooltip>
          )}
        </div>
      }
      size="small"
    >
      {showAIInsights && (
        <div style={{ marginBottom: 16 }}>
          <Progress
            percent={Math.round(completionRate)}
            status="active"
            format={() => `${completedActions.size}/${actions.length} Completed`}
            size="small"
          />
        </div>
      )}

      <List
        dataSource={sortedActions}
        renderItem={(action) => (
          <List.Item
            style={{
              padding: '12px 0',
              borderBottom: '1px solid #f0f0f0',
              backgroundColor: completedActions.has(action.id) ? '#f6ffed' : 'white'
            }}
          >
            <div style={{ width: '100%' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 }}>
                <div style={{ flex: 1 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                    {getStatusIcon(action.status)}
                    <span style={{ fontWeight: 'bold' }}>{action.action}</span>
                    {action.aiRecommendation && (
                      <Tag color="blue">AI Recommended</Tag>
                    )}
                  </div>
                  
                  <Space size="small" style={{ marginBottom: 8 }}>
                    <Tag color={getPriorityColor(action.priority)}>
                      {action.priority.toUpperCase()}
                    </Tag>
                    <span style={{ fontSize: '12px', color: '#666' }}>
                      ⏱️ {action.timeRequired}min
                    </span>
                    {action.confidence && (
                      <span style={{ fontSize: '12px', color: '#666' }}>
                        🎯 {Math.round(action.confidence * 100)}% confidence
                      </span>
                    )}
                  </Space>
                  
                  {action.resources.length > 0 && (
                    <div style={{ fontSize: '12px', color: '#666' }}>
                      Resources: {action.resources.join(', ')}
                    </div>
                  )}
                </div>
                
                <Checkbox
                  checked={completedActions.has(action.id)}
                  onChange={(e) => handleActionToggle(action.id, e.target.checked)}
                  disabled={action.status === 'in_progress'}
                >
                  Complete
                </Checkbox>
              </div>
              
              {action.priority === 'critical' && (
                <div style={{ 
                  marginTop: 8, 
                  padding: 4, 
                  backgroundColor: '#fff2f0', 
                  borderRadius: 4,
                  fontSize: '12px',
                  color: '#ff4d4f',
                  display: 'flex',
                  alignItems: 'center',
                  gap: 4
                }}>
                  <ExclamationCircleOutlined />
                  Critical priority - Immediate attention required
                </div>
              )}
            </div>
          </List.Item>
        )}
      />
      
      {actions.length === 0 && (
        <div style={{ textAlign: 'center', padding: '20px', color: '#666' }}>
          No preventive actions recommended at this time
        </div>
      )}
    </Card>
  );
};

export default PreventiveActions;

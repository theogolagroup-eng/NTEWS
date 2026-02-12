import React from 'react';
import { Progress, Tag, Tooltip, Card } from 'antd';
import { InfoCircleOutlined } from '@ant-design/icons';

interface AIConfidenceIndicatorProps {
  confidence: number;
  analysis?: string;
  modelUsed?: string;
  dataPoints?: number;
  showDetails?: boolean;
}

const AIConfidenceIndicator: React.FC<AIConfidenceIndicatorProps> = ({
  confidence,
  analysis,
  modelUsed = 'ensemble_predictive_models',
  dataPoints = 0,
  showDetails = true
}) => {
  const getConfidenceColor = (conf: number) => {
    if (conf >= 0.8) return '#52c41a'; // Green
    if (conf >= 0.6) return '#faad14'; // Orange
    return '#ff4d4f'; // Red
  };

  const getConfidenceStatus = (conf: number) => {
    if (conf >= 0.8) return 'High';
    if (conf >= 0.6) return 'Medium';
    return 'Low';
  };

  const confidencePercent = Math.round(confidence * 100);
  const confidenceColor = getConfidenceColor(confidence);
  const confidenceStatus = getConfidenceStatus(confidence);

  return (
    <Card 
      size="small" 
      title={
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <span>AI Confidence</span>
          <Tooltip title="AI model confidence in prediction accuracy">
            <InfoCircleOutlined style={{ color: '#1890ff' }} />
          </Tooltip>
        </div>
      }
      style={{ marginBottom: 16 }}
    >
      <Progress
        percent={confidencePercent}
        status="active"
        strokeColor={confidenceColor}
        format={(percent) => `${percent}%`}
        style={{ marginBottom: 12 }}
      />
      
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Tag color={confidenceColor}>
          {confidenceStatus} Confidence
        </Tag>
        
        {showDetails && (
          <div style={{ fontSize: '12px', color: '#666' }}>
            <div>Model: {modelUsed}</div>
            {dataPoints > 0 && <div>Data Points: {dataPoints.toLocaleString()}</div>}
          </div>
        )}
      </div>
      
      {analysis && (
        <div style={{ 
          marginTop: 12, 
          padding: 8, 
          backgroundColor: '#f5f5f5', 
          borderRadius: 4,
          fontSize: '12px',
          color: '#666'
        }}>
          <strong>Analysis:</strong> {analysis}
        </div>
      )}
    </Card>
  );
};

export default AIConfidenceIndicator;

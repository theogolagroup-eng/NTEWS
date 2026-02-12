import React, { useState, useEffect } from 'react';
import { Card, Typography, Tag, Progress } from 'antd';
import { ClockCircleOutlined, ExclamationCircleOutlined } from '@ant-design/icons';

const { Title, Text } = Typography;

interface CountdownTimerProps {
  timeToCritical: number; // in minutes
  title?: string;
  showProgress?: boolean;
  onCritical?: () => void;
}

const CountdownTimer: React.FC<CountdownTimerProps> = ({
  timeToCritical,
  title = "Time to Critical",
  showProgress = true,
  onCritical
}) => {
  const [timeLeft, setTimeLeft] = useState(timeToCritical * 60); // Convert to seconds
  const [isCritical, setIsCritical] = useState(false);

  useEffect(() => {
    const timer = setInterval(() => {
      setTimeLeft(prev => {
        const newTime = prev - 1;
        
        // Check if we've reached critical time
        if (newTime <= 0 && !isCritical) {
          setIsCritical(true);
          onCritical?.();
        }
        
        return Math.max(0, newTime);
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [isCritical, onCritical]);

  const formatTime = (seconds: number) => {
    if (seconds <= 0) return "CRITICAL";
    
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;

    if (hours > 0) {
      return `${hours}h ${minutes}m ${secs}s`;
    } else if (minutes > 0) {
      return `${minutes}m ${secs}s`;
    } else {
      return `${secs}s`;
    }
  };

  const getUrgencyLevel = (seconds: number) => {
    if (seconds <= 0) return { level: 'critical', color: '#ff4d4f', icon: <ExclamationCircleOutlined /> };
    if (seconds <= 300) return { level: 'urgent', color: '#ff7a45', icon: <ClockCircleOutlined /> }; // 5 minutes
    if (seconds <= 1800) return { level: 'high', color: '#faad14', icon: <ClockCircleOutlined /> }; // 30 minutes
    if (seconds <= 3600) return { level: 'medium', color: '#1890ff', icon: <ClockCircleOutlined /> }; // 1 hour
    return { level: 'normal', color: '#52c41a', icon: <ClockCircleOutlined /> };
  };

  const urgency = getUrgencyLevel(timeLeft);
  const totalTime = timeToCritical * 60;
  const progressPercent = totalTime > 0 ? ((totalTime - timeLeft) / totalTime) * 100 : 100;

  return (
    <Card 
      size="small"
      style={{
        border: `2px solid ${urgency.color}`,
        backgroundColor: timeLeft <= 0 ? '#fff2f0' : 'white'
      }}
    >
      <div style={{ textAlign: 'center' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8, marginBottom: 8 }}>
          {urgency.icon}
          <Title level={5} style={{ margin: 0, color: urgency.color }}>
            {title}
          </Title>
        </div>
        
        <Title 
          level={2} 
          style={{ 
            margin: '8px 0', 
            color: urgency.color,
            fontWeight: 'bold'
          }}
        >
          {formatTime(timeLeft)}
        </Title>
        
        <Tag color={urgency.color} style={{ marginBottom: 12 }}>
          {urgency.level.toUpperCase()}
        </Tag>
        
        {showProgress && totalTime > 0 && (
          <Progress
            percent={progressPercent}
            status={timeLeft <= 0 ? 'exception' : 'active'}
            strokeColor={urgency.color}
            showInfo={false}
            size="small"
          />
        )}
        
        {timeLeft <= 300 && timeLeft > 0 && (
          <Text type="danger" style={{ fontSize: '12px', display: 'block', marginTop: 8 }}>
            Immediate action required!
          </Text>
        )}
        
        {timeLeft <= 0 && (
          <Text type="danger" strong style={{ fontSize: '14px', display: 'block', marginTop: 8 }}>
            CRITICAL TIME REACHED - EMERGENCY PROTOCOLS ACTIVATED
          </Text>
        )}
      </div>
    </Card>
  );
};

export default CountdownTimer;

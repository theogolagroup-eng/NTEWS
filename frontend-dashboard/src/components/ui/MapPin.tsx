import React from 'react';
import { EnvironmentOutlined } from '@ant-design/icons';

interface MapPinProps {
  className?: string;
  size?: number;
  color?: string;
}

const MapPin: React.FC<MapPinProps> = ({ className, size = 24, color }) => {
  return (
    <EnvironmentOutlined 
      className={className}
      style={{ 
        fontSize: size, 
        color: color || '#ff4d4f' 
      }} 
    />
  );
};

export default MapPin;

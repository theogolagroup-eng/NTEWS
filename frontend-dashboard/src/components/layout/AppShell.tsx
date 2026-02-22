'use client';

import React, { useState, useEffect } from 'react';
import { 
  Layout, 
  Menu, 
  Avatar, 
  Badge, 
  Dropdown, 
  Space, 
  Typography,
  Button,
  Alert
} from 'antd';
import {
  DashboardOutlined,
  AlertOutlined,
  ThunderboltOutlined,
  DatabaseOutlined,
  UserOutlined,
  BellOutlined,
  SyncOutlined,
  LogoutOutlined,
  SettingOutlined,
  BulbOutlined,
  MoonOutlined
} from '@ant-design/icons';
import { useRouter, usePathname } from 'next/navigation';
import { useTheme } from '@/contexts/ThemeContext';

const { Header, Sider, Content } = Layout;
const { Text } = Typography;

interface AppShellProps {
  children: React.ReactNode;
}

interface GlobalThreatLevel {
  level: 'critical' | 'high' | 'medium' | 'low';
  tpi: number; // Threat Probability Index
  activeAlerts: number;
  lastUpdate: string;
  userRole: string;
}

export default function AppShell({ children }: AppShellProps) {
  const [collapsed, setCollapsed] = useState(false);
  const { isDarkMode, toggleTheme, themeStyles } = useTheme();
  const [globalThreat, setGlobalThreat] = useState<GlobalThreatLevel>({
    level: 'medium',
    tpi: 65,
    activeAlerts: 12,
    lastUpdate: new Date().toISOString(),
    userRole: 'Security Analyst'
  });

  const router = useRouter();
  const pathname = usePathname();

  // Fetch global threat level periodically
  useEffect(() => {
    const fetchGlobalThreatLevel = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/intelligence/dashboard/summary');
        const data = await response.json();
        
        // Calculate overall threat level
        const totalThreats = data.criticalThreats + data.highThreats + data.mediumThreats;
        let level: 'critical' | 'high' | 'medium' | 'low' = 'low';
        
        if (data.criticalThreats > 0) level = 'critical';
        else if (data.highThreats > 5) level = 'high';
        else if (totalThreats > 10) level = 'medium';
        
        setGlobalThreat(prev => ({
          ...prev,
          level,
          tpi: Math.min(95, Math.max(5, totalThreats * 5 + data.criticalThreats * 20)),
          activeAlerts: data.activeThreats,
          lastUpdate: new Date().toISOString()
        }));
      } catch (error) {
        console.error('Failed to fetch global threat level:', error);
      }
    };

    fetchGlobalThreatLevel();
    const interval = setInterval(fetchGlobalThreatLevel, 30000); // Update every 30 seconds

    return () => clearInterval(interval);
  }, []);

  const getThreatLevelColor = (level: string) => {
    switch (level) {
      case 'critical': return '#ff4d4f';
      case 'high': return '#ff7a45';
      case 'medium': return '#ffa940';
      case 'low': return '#52c41a';
      default: return '#d9d9d9';
    }
  };

  const menuItems = [
    {
      key: '/dashboard',
      icon: <DashboardOutlined />,
      label: 'Dashboard',
    },
    {
      key: '/alerts',
      icon: <AlertOutlined />,
      label: 'Alerts',
    },
    {
      key: '/forecast',
      icon: <ThunderboltOutlined />,
      label: 'Forecast',
    },
    {
      key: '/sources',
      icon: <DatabaseOutlined />,
      label: 'Data Sources',
    },
  ];

  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: 'Profile',
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: 'Settings',
    },
    {
      type: 'divider' as const,
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: 'Logout',
      danger: true,
    },
  ];

  const handleMenuClick = ({ key }: { key: string }) => {
    router.push(key);
  };

  const handleUserMenuClick = ({ key }: { key: string }) => {
    if (key === 'logout') {
      // Handle logout
      router.push('/login');
    }
  };

  return (
    <Layout style={{ minHeight: '100vh', background: themeStyles.background }}>
      <Sider 
        trigger={null} 
        collapsible 
        collapsed={collapsed}
        style={{
          background: themeStyles.sidebarBackground,
          position: 'fixed',
          height: '100vh',
          left: 0,
          top: 0,
          bottom: 0,
        }}
      >
        <div style={{ 
          height: 64, 
          display: 'flex', 
          alignItems: 'center', 
          justifyContent: 'center',
          background: isDarkMode ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.05)',
          margin: '16px',
          borderRadius: '6px'
        }}>
          <Text style={{ color: themeStyles.sidebarTextColor, fontWeight: 'bold', fontSize: collapsed ? '14px' : '18px' }}>
            {collapsed ? 'NTEWS' : 'NTEWS'}
          </Text>
        </div>
        
        <Menu
          theme={isDarkMode ? 'dark' : 'light'}
          mode="inline"
          selectedKeys={pathname ? [pathname] : []}
          items={menuItems}
          onClick={handleMenuClick}
          style={{ background: 'transparent' }}
        />
      </Sider>

      <Layout style={{ marginLeft: collapsed ? 80 : 200, transition: 'margin-left 0.2s' }}>
        <Header style={{ 
          padding: '0 24px', 
          background: themeStyles.headerColor,
          borderBottom: `1px solid ${themeStyles.cardBorder}`,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          position: 'sticky',
          top: 0,
          zIndex: 1000
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <Button
              type="text"
              icon={collapsed ? <DashboardOutlined /> : <DashboardOutlined />}
              onClick={() => setCollapsed(!collapsed)}
              style={{ fontSize: '16px', width: 64, height: 64 }}
            />
            
            {/* Global Threat Level Banner */}
            <Alert
              message={
                <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <div 
                      style={{ 
                        width: 12, 
                        height: 12, 
                        borderRadius: '50%', 
                        backgroundColor: getThreatLevelColor(globalThreat.level) 
                      }}
                    />
                    <Text strong style={{ color: getThreatLevelColor(globalThreat.level) }}>
                      {globalThreat.level.toUpperCase()} THREAT LEVEL
                    </Text>
                  </div>
                  
                  <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                    <Text>
                      TPI: <Text strong>{globalThreat.tpi}</Text>
                    </Text>
                    <Text>
                      Active Alerts: <Text strong>{globalThreat.activeAlerts}</Text>
                    </Text>
                    <Text type="secondary" style={{ fontSize: '12px' }} suppressHydrationWarning>
                      Last Update: {new Date(globalThreat.lastUpdate).toLocaleTimeString()}
                    </Text>
                  </div>
                </div>
              }
              type={globalThreat.level === 'critical' ? 'error' : 
                    globalThreat.level === 'high' ? 'warning' : 
                    globalThreat.level === 'medium' ? 'info' : 'success'}
              showIcon={false}
              style={{ flex: 1 }}
            />
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <Badge count={globalThreat.activeAlerts} size="small">
              <Button 
                type="text" 
                icon={<BellOutlined />} 
                style={{ color: isDarkMode ? '#ffffff' : '#000000' }}
              />
            </Badge>
            
            <Button 
              type="text" 
              icon={isDarkMode ? <BulbOutlined /> : <MoonOutlined />}
              onClick={toggleTheme}
              style={{ 
                color: isDarkMode ? '#ffffff' : '#000000',
                fontSize: '16px'
              }}
              title={isDarkMode ? 'Switch to Light Mode' : 'Switch to Dark Mode'}
            />
            
            <Button 
              type="text" 
              icon={<SyncOutlined spin />} 
              size="small"
              onClick={() => window.location.reload()}
              style={{ color: isDarkMode ? '#ffffff' : '#000000' }}
            />
            
            <Dropdown
              menu={{
                items: userMenuItems,
                onClick: handleUserMenuClick,
              }}
              placement="bottomRight"
            >
              <Space style={{ cursor: 'pointer' }}>
                <Avatar size="small" icon={<UserOutlined />} />
                <Text>{globalThreat.userRole}</Text>
              </Space>
            </Dropdown>
          </div>
        </Header>

        <Content style={{ 
          margin: '24px',
          padding: '24px',
          background: themeStyles.contentBackground,
          minHeight: 'calc(100vh - 112px)'
        }}>
          {children}
        </Content>
      </Layout>
    </Layout>
  );
}

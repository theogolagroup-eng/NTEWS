'use client';

import React, { createContext, useContext, useState, useEffect } from 'react';

// Theme Context
interface ThemeContextType {
  isDarkMode: boolean;
  toggleTheme: () => void;
  themeStyles: {
    background: string;
    textColor: string;
    secondaryTextColor: string;
    cardBackground: string;
    cardBorder: string;
    headerBackground: string;
    glassBackground: string;
    textShadow: string;
    hoverBackground: string;
    dividerColor: string;
    placeholderBackground: string;
    successColor: string;
    warningColor: string;
    errorColor: string;
    infoColor: string;
    sidebarBackground: string;
    sidebarTextColor: string;
    headerColor: string;
    contentBackground: string;
  };
}

const ThemeContext = createContext<ThemeContextType>({
  isDarkMode: true,
  toggleTheme: () => {},
  themeStyles: {} as any,
});

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
  glassBackground: isDarkMode 
    ? 'rgba(255,255,255,0.06)' 
    : 'rgba(255,255,255,0.9)',
  textShadow: isDarkMode 
    ? '0 1px 2px rgba(0,0,0,0.4)' 
    : '0 1px 2px rgba(255,255,255,0.8)',
  hoverBackground: isDarkMode 
    ? 'rgba(255,255,255,0.08)' 
    : 'rgba(0,0,0,0.04)',
  dividerColor: isDarkMode 
    ? 'rgba(255,255,255,0.06)' 
    : 'rgba(0,0,0,0.06)',
  placeholderBackground: isDarkMode 
    ? 'rgba(0,0,0,0.3)' 
    : 'rgba(0,0,0,0.1)',
  successColor: '#52c41a',
  warningColor: '#fa8c16',
  errorColor: '#ff4d4f',
  infoColor: '#1890ff',
  sidebarBackground: isDarkMode ? '#001529' : '#ffffff',
  sidebarTextColor: isDarkMode ? '#ffffff' : '#000000',
  headerColor: isDarkMode ? '#001529' : '#ffffff',
  contentBackground: isDarkMode ? '#0f1419' : '#f0f2f5'
});

// Theme Provider Component
export const ThemeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [isDarkMode, setIsDarkMode] = useState(true);

  // Load theme preference from localStorage on mount
  useEffect(() => {
    const savedTheme = localStorage.getItem('ntews-theme');
    if (savedTheme) {
      setIsDarkMode(savedTheme === 'dark');
    }
  }, []);

  // Save theme preference to localStorage when it changes
  useEffect(() => {
    localStorage.setItem('ntews-theme', isDarkMode ? 'dark' : 'light');
  }, [isDarkMode]);

  const toggleTheme = () => {
    setIsDarkMode(!isDarkMode);
  };

  const themeStyles = getThemeStyles(isDarkMode);

  return (
    <ThemeContext.Provider value={{ isDarkMode, toggleTheme, themeStyles }}>
      {children}
    </ThemeContext.Provider>
  );
};

// Hook to use theme
export const useTheme = () => useContext(ThemeContext);

export default ThemeContext;

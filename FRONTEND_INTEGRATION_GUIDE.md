# Frontend Integration Guide

## Overview
Complete guide for integrating the NTEWS frontend with the updated backend services and new endpoints.

## 🚀 Quick Start

### 1. API Client Setup
The frontend API client is already configured in `src/services/api.ts` with all new endpoints.

```typescript
import { apiClient, API_ENDPOINTS } from '../services/api';

// Example: Get alerts dashboard
const dashboard = await apiClient.get(API_ENDPOINTS.ALERTS.DASHBOARD);

// Example: Login
const loginResponse = await apiClient.login({
  username: 'admin',
  password: 'admin123'
});
```

### 2. Authentication Flow
```typescript
// Login
const loginData = await apiClient.login({
  username: 'admin',
  password: 'admin123'
});

// Token is automatically set in the client
console.log('Logged in with token:', loginData.token);

// Validate token
const validation = await apiClient.validateToken();

// Logout
await apiClient.logout();
```

## 📊 Service Integration Examples

### Alert Service Integration
```typescript
// Get all alerts with filters
const alerts = await apiClient.get(API_ENDPOINTS.ALERTS.ALL + '?severity=HIGH&status=ACTIVE');

// Create new alert
const newAlert = await apiClient.post(API_ENDPOINTS.ALERTS.CREATE, {
  title: "Security Alert",
  description: "Suspicious activity detected",
  severity: "HIGH",
  category: "SECURITY",
  location: "Nairobi CBD"
});

// Acknowledge alert
const acknowledged = await apiClient.post(API_ENDPOINTS.ALERTS.ACKNOWLEDGE(alertId));

// NLP Analysis
const nlpAnalysis = await apiClient.post(API_ENDPOINTS.ALERTS.NLP_ANALYZE_TEXT, {
  text: "Armed robbery in progress",
  context: "criminal activity"
});
```

### Prediction Service Integration
```typescript
// Get risk forecasts
const forecasts = await apiClient.get(API_ENDPOINTS.PREDICTIONS.FORECASTS);

// Generate new forecast
const newForecast = await apiClient.post(API_ENDPOINTS.PREDICTIONS.GENERATE_FORECAST, {
  forecastType: "risk_trend",
  parameters: { hours: 24 }
});

// Get hotspots
const hotspots = await apiClient.get(API_ENDPOINTS.PREDICTIONS.HOTSPOTS);

// Location-based risk
const locationRisk = await apiClient.get(
  API_ENDPOINTS.PREDICTIONS.LOCATION_RISK + '?lat=-1.2864&lon=36.8172'
);
```

### Intelligence Service Integration
```typescript
// Get intelligence reports
const reports = await apiClient.get(API_ENDPOINTS.INTELLIGENCE.REPORTS);

// Create new report
const newReport = await apiClient.post(API_ENDPOINTS.INTELLIGENCE.CREATE, {
  title: "Intelligence Brief",
  threatLevel: "HIGH",
  category: "SECURITY",
  content: "Gathering intelligence on suspicious activities"
});

// Get threat trends
const trends = await apiClient.get(API_ENDPOINTS.INTELLIGENCE.THREAT_TRENDS);

// Get threat map data
const threatMap = await apiClient.get(API_ENDPOINTS.INTELLIGENCE.THREAT_MAP);
```

### Ingestion Service Integration
```typescript
// Ingest social media data
const socialData = await apiClient.post(API_ENDPOINTS.INGESTION.SOCIAL_MEDIA, {
  id: "sm-001",
  platform: "twitter",
  content: "Protest forming at central park #nairobi",
  author: "user123",
  timestamp: new Date().toISOString(),
  geoTagLatitude: -1.2864,
  geoTagLongitude: 36.8172
});

// Ingest CCTV data
const cctvData = await apiClient.post(API_ENDPOINTS.INGESTION.CCTV, {
  id: "cctv-001",
  cameraId: "CAM-001",
  imageUrl: "http://example.com/image.jpg",
  timestamp: new Date().toISOString(),
  latitude: -1.2864,
  longitude: 36.8172
});

// Start batch ingestion
const batchResult = await apiClient.post(API_ENDPOINTS.INGESTION.START_BATCH);
```

### AI Engine Integration
```typescript
// Direct AI Engine analysis
const analysis = await apiClient.post(API_ENDPOINTS.AI_ENGINE.ANALYZE, {
  id: "threat-001",
  type: "threat",
  source: "manual",
  content: "Suspicious activity detected",
  timestamp: new Date().toISOString(),
  severity: "high"
});

// Risk prediction
const prediction = await apiClient.post(API_ENDPOINTS.AI_ENGINE.PREDICT, {
  reports: [],
  lookback_days: 30,
  forecast_hours: 24
});

// Hotspot prediction
const hotspots = await apiClient.post(API_ENDPOINTS.AI_ENGINE.PREDICT_HOTSPOTS, {
  center_latitude: -1.2864,
  center_longitude: 36.8172,
  radius_km: 50
});

// NLP Analysis
const nlpResult = await apiClient.post(API_ENDPOINTS.AI_ENGINE.NLP_ANALYZE_TEXT, {
  text: "Multiple gunshots heard in the area",
  context: "security incident"
});
```

## 🎯 React Component Examples

### Alert Dashboard Component
```typescript
import React, { useState, useEffect } from 'react';
import { apiClient, API_ENDPOINTS } from '../services/api';

const AlertDashboard: React.FC = () => {
  const [dashboard, setDashboard] = useState(null);
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      const [dashboardData, alertsData] = await Promise.all([
        apiClient.get(API_ENDPOINTS.ALERTS.DASHBOARD),
        apiClient.get(API_ENDPOINTS.ALERTS.ACTIVE)
      ]);
      
      setDashboard(dashboardData);
      setAlerts(alertsData);
    } catch (error) {
      console.error('Failed to load dashboard:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleAcknowledge = async (alertId: string) => {
    try {
      await apiClient.post(API_ENDPOINTS.ALERTS.ACKNOWLEDGE(alertId));
      loadDashboardData(); // Refresh data
    } catch (error) {
      console.error('Failed to acknowledge alert:', error);
    }
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div>
      <h2>Alert Dashboard</h2>
      <div>
        <p>Total Alerts: {dashboard.totalAlerts}</p>
        <p>Active Alerts: {dashboard.activeAlerts}</p>
        <p>Critical Alerts: {dashboard.criticalAlerts}</p>
      </div>
      
      <h3>Active Alerts</h3>
      <ul>
        {alerts.map(alert => (
          <li key={alert.id}>
            {alert.title} - {alert.severity}
            <button onClick={() => handleAcknowledge(alert.id)}>
              Acknowledge
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default AlertDashboard;
```

### Prediction Dashboard Component
```typescript
import React, { useState, useEffect } from 'react';
import { apiClient, API_ENDPOINTS } from '../services/api';

const PredictionDashboard: React.FC = () => {
  const [forecasts, setForecasts] = useState([]);
  const [hotspots, setHotspots] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadPredictionData();
  }, []);

  const loadPredictionData = async () => {
    try {
      const [forecastsData, hotspotsData] = await Promise.all([
        apiClient.get(API_ENDPOINTS.PREDICTIONS.FORECASTS),
        apiClient.get(API_ENDPOINTS.PREDICTIONS.HOTSPOTS)
      ]);
      
      setForecasts(forecastsData.content || forecastsData);
      setHotspots(hotspotsData.content || hotspotsData);
    } catch (error) {
      console.error('Failed to load prediction data:', error);
    } finally {
      setLoading(false);
    }
  };

  const generateNewForecast = async () => {
    try {
      await apiClient.post(API_ENDPOINTS.PREDICTIONS.GENERATE_FORECAST, {
        forecastType: "risk_trend",
        parameters: { hours: 24 }
      });
      loadPredictionData(); // Refresh data
    } catch (error) {
      console.error('Failed to generate forecast:', error);
    }
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div>
      <h2>Prediction Dashboard</h2>
      
      <button onClick={generateNewForecast}>
        Generate New Forecast
      </button>
      
      <h3>Risk Forecasts</h3>
      <ul>
        {forecasts.map(forecast => (
          <li key={forecast.id}>
            {forecast.riskLevel} - {forecast.confidence}% confidence
          </li>
        ))}
      </ul>
      
      <h3>Threat Hotspots</h3>
      <ul>
        {hotspots.map(hotspot => (
          <li key={hotspot.id}>
            {hotspot.location} - Risk: {hotspot.probability}
          </li>
        ))}
      </ul>
    </div>
  );
};

export default PredictionDashboard;
```

### Authentication Component
```typescript
import React, { useState } from 'react';
import { apiClient } from '../services/api';

const LoginForm: React.FC = () => {
  const [credentials, setCredentials] = useState({
    username: '',
    password: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setCredentials({
      ...credentials,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await apiClient.login(credentials);
      console.log('Login successful:', response);
      // Redirect to dashboard or update app state
    } catch (error) {
      setError('Login failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <h2>Login</h2>
      {error && <div style={{ color: 'red' }}>{error}</div>}
      
      <div>
        <label>Username:</label>
        <input
          type="text"
          name="username"
          value={credentials.username}
          onChange={handleChange}
          required
        />
      </div>
      
      <div>
        <label>Password:</label>
        <input
          type="password"
          name="password"
          value={credentials.password}
          onChange={handleChange}
          required
        />
      </div>
      
      <button type="submit" disabled={loading}>
        {loading ? 'Logging in...' : 'Login'}
      </button>
    </form>
  );
};

export default LoginForm;
```

## 🔧 Error Handling

### Global Error Handler
```typescript
import React, { createContext, useContext, useState } from 'react';

interface ErrorContextType {
  error: string | null;
  setError: (error: string | null) => void;
}

const ErrorContext = createContext<ErrorContextType | null>(null);

export const ErrorProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [error, setError] = useState<string | null>(null);

  return (
    <ErrorContext.Provider value={{ error, setError }}>
      {children}
    </ErrorContext.Provider>
  );
};

export const useError = () => {
  const context = useContext(ErrorContext);
  if (!context) {
    throw new Error('useError must be used within ErrorProvider');
  }
  return context;
};
```

### API Error Wrapper
```typescript
import { useError } from '../contexts/ErrorContext';

export const useApiCall = () => {
  const { setError } = useError();

  const apiCall = async (apiFunction: () => Promise<any>) => {
    try {
      return await apiFunction();
    } catch (error) {
      console.error('API call failed:', error);
      setError(error.message || 'An unexpected error occurred');
      throw error;
    }
  };

  return { apiCall };
};
```

## 📱 Real-time Updates

### WebSocket Integration (Optional)
```typescript
const useRealTimeUpdates = (endpoint: string) => {
  const [data, setData] = useState(null);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    const ws = new WebSocket(`ws://localhost:8080/ws/${endpoint}`);

    ws.onopen = () => {
      setConnected(true);
      console.log('WebSocket connected');
    };

    ws.onmessage = (event) => {
      const newData = JSON.parse(event.data);
      setData(newData);
    };

    ws.onclose = () => {
      setConnected(false);
      console.log('WebSocket disconnected');
    };

    return () => {
      ws.close();
    };
  }, [endpoint]);

  return { data, connected };
};
```

## 🎨 UI Components

### Loading Spinner
```typescript
const LoadingSpinner: React.FC = () => (
  <div className="flex justify-center items-center">
    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
  </div>
);
```

### Error Alert
```typescript
const ErrorAlert: React.FC<{ message: string; onClose: () => void }> = ({ 
  message, 
  onClose 
}) => (
  <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative">
    <span className="block sm:inline">{message}</span>
    <button 
      onClick={onClose}
      className="absolute top-0 bottom-0 right-0 px-4 py-3"
    >
      <span>&times;</span>
    </button>
  </div>
);
```

## 🚀 Deployment Notes

### Environment Variables
Create `.env.local` in the frontend root:
```
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
NEXT_PUBLIC_AI_ENGINE_URL=http://localhost:8000
```

### Production Configuration
Update `src/services/api.ts` for production:
```typescript
const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';
```

## 📊 Performance Optimization

### React Query for Data Fetching
```typescript
import { useQuery, useMutation, useQueryClient } from 'react-query';

// Custom hook for alerts
export const useAlerts = (filters = {}) => {
  return useQuery(
    ['alerts', filters],
    () => apiClient.get(API_ENDPOINTS.ALERTS.ALL + new URLSearchParams(filters)),
    {
      staleTime: 30000, // 30 seconds
      refetchInterval: 60000, // 1 minute
    }
  );
};

// Custom hook for mutations
export const useCreateAlert = () => {
  const queryClient = useQueryClient();
  
  return useMutation(
    (alertData) => apiClient.post(API_ENDPOINTS.ALERTS.CREATE, alertData),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('alerts');
      },
    }
  );
};
```

## ✅ Testing Checklist

### Frontend Integration Testing
- [ ] Authentication flow works correctly
- [ ] All service endpoints accessible
- [ ] Error handling displays properly
- [ ] Loading states show correctly
- [ ] Data refreshes on actions
- [ ] Responsive design works
- [ ] CORS issues resolved
- [ ] Real-time updates functional (if implemented)

### API Integration Testing
```bash
# Run the comprehensive test script
cd ai-engine
python simple_test.py

# Test specific endpoints
curl -X GET http://localhost:8080/api/alerts/dashboard/summary
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

## 🎯 Next Steps

1. **Implement Components**: Create React components for each service
2. **Add State Management**: Implement Redux or Context API
3. **Add Charts**: Integrate charting libraries for data visualization
4. **Add Maps**: Implement map components for hotspot visualization
5. **Add Real-time**: Implement WebSocket connections for live updates
6. **Add Testing**: Write unit and integration tests
7. **Add CI/CD**: Set up deployment pipelines

The frontend is now fully ready to integrate with all backend services and new endpoints!

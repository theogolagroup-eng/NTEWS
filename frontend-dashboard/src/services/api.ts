// API Service Configuration for NTEWS Frontend
// All API calls should go through the API Gateway at localhost:8080

const API_BASE_URL = 'http://localhost:8080';

// API Endpoints
export const API_ENDPOINTS = {
  // Auth Service
  AUTH: {
    LOGIN: `${API_BASE_URL}/api/auth/login`,
    REGISTER: `${API_BASE_URL}/api/auth/register`,
    PROFILE: `${API_BASE_URL}/api/auth/profile`,
  },
  
  // Intelligence Service
  INTELLIGENCE: {
    DASHBOARD: `${API_BASE_URL}/api/intelligence/dashboard/summary`,
    REPORTS: `${API_BASE_URL}/api/intelligence/reports`,
    ANALYZE: `${API_BASE_URL}/api/intelligence/analyze`,
  },
  
  // Alert Service
  ALERTS: {
    DASHBOARD: `${API_BASE_URL}/api/alerts/dashboard/summary`,
    ALL: `${API_BASE_URL}/api/alerts`,
    CREATE: `${API_BASE_URL}/api/alerts`,
    UPDATE: (id: string) => `${API_BASE_URL}/api/alerts/${id}`,
    DELETE: (id: string) => `${API_BASE_URL}/api/alerts/${id}`,
  },
  
  // Prediction Service
  PREDICTIONS: {
    DASHBOARD: `${API_BASE_URL}/api/predictions/dashboard/summary`,
    FORECAST: `${API_BASE_URL}/api/predictions/forecast`,
    HOTSPOTS: `${API_BASE_URL}/api/predictions/hotspots`,
  },
  
  // Ingestion Service
  INGESTION: {
    DATA_SOURCES: `${API_BASE_URL}/api/ingestion/sources`,
    INGEST: `${API_BASE_URL}/api/ingestion/ingest`,
    STATUS: `${API_BASE_URL}/api/ingestion/status`,
  },
  
  // AI Engine (through API Gateway for unified access)
  AI_ENGINE: {
    BASE: `${API_BASE_URL}/api/ai-engine`,
    PREDICT: `${API_BASE_URL}/api/ai-engine/predict`,
    ANALYZE: `${API_BASE_URL}/api/ai-engine/analyze`,
    HOTSPOTS: `${API_BASE_URL}/api/ai-engine/predict/hotspots`,
    HEALTH: `${API_BASE_URL}/api/ai-engine/health`,
    STATS: `${API_BASE_URL}/api/ai-engine/stats`,
    MODELS: `${API_BASE_URL}/api/ai-engine/models`,
    CAPABILITIES: `${API_BASE_URL}/api/ai-engine/capabilities`,
    PREDICTION_ANALYSIS: `${API_BASE_URL}/api/ai-engine/prediction-analysis`
  }
};

// API Client with error handling
export class ApiClient {
  private baseUrl: string;
  
  constructor(baseUrl: string = API_BASE_URL) {
    this.baseUrl = baseUrl;
  }
  
  async request(endpoint: string, options: RequestInit = {}): Promise<any> {
    try {
      const url = endpoint.startsWith('http') ? endpoint : `${this.baseUrl}${endpoint}`;
      
      const response = await fetch(url, {
        headers: {
          'Content-Type': 'application/json',
          ...options.headers,
        },
        ...options,
      });
      
      if (!response.ok) {
        throw new Error(`API Error: ${response.status} ${response.statusText}`);
      }
      
      return await response.json();
    } catch (error) {
      console.error('API Request failed:', error);
      throw error;
    }
  }
  
  async get(endpoint: string): Promise<any> {
    return this.request(endpoint, { method: 'GET' });
  }
  
  async post(endpoint: string, data?: any): Promise<any> {
    return this.request(endpoint, {
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined,
    });
  }
  
  async put(endpoint: string, data?: any): Promise<any> {
    return this.request(endpoint, {
      method: 'PUT',
      body: data ? JSON.stringify(data) : undefined,
    });
  }
  
  async delete(endpoint: string): Promise<any> {
    return this.request(endpoint, { method: 'DELETE' });
  }
}

// Create API client instances
export const apiClient = new ApiClient();
export const aiEngineClient = new ApiClient('http://localhost:8000');

// Export default API client
export default apiClient;
